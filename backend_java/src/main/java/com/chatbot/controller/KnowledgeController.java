package com.chatbot.controller;

import com.chatbot.dto.DocumentUploadDto;
import com.chatbot.dto.KnowledgeBaseDto;
import com.chatbot.dto.SearchResultDto;
import com.chatbot.entity.DynamicToolEntity;
import com.chatbot.entity.KnowledgeBaseEntity;
import com.chatbot.entity.KnowledgeChunkEntity;
import com.chatbot.entity.KnowledgeDocumentEntity;
import com.chatbot.entity.ToolType;
import com.chatbot.repository.DynamicToolRepository;
import com.chatbot.repository.KnowledgeBaseRepository;
import com.chatbot.repository.KnowledgeChunkRepository;
import com.chatbot.repository.KnowledgeDocumentRepository;
import com.chatbot.service.DocumentChunkerService;
import com.chatbot.service.SemanticSearchService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/knowledge")
public class KnowledgeController {

    private static final Logger log = LoggerFactory.getLogger(KnowledgeController.class);

    private final KnowledgeBaseRepository kbRepository;
    private final KnowledgeDocumentRepository docRepository;
    private final KnowledgeChunkRepository chunkRepository;
    private final DocumentChunkerService chunkerService;
    private final SemanticSearchService searchService;
    private final DynamicToolRepository toolRepository;

    public KnowledgeController(KnowledgeBaseRepository kbRepository,
                               KnowledgeDocumentRepository docRepository,
                               KnowledgeChunkRepository chunkRepository,
                               DocumentChunkerService chunkerService,
                               SemanticSearchService searchService,
                               DynamicToolRepository toolRepository) {
        this.kbRepository = kbRepository;
        this.docRepository = docRepository;
        this.chunkRepository = chunkRepository;
        this.chunkerService = chunkerService;
        this.searchService = searchService;
        this.toolRepository = toolRepository;
    }

    /**
     * Lấy danh sách tất cả các Kho Tri Thức kèm số lượng tài liệu & chunks
     */
    @GetMapping
    public List<KnowledgeBaseDto> listKnowledgeBases() {
        List<KnowledgeBaseEntity> list = kbRepository.findAllByOrderByCreatedAtDesc();
        return list.stream().map(kb -> {
            KnowledgeBaseDto dto = new KnowledgeBaseDto();
            dto.setId(kb.getId());
            dto.setName(kb.getName());
            dto.setDescription(kb.getDescription());
            dto.setIcon(kb.getIcon());
            dto.setCreatedAt(kb.getCreatedAt());
            dto.setUpdatedAt(kb.getUpdatedAt());
            dto.setDocumentCount(docRepository.countByKnowledgeBaseId(kb.getId()));
            dto.setChunkCount(chunkRepository.countByKnowledgeBaseId(kb.getId()));
            return dto;
        }).collect(Collectors.toList());
    }

    /**
     * Tạo mới một Kho Tri Thức
     */
    @PostMapping
    public ResponseEntity<KnowledgeBaseEntity> createKnowledgeBase(@RequestBody KnowledgeBaseEntity request) {
        if (request.getName() == null || request.getName().trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        if (request.getIcon() == null || request.getIcon().trim().isEmpty()) {
            request.setIcon("📚");
        }
        KnowledgeBaseEntity saved = kbRepository.save(request);
        log.info("[Knowledge Base Created] ID: {}, Name: {}", saved.getId(), saved.getName());
        return ResponseEntity.ok(saved);
    }

    /**
     * Xóa một Kho Tri Thức và toàn bộ tài liệu & chunks liên quan
     */
    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<Void> deleteKnowledgeBase(@PathVariable Long id) {
        if (!kbRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        chunkRepository.deleteByKnowledgeBaseId(id);
        docRepository.deleteByKnowledgeBaseId(id);
        kbRepository.deleteById(id);
        log.info("[Knowledge Base Deleted] ID: {}", id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Lấy danh sách tài liệu trong một Kho Tri Thức
     */
    @GetMapping("/{id}/documents")
    public ResponseEntity<List<KnowledgeDocumentEntity>> listDocuments(@PathVariable Long id) {
        if (!kbRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        List<KnowledgeDocumentEntity> docs = docRepository.findByKnowledgeBaseIdOrderByCreatedAtDesc(id);
        return ResponseEntity.ok(docs);
    }

    /**
     * Thêm tài liệu mới vào Kho Tri Thức (Tự động phân đoạn / Chunking)
     */
    @PostMapping("/{id}/documents")
    @Transactional
    public ResponseEntity<?> uploadDocument(@PathVariable Long id, @RequestBody DocumentUploadDto dto) {
        Optional<KnowledgeBaseEntity> kbOpt = kbRepository.findById(id);
        if (!kbOpt.isPresent()) {
            return ResponseEntity.notFound().build();
        }

        if (dto.getContent() == null || dto.getContent().trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Collections.singletonMap("error", "Nội dung tài liệu không được để trống."));
        }

        String title = (dto.getTitle() != null && !dto.getTitle().trim().isEmpty())
                ? dto.getTitle().trim()
                : "Tài liệu " + System.currentTimeMillis();

        String sourceType = (dto.getSourceType() != null && !dto.getSourceType().trim().isEmpty())
                ? dto.getSourceType().trim()
                : "TEXT";

        // 1. Phân đoạn văn bản thành các chunks
        List<String> rawChunks = chunkerService.chunkText(dto.getContent(), 500, 60);

        // 2. Lưu document
        KnowledgeDocumentEntity doc = new KnowledgeDocumentEntity();
        doc.setKnowledgeBaseId(id);
        doc.setTitle(title);
        doc.setSourceType(sourceType);
        doc.setContent(dto.getContent());
        doc.setCharCount(dto.getContent().length());
        doc.setChunkCount(rawChunks.size());
        KnowledgeDocumentEntity savedDoc = docRepository.save(doc);

        // 3. Lưu từng chunk vào H2DB
        List<KnowledgeChunkEntity> chunkEntities = new ArrayList<>();
        for (int i = 0; i < rawChunks.size(); i++) {
            KnowledgeChunkEntity chunk = new KnowledgeChunkEntity(
                    savedDoc.getId(),
                    id,
                    i + 1,
                    rawChunks.get(i)
            );
            chunkEntities.add(chunk);
        }
        chunkRepository.saveAll(chunkEntities);

        log.info("[Document Ingestion] Doc ID: {}, Title: '{}', Chunks: {}", savedDoc.getId(), title, rawChunks.size());

        Map<String, Object> result = new HashMap<>();
        result.put("documentId", savedDoc.getId());
        result.put("title", savedDoc.getTitle());
        result.put("chunkCount", rawChunks.size());
        result.put("charCount", savedDoc.getCharCount());
        result.put("previewChunks", rawChunks.stream().limit(3).collect(Collectors.toList()));

        return ResponseEntity.ok(result);
    }

    /**
     * Thử nghiệm Semantic Search trực tiếp trên Kho Tri Thức
     */
    @PostMapping("/{id}/search")
    public ResponseEntity<?> searchKnowledgeBase(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        if (!kbRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }

        String query = body.get("query") != null ? body.get("query").toString() : "";
        int topK = body.get("topK") != null ? Integer.parseInt(body.get("topK").toString()) : 5;

        long start = System.currentTimeMillis();
        List<SearchResultDto> results = searchService.search(id, query, topK);
        long timeMs = System.currentTimeMillis() - start;

        Map<String, Object> res = new HashMap<>();
        res.put("query", query);
        res.put("totalResults", results.size());
        res.put("executionTimeMs", timeMs);
        res.put("results", results);

        return ResponseEntity.ok(res);
    }

    /**
     * 1-Click: Tự động liên kết Kho Tri Thức này thành một Dynamic Tool cho AI Agent
     */
    @PostMapping("/{id}/create-tool")
    public ResponseEntity<?> createToolFromKnowledgeBase(@PathVariable Long id) {
        Optional<KnowledgeBaseEntity> kbOpt = kbRepository.findById(id);
        if (!kbOpt.isPresent()) {
            return ResponseEntity.notFound().build();
        }

        KnowledgeBaseEntity kb = kbOpt.get();
        String toolName = "search_kb_" + kb.getName().toLowerCase()
                .replaceAll("[^a-zA-Z0-9_]+", "_")
                .replaceAll("^_+|_+$", "");

        if (toolName.length() > 50) {
            toolName = toolName.substring(0, 50);
        }

        DynamicToolEntity tool = toolRepository.findByName(toolName).orElse(new DynamicToolEntity());
        tool.setName(toolName);
        tool.setDescription("Tra cứu và tìm kiếm thông tin từ kho tri thức: " + kb.getName() + " (" + (kb.getDescription() != null ? kb.getDescription() : "") + ")");
        tool.setToolType(ToolType.QDRANT_VECTOR); // Sử dụng engine semantic search
        tool.setParametersSchema("{\n" +
                "  \"type\": \"object\",\n" +
                "  \"properties\": {\n" +
                "    \"query\": {\n" +
                "      \"type\": \"string\",\n" +
                "      \"description\": \"Từ khóa hoặc câu hỏi cần tra cứu trong kho tri thức " + kb.getName() + "\"\n" +
                "    }\n" +
                "  },\n" +
                "  \"required\": [\"query\"]\n" +
                "}");
        tool.setConfigData("{\"knowledgeBaseId\": " + kb.getId() + ", \"kbName\": \"" + kb.getName() + "\"}");
        tool.setEnabled(true);

        DynamicToolEntity savedTool = toolRepository.save(tool);
        log.info("[Knowledge Tool Created] Linked KB ID {} to Tool '{}'", kb.getId(), savedTool.getName());

        return ResponseEntity.ok(savedTool);
    }
}

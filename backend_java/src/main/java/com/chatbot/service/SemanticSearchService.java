package com.chatbot.service;

import com.chatbot.dto.SearchResultDto;
import com.chatbot.entity.KnowledgeChunkEntity;
import com.chatbot.repository.KnowledgeChunkRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class SemanticSearchService {

    private static final Logger log = LoggerFactory.getLogger(SemanticSearchService.class);
    private static final Pattern WORD_SPLIT = Pattern.compile("[\\s\\p{Punct}]+");

    private final KnowledgeChunkRepository chunkRepository;

    public SemanticSearchService(KnowledgeChunkRepository chunkRepository) {
        this.chunkRepository = chunkRepository;
    }

    /**
     * Tìm kiếm đoạn văn bản tương đồng ngữ nghĩa nhất trong một Kho Tri Thức (Top-K)
     */
    public List<SearchResultDto> search(Long knowledgeBaseId, String query, int topK) {
        if (query == null || query.trim().isEmpty()) {
            return Collections.emptyList();
        }

        List<KnowledgeChunkEntity> chunks = chunkRepository.findByKnowledgeBaseId(knowledgeBaseId);
        if (chunks.isEmpty()) {
            return Collections.emptyList();
        }

        List<String> queryTokens = tokenize(query);
        if (queryTokens.isEmpty()) {
            return Collections.emptyList();
        }

        // 1. Tính Document Frequency (DF) cho từng token trong toàn bộ tập chunks
        Map<String, Integer> docFreqMap = new HashMap<>();
        List<Map<String, Integer>> chunkTermFreqs = new ArrayList<>(chunks.size());

        for (KnowledgeChunkEntity chunk : chunks) {
            List<String> tokens = tokenize(chunk.getContent());
            Map<String, Integer> tfMap = new HashMap<>();
            for (String t : tokens) {
                tfMap.put(t, tfMap.getOrDefault(t, 0) + 1);
            }
            chunkTermFreqs.add(tfMap);

            for (String uniqueToken : tfMap.keySet()) {
                docFreqMap.put(uniqueToken, docFreqMap.getOrDefault(uniqueToken, 0) + 1);
            }
        }

        int totalDocs = chunks.size();

        // 2. Tạo vector trọng số TF-IDF cho Query
        Map<String, Integer> queryTf = new HashMap<>();
        for (String t : queryTokens) {
            queryTf.put(t, queryTf.getOrDefault(t, 0) + 1);
        }

        Map<String, Double> queryVector = new HashMap<>();
        double queryNormSq = 0.0;

        for (Map.Entry<String, Integer> entry : queryTf.entrySet()) {
            String term = entry.getKey();
            int df = docFreqMap.getOrDefault(term, 1);
            double idf = Math.log((totalDocs + 1.0) / (df + 0.5)) + 1.0;
            double weight = (1.0 + Math.log(entry.getValue())) * idf;
            queryVector.put(term, weight);
            queryNormSq += weight * weight;
        }
        double queryNorm = Math.sqrt(queryNormSq);

        if (queryNorm == 0.0) {
            return Collections.emptyList();
        }

        // 3. Tính Cosine Similarity giữa Query và từng Chunk
        List<SearchResultDto> scoredResults = new ArrayList<>();

        for (int i = 0; i < chunks.size(); i++) {
            KnowledgeChunkEntity chunk = chunks.get(i);
            Map<String, Integer> chunkTf = chunkTermFreqs.get(i);

            double dotProduct = 0.0;
            double chunkNormSq = 0.0;

            for (Map.Entry<String, Integer> entry : chunkTf.entrySet()) {
                String term = entry.getKey();
                int df = docFreqMap.getOrDefault(term, 1);
                double idf = Math.log((totalDocs + 1.0) / (df + 0.5)) + 1.0;
                double weight = (1.0 + Math.log(entry.getValue())) * idf;
                chunkNormSq += weight * weight;

                if (queryVector.containsKey(term)) {
                    dotProduct += weight * queryVector.get(term);
                }
            }

            double chunkNorm = Math.sqrt(chunkNormSq);
            double similarity = 0.0;
            if (chunkNorm > 0.0 && dotProduct > 0.0) {
                similarity = dotProduct / (queryNorm * chunkNorm);
            }

            // Keyword boost nếu query chứa trực tiếp cụm từ trong chunk
            String lowerContent = chunk.getContent().toLowerCase();
            String lowerQuery = query.toLowerCase().trim();
            if (lowerContent.contains(lowerQuery)) {
                similarity = Math.min(1.0, similarity + 0.25);
            }

            if (similarity > 0.05) {
                double roundedScore = Math.round(similarity * 1000.0) / 1000.0;
                scoredResults.add(new SearchResultDto(
                        chunk.getId(),
                        chunk.getDocumentId(),
                        chunk.getChunkIndex(),
                        chunk.getContent(),
                        roundedScore
                ));
            }
        }

        // 4. Sắp xếp giảm dần theo điểm tương đồng và lấy top-K
        scoredResults.sort((a, b) -> Double.compare(b.getScore(), a.getScore()));

        int limit = Math.min(topK > 0 ? topK : 5, scoredResults.size());
        return scoredResults.stream().limit(limit).collect(Collectors.toList());
    }

    private List<String> tokenize(String text) {
        if (text == null || text.trim().isEmpty()) {
            return Collections.emptyList();
        }

        String[] parts = WORD_SPLIT.split(text.toLowerCase());
        List<String> tokens = new ArrayList<>();
        for (String p : parts) {
            String trimmed = p.trim();
            if (trimmed.length() >= 2) {
                tokens.add(trimmed);
            }
        }
        return tokens;
    }
}

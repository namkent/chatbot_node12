package com.chatbot.service;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class DocumentChunkerService {

    private static final int DEFAULT_CHUNK_SIZE = 500;
    private static final int DEFAULT_OVERLAP = 60;

    /**
     * Phân đoạn văn bản thông minh theo đoạn văn và câu với độ chồng lấn (overlap)
     */
    public List<String> chunkText(String rawText, Integer chunkSize, Integer overlap) {
        List<String> chunks = new ArrayList<>();
        if (rawText == null || rawText.trim().isEmpty()) {
            return chunks;
        }

        int targetChunkSize = (chunkSize != null && chunkSize > 100) ? chunkSize : DEFAULT_CHUNK_SIZE;
        int targetOverlap = (overlap != null && overlap >= 0 && overlap < targetChunkSize) ? overlap : DEFAULT_OVERLAP;

        String text = rawText.trim().replace("\r\n", "\n").replace("\r", "\n");

        // Nếu toàn bộ văn bản ngắn hơn targetChunkSize thì tạo đúng 1 chunk
        if (text.length() <= targetChunkSize) {
            chunks.add(text);
            return chunks;
        }

        // Tách sơ bộ theo đoạn văn "\n\n" hoặc ngắt dòng "\n"
        String[] paragraphs = text.split("\n\n+");

        StringBuilder currentChunk = new StringBuilder();

        for (String para : paragraphs) {
            String trimmedPara = para.trim();
            if (trimmedPara.isEmpty()) continue;

            // Nếu đoạn văn hiện tại cộng thêm đoạn mới nhỏ hơn hoặc bằng chunkSize
            if (currentChunk.length() + trimmedPara.length() + 2 <= targetChunkSize) {
                if (currentChunk.length() > 0) {
                    currentChunk.append("\n\n");
                }
                currentChunk.append(trimmedPara);
            } else {
                // Nếu currentChunk đã có nội dung, lưu lại
                if (currentChunk.length() > 0) {
                    chunks.add(currentChunk.toString().trim());

                    // Tạo overlap từ phần đuôi của chunk vừa lưu
                    String prevStr = currentChunk.toString();
                    currentChunk.setLength(0);

                    if (targetOverlap > 0 && prevStr.length() > targetOverlap) {
                        String overlapText = prevStr.substring(prevStr.length() - targetOverlap);
                        // Cố gắng ngắt ở dấu cách hoặc dấu chấm
                        int spaceIdx = overlapText.indexOf(' ');
                        if (spaceIdx != -1 && spaceIdx < overlapText.length() - 5) {
                            overlapText = overlapText.substring(spaceIdx + 1);
                        }
                        currentChunk.append(overlapText).append("\n\n");
                    }
                }

                // Nếu riêng đoạn para này quá dài (> targetChunkSize), chia nhỏ theo câu
                if (trimmedPara.length() > targetChunkSize) {
                    List<String> subChunks = splitLongParagraph(trimmedPara, targetChunkSize, targetOverlap);
                    for (int i = 0; i < subChunks.size(); i++) {
                        if (i == subChunks.size() - 1) {
                            currentChunk.append(subChunks.get(i));
                        } else {
                            chunks.add(subChunks.get(i));
                        }
                    }
                } else {
                    currentChunk.append(trimmedPara);
                }
            }
        }

        if (currentChunk.length() > 0) {
            chunks.add(currentChunk.toString().trim());
        }

        return chunks;
    }

    private List<String> splitLongParagraph(String paragraph, int chunkSize, int overlap) {
        List<String> list = new ArrayList<>();
        int start = 0;
        int length = paragraph.length();

        while (start < length) {
            int end = Math.min(start + chunkSize, length);

            // Tìm điểm ngắt câu hợp lý (dấu chấm, dấu hỏi, dấu chấm than, xuống dòng)
            if (end < length) {
                int sentenceEnd = -1;
                for (int i = end; i >= start + (chunkSize / 2); i--) {
                    char c = paragraph.charAt(i);
                    if (c == '.' || c == '!' || c == '?' || c == '\n') {
                        sentenceEnd = i + 1;
                        break;
                    }
                }
                if (sentenceEnd != -1) {
                    end = sentenceEnd;
                } else {
                    // Nếu không có dấu câu, tìm dấu cách gần nhất
                    int spaceIdx = paragraph.lastIndexOf(' ', end);
                    if (spaceIdx > start + (chunkSize / 2)) {
                        end = spaceIdx + 1;
                    }
                }
            }

            String chunk = paragraph.substring(start, end).trim();
            if (!chunk.isEmpty()) {
                list.add(chunk);
            }

            if (end >= length) break;
            start = Math.max(start + 1, end - overlap);
        }

        return list;
    }
}

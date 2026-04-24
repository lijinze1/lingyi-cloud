package com.lingyi.ai.rag.service.impl;

import com.lingyi.ai.rag.config.RagDefaultsProperties;
import com.lingyi.ai.rag.entity.LyKb;
import com.lingyi.ai.rag.entity.LyKbFile;
import com.lingyi.ai.rag.service.KnowledgeDocumentParser;
import com.lingyi.common.web.domain.ErrorCode;
import com.lingyi.common.web.exception.BizException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;

@Service
public class SpringAiKnowledgeDocumentParser implements KnowledgeDocumentParser {

    private final RagDefaultsProperties defaultsProperties;

    public SpringAiKnowledgeDocumentParser(RagDefaultsProperties defaultsProperties) {
        this.defaultsProperties = defaultsProperties;
    }

    @Override
    public List<Document> parse(LyKb kb, LyKbFile kbFile, byte[] fileBytes) {
        String fileType = normalizeType(kbFile.getFileType(), kbFile.getFileName());
        if (!defaultsProperties.getSupportedFileTypes().contains(fileType)) {
            throw new BizException(ErrorCode.KB_FILE_TYPE_UNSUPPORTED, "unsupported file type: " + fileType);
        }
        List<Document> sourceDocuments = switch (fileType) {
            case "txt", "md" -> List.of(new Document(new String(fileBytes, StandardCharsets.UTF_8),
                    baseMetadata(kb, kbFile, fileType, null)));
            case "pdf" -> parsePdf(kb, kbFile, fileBytes);
            case "docx" -> parseDocx(kb, kbFile, fileBytes);
            default -> throw new BizException(ErrorCode.KB_FILE_TYPE_UNSUPPORTED, "unsupported file type: " + fileType);
        };
        TokenTextSplitter splitter = TokenTextSplitter.builder()
                .withChunkSize(kb.getChunkSize() == null ? 500 : kb.getChunkSize())
                .withMinChunkSizeChars(50)
                .withMinChunkLengthToEmbed(5)
                .withMaxNumChunks(10000)
                .withKeepSeparator(true)
                .build();
        return splitter.apply(sourceDocuments);
    }

    private List<Document> parsePdf(LyKb kb, LyKbFile kbFile, byte[] fileBytes) {
        List<Document> pages = new PagePdfDocumentReader(new ByteArrayResource(fileBytes)).get();
        List<Document> documents = new ArrayList<>();
        for (int i = 0; i < pages.size(); i++) {
            Document page = pages.get(i);
            Map<String, Object> metadata = new LinkedHashMap<>(page.getMetadata());
            metadata.putAll(baseMetadata(kb, kbFile, "pdf", i + 1));
            documents.add(new Document(page.getText(), metadata));
        }
        return documents;
    }

    private List<Document> parseDocx(LyKb kb, LyKbFile kbFile, byte[] fileBytes) {
        List<Document> documents = new TikaDocumentReader(new ByteArrayResource(fileBytes)).get();
        List<Document> normalized = new ArrayList<>();
        for (Document document : documents) {
            Map<String, Object> metadata = new LinkedHashMap<>(document.getMetadata());
            metadata.putAll(baseMetadata(kb, kbFile, "docx", null));
            normalized.add(new Document(document.getText(), metadata));
        }
        return normalized;
    }

    private Map<String, Object> baseMetadata(LyKb kb, LyKbFile kbFile, String sourceType, Integer pageNo) {
        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("kbId", kb.getId());
        metadata.put("kbCode", kb.getKbCode());
        metadata.put("kbName", kb.getName());
        metadata.put("fileId", kbFile.getId());
        metadata.put("fileName", kbFile.getFileName());
        metadata.put("sourceType", sourceType);
        if (pageNo != null) {
            metadata.put("pageNo", pageNo);
        }
        return metadata;
    }

    private String normalizeType(String fileType, String fileName) {
        if (fileType != null && !fileType.isBlank()) {
            String normalized = fileType.toLowerCase();
            if ("text/plain".equals(normalized)) {
                return "txt";
            }
            if ("text/markdown".equals(normalized)) {
                return "md";
            }
            if ("application/pdf".equals(normalized)) {
                return "pdf";
            }
            if ("application/vnd.openxmlformats-officedocument.wordprocessingml.document".equals(normalized)) {
                return "docx";
            }
            if (normalized.contains("/")) {
                normalized = normalized.substring(normalized.lastIndexOf('/') + 1);
            }
            return normalized;
        }
        int index = fileName == null ? -1 : fileName.lastIndexOf('.');
        return index >= 0 ? fileName.substring(index + 1).toLowerCase() : "unknown";
    }
}

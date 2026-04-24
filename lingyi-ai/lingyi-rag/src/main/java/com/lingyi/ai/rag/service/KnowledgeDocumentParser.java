package com.lingyi.ai.rag.service;

import com.lingyi.ai.rag.entity.LyKb;
import com.lingyi.ai.rag.entity.LyKbFile;
import java.util.List;
import org.springframework.ai.document.Document;

public interface KnowledgeDocumentParser {

    List<Document> parse(LyKb kb, LyKbFile kbFile, byte[] fileBytes);
}

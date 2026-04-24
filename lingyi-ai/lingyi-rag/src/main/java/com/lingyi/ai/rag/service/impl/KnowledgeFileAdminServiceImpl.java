package com.lingyi.ai.rag.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lingyi.ai.rag.config.MinioProperties;
import com.lingyi.ai.rag.config.RagDefaultsProperties;
import com.lingyi.ai.rag.config.SpringAiQdrantProperties;
import com.lingyi.ai.rag.entity.LyKb;
import com.lingyi.ai.rag.entity.LyKbChunk;
import com.lingyi.ai.rag.entity.LyKbFile;
import com.lingyi.ai.rag.mapper.KnowledgeBaseMapper;
import com.lingyi.ai.rag.mapper.KnowledgeChunkMapper;
import com.lingyi.ai.rag.mapper.KnowledgeFileMapper;
import com.lingyi.ai.rag.service.KnowledgeDocumentParser;
import com.lingyi.ai.rag.service.KnowledgeFileAdminService;
import com.lingyi.ai.rag.vo.KnowledgeChunkVO;
import com.lingyi.ai.rag.vo.KnowledgeFileVO;
import com.lingyi.common.util.SnowflakeIdGenerator;
import com.lingyi.common.web.domain.ErrorCode;
import com.lingyi.common.web.exception.BizException;
import io.minio.BucketExistsArgs;
import io.minio.GetObjectArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.StatObjectArgs;
import io.qdrant.client.QdrantClient;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.qdrant.QdrantVectorStore;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class KnowledgeFileAdminServiceImpl implements KnowledgeFileAdminService {

    private final KnowledgeBaseMapper knowledgeBaseMapper;
    private final KnowledgeFileMapper knowledgeFileMapper;
    private final KnowledgeChunkMapper knowledgeChunkMapper;
    private final SnowflakeIdGenerator idGenerator;
    private final MinioClient minioClient;
    private final MinioProperties minioProperties;
    private final RagDefaultsProperties defaultsProperties;
    private final SpringAiQdrantProperties qdrantProperties;
    private final QdrantClient qdrantClient;
    private final EmbeddingModel embeddingModel;
    private final KnowledgeDocumentParser knowledgeDocumentParser;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public KnowledgeFileAdminServiceImpl(KnowledgeBaseMapper knowledgeBaseMapper,
                                         KnowledgeFileMapper knowledgeFileMapper,
                                         KnowledgeChunkMapper knowledgeChunkMapper,
                                         SnowflakeIdGenerator idGenerator,
                                         MinioClient minioClient,
                                         MinioProperties minioProperties,
                                         RagDefaultsProperties defaultsProperties,
                                         SpringAiQdrantProperties qdrantProperties,
                                         QdrantClient qdrantClient,
                                         EmbeddingModel embeddingModel,
                                         KnowledgeDocumentParser knowledgeDocumentParser) {
        this.knowledgeBaseMapper = knowledgeBaseMapper;
        this.knowledgeFileMapper = knowledgeFileMapper;
        this.knowledgeChunkMapper = knowledgeChunkMapper;
        this.idGenerator = idGenerator;
        this.minioClient = minioClient;
        this.minioProperties = minioProperties;
        this.defaultsProperties = defaultsProperties;
        this.qdrantProperties = qdrantProperties;
        this.qdrantClient = qdrantClient;
        this.embeddingModel = embeddingModel;
        this.knowledgeDocumentParser = knowledgeDocumentParser;
    }

    @Override
    public List<KnowledgeFileVO> files(Long kbId) {
        requireKb(kbId);
        return knowledgeFileMapper.selectList(new LambdaQueryWrapper<LyKbFile>()
                        .eq(LyKbFile::getKbId, kbId)
                        .eq(LyKbFile::getIsDeleted, 0)
                        .orderByDesc(LyKbFile::getCreatedAt))
                .stream()
                .map(this::toFileVO)
                .toList();
    }

    @Override
    @Transactional
    public KnowledgeFileVO upload(Long kbId, MultipartFile file) {
        requireKb(kbId);
        ensureMinioConfig();
        if (file == null || file.isEmpty()) {
            throw new BizException(ErrorCode.BAD_REQUEST, "file is required");
        }
        long fileId = idGenerator.nextId();
        String bucket = minioProperties.getBucket();
        String objectKey = "kb/" + kbId + "/" + fileId + "/" + file.getOriginalFilename();
        try {
            createBucketIfAbsent(bucket);
            byte[] bytes = file.getBytes();
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(bucket)
                    .object(objectKey)
                    .stream(new ByteArrayInputStream(bytes), bytes.length, -1)
                    .contentType(file.getContentType())
                    .build());
            String checksum = md5(bytes);
            String fileUrl = buildFileUrl(bucket, objectKey);
            String etag = minioClient.statObject(StatObjectArgs.builder().bucket(bucket).object(objectKey).build()).etag();

            LyKbFile kbFile = new LyKbFile();
            kbFile.setId(fileId);
            kbFile.setKbId(kbId);
            kbFile.setFileName(file.getOriginalFilename());
            kbFile.setBucket(bucket);
            kbFile.setObjectKey(objectKey);
            kbFile.setFileUrl(fileUrl);
            kbFile.setContentType(file.getContentType());
            kbFile.setEtag(etag);
            kbFile.setChecksum(checksum);
            kbFile.setFileType(detectFileType(file.getOriginalFilename(), file.getContentType()));
            kbFile.setFileSize(file.getSize());
            kbFile.setParseStatus(0);
            kbFile.setIndexStatus(0);
            kbFile.setFailureReason(null);
            kbFile.setIsDeleted(0);
            knowledgeFileMapper.insert(kbFile);
            return toFileVO(kbFile);
        } catch (Exception ex) {
            throw new BizException(ErrorCode.KB_FILE_UPLOAD_FAILED, ex.getMessage());
        }
    }

    @Override
    public KnowledgeFileVO detail(Long kbId, Long fileId) {
        return toFileVO(requireFile(kbId, fileId));
    }

    @Override
    @Transactional
    public void delete(Long kbId, Long fileId) {
        LyKbFile kbFile = requireFile(kbId, fileId);
        try {
            minioClient.removeObject(RemoveObjectArgs.builder()
                    .bucket(kbFile.getBucket())
                    .object(kbFile.getObjectKey())
                    .build());
        } catch (Exception ignored) {
        }
        List<String> vectorIds = knowledgeChunkMapper.selectList(new LambdaQueryWrapper<LyKbChunk>()
                        .eq(LyKbChunk::getFileId, fileId)
                        .eq(LyKbChunk::getIsDeleted, 0))
                .stream()
                .map(LyKbChunk::getVectorId)
                .filter(id -> id != null && !id.isBlank())
                .toList();
        if (!vectorIds.isEmpty()) {
            buildVectorStore(requireKb(kbId).getQdrantCollection()).delete(vectorIds);
        }
        knowledgeChunkMapper.delete(new LambdaQueryWrapper<LyKbChunk>().eq(LyKbChunk::getFileId, fileId));
        knowledgeFileMapper.deleteById(fileId);
    }

    @Override
    @Transactional
    public KnowledgeFileVO parse(Long kbId, Long fileId) {
        LyKb kb = requireKb(kbId);
        LyKbFile kbFile = requireFile(kbId, fileId);
        try {
            byte[] fileBytes = loadFileBytes(kbFile);
            List<Document> documents = knowledgeDocumentParser.parse(kb, kbFile, fileBytes);
            if (documents.isEmpty()) {
                throw new BizException(ErrorCode.KB_PARSE_FAILED, "file content is blank");
            }
            knowledgeChunkMapper.delete(new LambdaQueryWrapper<LyKbChunk>().eq(LyKbChunk::getFileId, fileId));
            for (int i = 0; i < documents.size(); i++) {
                Document document = documents.get(i);
                LyKbChunk chunk = new LyKbChunk();
                chunk.setId(idGenerator.nextId());
                chunk.setKbId(kbId);
                chunk.setFileId(fileId);
                chunk.setChunkIndex(i);
                chunk.setContent(document.getText());
                chunk.setTokenCount(Math.max(1, document.getText().length() / 4));
                chunk.setMetadataJson(writeMetadata(document.getMetadata()));
                chunk.setIsDeleted(0);
                knowledgeChunkMapper.insert(chunk);
            }
            kbFile.setParseStatus(1);
            kbFile.setIndexStatus(0);
            kbFile.setFailureReason(null);
            knowledgeFileMapper.updateById(kbFile);
            return toFileVO(kbFile);
        } catch (BizException ex) {
            kbFile.setParseStatus(2);
            kbFile.setFailureReason(ex.getMessage());
            knowledgeFileMapper.updateById(kbFile);
            throw ex;
        } catch (Exception ex) {
            kbFile.setParseStatus(2);
            kbFile.setFailureReason(ex.getMessage());
            knowledgeFileMapper.updateById(kbFile);
            throw new BizException(ErrorCode.KB_PARSE_FAILED, ex.getMessage());
        }
    }

    @Override
    @Transactional
    public KnowledgeFileVO index(Long kbId, Long fileId) {
        LyKb kb = requireKb(kbId);
        LyKbFile kbFile = requireFile(kbId, fileId);
        ensureVectorConfig();
        if (kb.getEmbeddingModel() != null && defaultsProperties.getEmbeddingModel() != null
                && !kb.getEmbeddingModel().equalsIgnoreCase(defaultsProperties.getEmbeddingModel())) {
            throw new BizException(ErrorCode.AI_CONFIG_MISSING,
                    "knowledge base embeddingModel must match spring.ai.dashscope.embedding.options.model");
        }
        List<LyKbChunk> chunks = knowledgeChunkMapper.selectList(new LambdaQueryWrapper<LyKbChunk>()
                .eq(LyKbChunk::getFileId, fileId)
                .eq(LyKbChunk::getIsDeleted, 0)
                .orderByAsc(LyKbChunk::getChunkIndex));
        if (chunks.isEmpty()) {
            throw new BizException(ErrorCode.KB_INDEX_FAILED, "parse chunks before index");
        }
        try {
            QdrantVectorStore vectorStore = buildVectorStore(kb.getQdrantCollection());
            List<Document> documents = new ArrayList<>();
            for (LyKbChunk chunk : chunks) {
                Map<String, Object> metadata = readMetadata(chunk.getMetadataJson());
                metadata.put("kbId", kbId);
                metadata.put("fileId", fileId);
                metadata.put("chunkId", chunk.getId());
                metadata.put("fileName", kbFile.getFileName());
                documents.add(Document.builder()
                        .id(String.valueOf(chunk.getId()))
                        .text(chunk.getContent())
                        .metadata(metadata)
                        .build());
                chunk.setVectorId(String.valueOf(chunk.getId()));
                knowledgeChunkMapper.updateById(chunk);
            }
            vectorStore.add(documents);
            kbFile.setIndexStatus(1);
            kbFile.setFailureReason(null);
            knowledgeFileMapper.updateById(kbFile);
            return toFileVO(kbFile);
        } catch (BizException ex) {
            kbFile.setIndexStatus(2);
            kbFile.setFailureReason(ex.getMessage());
            knowledgeFileMapper.updateById(kbFile);
            throw ex;
        } catch (Exception ex) {
            kbFile.setIndexStatus(2);
            kbFile.setFailureReason(ex.getMessage());
            knowledgeFileMapper.updateById(kbFile);
            throw new BizException(ErrorCode.KB_INDEX_FAILED, ex.getMessage());
        }
    }

    @Override
    public List<KnowledgeChunkVO> chunks(Long kbId, Long fileId) {
        requireFile(kbId, fileId);
        return knowledgeChunkMapper.selectList(new LambdaQueryWrapper<LyKbChunk>()
                        .eq(LyKbChunk::getKbId, kbId)
                        .eq(LyKbChunk::getFileId, fileId)
                        .eq(LyKbChunk::getIsDeleted, 0)
                        .orderByAsc(LyKbChunk::getChunkIndex))
                .stream()
                .map(this::toChunkVO)
                .toList();
    }

    private LyKb requireKb(Long kbId) {
        LyKb kb = knowledgeBaseMapper.selectById(kbId);
        if (kb == null || Integer.valueOf(1).equals(kb.getIsDeleted())) {
            throw new BizException(ErrorCode.RESOURCE_NOT_FOUND);
        }
        return kb;
    }

    private LyKbFile requireFile(Long kbId, Long fileId) {
        LyKbFile kbFile = knowledgeFileMapper.selectById(fileId);
        if (kbFile == null || Integer.valueOf(1).equals(kbFile.getIsDeleted()) || !kbId.equals(kbFile.getKbId())) {
            throw new BizException(ErrorCode.KB_FILE_NOT_FOUND);
        }
        return kbFile;
    }

    private void createBucketIfAbsent(String bucket) throws Exception {
        boolean exists = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucket).build());
        if (!exists) {
            minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
        }
    }

    private void ensureMinioConfig() {
        if (isBlank(minioProperties.getEndpoint())
                || isBlank(minioProperties.getAccessKey())
                || isBlank(minioProperties.getSecretKey())
                || isBlank(minioProperties.getBucket())) {
            throw new BizException(ErrorCode.AI_CONFIG_MISSING, "minio config is incomplete");
        }
    }

    private void ensureVectorConfig() {
        if (qdrantClient == null || embeddingModel == null) {
            throw new BizException(ErrorCode.AI_CONFIG_MISSING, "spring ai vector config is incomplete");
        }
        if (isBlank(qdrantProperties.getHost())
                || qdrantProperties.getPort() == null
                || isBlank(qdrantProperties.getApiKey())) {
            throw new BizException(ErrorCode.AI_CONFIG_MISSING, "qdrant config is incomplete");
        }
    }

    private byte[] loadFileBytes(LyKbFile kbFile) throws Exception {
        try (InputStream inputStream = minioClient.getObject(GetObjectArgs.builder()
                .bucket(kbFile.getBucket())
                .object(kbFile.getObjectKey())
                .build())) {
            return inputStream.readAllBytes();
        }
    }

    private String buildFileUrl(String bucket, String objectKey) {
        if (!isBlank(minioProperties.getPublicBaseUrl())) {
            return minioProperties.getPublicBaseUrl().replaceAll("/$", "") + "/" + objectKey;
        }
        return minioProperties.getEndpoint().replaceAll("/$", "") + "/" + bucket + "/" + objectKey;
    }

    private String detectFileType(String fileName, String contentType) {
        if (contentType != null && !contentType.isBlank()) {
            if ("text/plain".equalsIgnoreCase(contentType)) {
                return "txt";
            }
            if ("text/markdown".equalsIgnoreCase(contentType)) {
                return "md";
            }
            if ("application/pdf".equalsIgnoreCase(contentType)) {
                return "pdf";
            }
            if ("application/vnd.openxmlformats-officedocument.wordprocessingml.document".equalsIgnoreCase(contentType)) {
                return "docx";
            }
        }
        int index = fileName == null ? -1 : fileName.lastIndexOf('.');
        return index >= 0 ? fileName.substring(index + 1).toLowerCase() : "unknown";
    }

    private String writeMetadata(Map<String, Object> metadata) {
        try {
            return objectMapper.writeValueAsString(metadata == null ? new HashMap<>() : metadata);
        } catch (JsonProcessingException ex) {
            return "{}";
        }
    }

    private Map<String, Object> readMetadata(String metadataJson) {
        if (metadataJson == null || metadataJson.isBlank()) {
            return new LinkedHashMap<>();
        }
        try {
            return objectMapper.readValue(metadataJson, new TypeReference<>() {
            });
        } catch (Exception ex) {
            return new LinkedHashMap<>();
        }
    }

    private String md5(byte[] bytes) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("MD5");
        byte[] md5 = digest.digest(bytes);
        StringBuilder builder = new StringBuilder();
        for (byte b : md5) {
            builder.append(String.format("%02x", b));
        }
        return builder.toString();
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private QdrantVectorStore buildVectorStore(String collectionName) {
        return QdrantVectorStore.builder(qdrantClient, embeddingModel)
                .collectionName(collectionName)
                .initializeSchema(Boolean.TRUE.equals(qdrantProperties.getInitializeSchema()))
                .build();
    }

    private KnowledgeFileVO toFileVO(LyKbFile file) {
        KnowledgeFileVO vo = new KnowledgeFileVO();
        vo.setId(file.getId());
        vo.setKbId(file.getKbId());
        vo.setFileName(file.getFileName());
        vo.setFileUrl(file.getFileUrl());
        vo.setFileType(file.getFileType());
        vo.setFileSize(file.getFileSize());
        vo.setParseStatus(file.getParseStatus());
        vo.setIndexStatus(file.getIndexStatus());
        vo.setFailureReason(file.getFailureReason());
        vo.setCreatedAt(file.getCreatedAt());
        return vo;
    }

    private KnowledgeChunkVO toChunkVO(LyKbChunk chunk) {
        Map<String, Object> metadata = readMetadata(chunk.getMetadataJson());
        KnowledgeChunkVO vo = new KnowledgeChunkVO();
        vo.setId(chunk.getId());
        vo.setKbId(chunk.getKbId());
        vo.setFileId(chunk.getFileId());
        vo.setChunkIndex(chunk.getChunkIndex());
        vo.setContent(chunk.getContent());
        vo.setVectorId(chunk.getVectorId());
        vo.setTokenCount(chunk.getTokenCount());
        vo.setMetadataJson(chunk.getMetadataJson());
        Object pageNo = metadata.get("pageNo");
        vo.setPageNo(pageNo instanceof Number ? ((Number) pageNo).intValue() : null);
        Object sourceType = metadata.get("sourceType");
        vo.setSourceType(sourceType == null ? null : String.valueOf(sourceType));
        return vo;
    }
}

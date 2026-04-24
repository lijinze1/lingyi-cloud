package com.lingyi.common.web.domain;

public enum ErrorCode {

    SUCCESS(0, "success"),
    BAD_REQUEST(400, "bad request"),
    RESOURCE_NOT_FOUND(404, "resource not found"),
    INTERNAL_ERROR(500, "internal error"),
    AI_CONFIG_MISSING(20001, "ai config missing"),
    PROMPT_CATEGORY_HAS_CHILDREN(21001, "prompt category has child categories"),
    PROMPT_CATEGORY_HAS_PROMPTS(21002, "prompt category has prompts"),
    PROMPT_CODE_ALREADY_EXISTS(21003, "prompt code already exists"),
    PROMPT_VERSION_ALREADY_PUBLISHED(21004, "prompt version already published"),
    PROMPT_PUBLISHED_VERSION_NOT_FOUND(21005, "published prompt version not found"),
    KB_NAME_ALREADY_EXISTS(22001, "knowledge base name already exists"),
    KB_FILE_NOT_FOUND(22002, "knowledge file not found"),
    KB_FILE_UPLOAD_FAILED(22003, "knowledge file upload failed"),
    KB_PARSE_FAILED(22004, "knowledge file parse failed"),
    KB_INDEX_FAILED(22005, "knowledge file index failed"),
    KB_FILE_TYPE_UNSUPPORTED(22006, "knowledge file type unsupported"),
    CHAT_SESSION_NOT_FOUND(23001, "chat session not found");

    private final int code;
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}

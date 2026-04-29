package com.lingyi.common.web.domain;

public enum ErrorCode {
    SUCCESS("00000", "success"),
    BAD_REQUEST("A0400", "bad request"),
    UNAUTHORIZED("A0401", "login required"),
    FORBIDDEN("A0403", "forbidden"),
    USER_ALREADY_EXISTS("A0409", "user already exists"),
    USERNAME_OR_PASSWORD_INVALID("A0410", "username or password invalid"),
    CAPTCHA_INVALID("A0411", "captcha invalid"),
    CAPTCHA_EXPIRED("A0412", "captcha expired"),
    PHONE_FORMAT_INVALID("A0413", "phone format invalid"),
    SMS_CODE_INVALID("A0414", "sms code invalid"),
    SMS_CODE_EXPIRED("A0415", "sms code expired"),
    SMS_CODE_SEND_TOO_FREQUENT("A0416", "sms code sent too frequently"),
    SMS_SCENE_INVALID("A0417", "sms scene invalid"),
    PHONE_ALREADY_EXISTS("A0418", "phone already exists"),
    PHONE_NOT_REGISTERED("A0419", "phone not registered"),
    USER_DISABLED("A0420", "user disabled"),
    ROLE_CODE_ALREADY_EXISTS("A0421", "role code already exists"),
    PERMISSION_CODE_ALREADY_EXISTS("A0422", "permission code already exists"),
    SESSION_INVALID("A0423", "session invalid"),
    RESOURCE_NOT_FOUND("A0424", "resource not found"),
    INTERNAL_ERROR("B0001", "internal error"),
    CAPTCHA_GENERATE_FAILED("B0002", "captcha generate failed"),
    AI_CONFIG_MISSING("B2001", "ai config missing"),
    PROMPT_CATEGORY_HAS_CHILDREN("B2101", "prompt category has child categories"),
    PROMPT_CATEGORY_HAS_PROMPTS("B2102", "prompt category has prompts"),
    PROMPT_CODE_ALREADY_EXISTS("B2103", "prompt code already exists"),
    PROMPT_PUBLISHED_VERSION_NOT_FOUND("B2104", "published prompt version not found"),
    KB_NAME_ALREADY_EXISTS("B2201", "knowledge base name already exists"),
    KB_FILE_NOT_FOUND("B2202", "knowledge file not found"),
    KB_FILE_UPLOAD_FAILED("B2203", "knowledge file upload failed"),
    KB_PARSE_FAILED("B2204", "knowledge file parse failed"),
    KB_INDEX_FAILED("B2205", "knowledge file index failed"),
    KB_FILE_TYPE_UNSUPPORTED("B2206", "knowledge file type unsupported"),
    CHAT_SESSION_NOT_FOUND("B2301", "chat session not found");

    private final String code;
    private final String message;

    ErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}

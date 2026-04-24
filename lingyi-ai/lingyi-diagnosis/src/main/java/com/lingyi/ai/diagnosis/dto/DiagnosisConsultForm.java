package com.lingyi.ai.diagnosis.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class DiagnosisConsultForm {

    private Long sessionId;

    @NotNull
    private Long userId;

    @NotBlank
    private String question;

    private MultipartFile[] images;
}

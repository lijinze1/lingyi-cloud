package com.lingyi.ai.diagnosis.controller;

import com.lingyi.ai.diagnosis.dto.DiagnosisConsultForm;
import com.lingyi.ai.diagnosis.service.DiagnosisService;
import com.lingyi.ai.diagnosis.vo.DiagnosisReplyVO;
import com.lingyi.common.web.domain.Result;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/ai/diagnosis")
public class DiagnosisController {

    private final DiagnosisService diagnosisService;

    public DiagnosisController(DiagnosisService diagnosisService) {
        this.diagnosisService = diagnosisService;
    }

    @PostMapping(value = "/consultations", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Result<DiagnosisReplyVO> consult(@Valid @ModelAttribute DiagnosisConsultForm form) {
        return Result.success(diagnosisService.consult(form));
    }
}

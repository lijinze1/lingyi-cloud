package com.lingyi.ai.diagnosis.service;

import com.lingyi.ai.diagnosis.dto.DiagnosisConsultForm;
import com.lingyi.ai.diagnosis.vo.DiagnosisReplyVO;

public interface DiagnosisService {

    DiagnosisReplyVO consult(DiagnosisConsultForm form);
}

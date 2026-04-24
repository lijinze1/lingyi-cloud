package com.lingyi.ai.diagnosis.vo;

import java.util.List;
import lombok.Data;

@Data
public class DiagnosisReplyVO {

    private Long sessionId;
    private String answer;
    private List<String> imageUrls;
}

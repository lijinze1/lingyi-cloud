package com.lingyi.ai.prompt.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class PageQueryDTO {

    @Min(1)
    private long current = 1;

    @Min(1)
    @Max(100)
    private long size = 10;

    private String keyword;
}

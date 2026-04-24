package com.lingyi.ai.prompt.vo;

import java.util.List;
import lombok.Data;

@Data
public class PromptDetailVO extends PromptVO {

    private List<PromptVersionVO> versions;
}

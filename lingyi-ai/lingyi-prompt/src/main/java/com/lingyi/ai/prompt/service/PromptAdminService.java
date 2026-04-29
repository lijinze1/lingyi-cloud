package com.lingyi.ai.prompt.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.lingyi.ai.prompt.dto.PageQueryDTO;
import com.lingyi.ai.prompt.dto.PromptSaveRequest;
import com.lingyi.ai.prompt.dto.PromptVersionCreateRequest;
import com.lingyi.ai.prompt.vo.PromptDetailVO;
import com.lingyi.ai.prompt.vo.PromptVO;
import com.lingyi.ai.prompt.vo.PromptVersionVO;
import java.util.List;

public interface PromptAdminService {

    IPage<PromptVO> page(PageQueryDTO queryDTO, Long categoryId);

    PromptDetailVO detail(Long promptId);

    PromptVO create(PromptSaveRequest request);

    PromptVO update(Long promptId, PromptSaveRequest request);

    void delete(Long promptId);

    List<PromptVersionVO> versions(Long promptId);

    PromptVersionVO createVersion(Long promptId, PromptVersionCreateRequest request);

    void deleteVersion(Long promptId, Long versionId);

    PromptVersionVO publish(Long promptId, Long versionId);

    PromptVersionVO rollback(Long promptId, Long versionId);
}

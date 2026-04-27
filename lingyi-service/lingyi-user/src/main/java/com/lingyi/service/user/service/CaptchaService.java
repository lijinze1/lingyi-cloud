package com.lingyi.service.user.service;

import com.lingyi.service.user.vo.CaptchaVO;

public interface CaptchaService {

    CaptchaVO generate();

    void verifyAndConsume(String captchaId, String captchaCode);
}

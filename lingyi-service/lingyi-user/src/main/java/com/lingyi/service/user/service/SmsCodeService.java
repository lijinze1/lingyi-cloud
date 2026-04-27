package com.lingyi.service.user.service;

import com.lingyi.service.user.vo.SmsCodeSendVO;

public interface SmsCodeService {

    SmsCodeSendVO send(String phone, String scene);

    void verifyAndConsume(String phone, String scene, String smsCode);

    String normalizePhone(String phone);
}

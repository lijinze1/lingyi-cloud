package com.lingyi.service.user.dto;

import lombok.Data;

@Data

public class UserPageQueryDTO {

    private long current = 1;
    private long size = 10;
    private String keyword;

}


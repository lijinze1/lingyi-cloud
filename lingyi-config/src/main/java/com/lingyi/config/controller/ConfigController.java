package com.lingyi.config.controller;

import com.lingyi.common.web.domain.Result;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/config")
public class ConfigController {

    @GetMapping("/ping")
    public Result<Map<String, Object>> ping() {
        return Result.success(Map.of(
                "service", "lingyi-config",
                "status", "UP"
        ));
    }
}


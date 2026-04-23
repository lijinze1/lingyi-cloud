package com.lingyi.service.user.service;

import com.lingyi.service.user.repository.UserAuthRepository;
import com.lingyi.service.user.util.SnowflakeIdGenerator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class AdminBootstrapService implements ApplicationRunner {

    private static final long DEFAULT_ADMIN_USER_ID = 1000000000001L;
    private static final long DEFAULT_ADMIN_AUTH_ID = 1000000000101L;
    private static final long DEFAULT_ADMIN_ROLE_ID = 1000000000201L;

    private final UserAuthRepository userAuthRepository;
    private final SnowflakeIdGenerator idGenerator;
    private final BCryptPasswordEncoder passwordEncoder;

    @Value("${lingyi.bootstrap.admin.username:admin}")
    private String adminUsername;

    @Value("${lingyi.bootstrap.admin.password:password}")
    private String adminPassword;

    @Value("${lingyi.bootstrap.admin.nickname:系统管理员}")
    private String adminNickname;

    public AdminBootstrapService(UserAuthRepository userAuthRepository, SnowflakeIdGenerator idGenerator) {
        this.userAuthRepository = userAuthRepository;
        this.idGenerator = idGenerator;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    @Override
    public void run(org.springframework.boot.ApplicationArguments args) {
        Long userId = userAuthRepository.findUserIdByUsername(adminUsername)
                .orElseGet(() -> {
                    userAuthRepository.insertUser(DEFAULT_ADMIN_USER_ID, adminUsername, adminNickname);
                    return DEFAULT_ADMIN_USER_ID;
                });

        String encryptedPassword = passwordEncoder.encode(adminPassword);
        int updated = userAuthRepository.updatePasswordAuth(userId, adminUsername, encryptedPassword);
        if (updated == 0) {
            userAuthRepository.insertPasswordAuth(DEFAULT_ADMIN_AUTH_ID, userId, adminUsername, encryptedPassword);
        }

        Long adminRoleId = userAuthRepository.findRoleIdByCode("ROLE_ADMIN")
                .orElseGet(() -> {
                    userAuthRepository.insertRole(DEFAULT_ADMIN_ROLE_ID, "ROLE_ADMIN", "管理员");
                    return DEFAULT_ADMIN_ROLE_ID;
                });

        if (!userAuthRepository.existsUserRole(userId, adminRoleId)) {
            userAuthRepository.insertUserRole(idGenerator.nextId(), userId, adminRoleId);
        }
    }
}

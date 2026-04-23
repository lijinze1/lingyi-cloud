package com.lingyi.service.user.repository;

import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class UserAuthRepository {

    private final JdbcTemplate jdbcTemplate;

    public UserAuthRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public boolean existsUsername(String username) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(1) FROM ly_user WHERE username = ? AND is_deleted = 0",
                Integer.class,
                username
        );
        return count != null && count > 0;
    }

    public void insertUser(Long id, String username, String nickname) {
        jdbcTemplate.update(
                "INSERT INTO ly_user (id, username, nickname, status, created_at, updated_at, is_deleted) VALUES (?, ?, ?, 1, NOW(), NOW(), 0)",
                id, username, nickname
        );
    }

    public void insertPasswordAuth(Long id, Long userId, String username, String encryptedPassword) {
        jdbcTemplate.update(
                "INSERT INTO ly_user_auth (id, user_id, auth_type, auth_key, auth_secret, status, created_at, updated_at, is_deleted) VALUES (?, ?, 'PASSWORD', ?, ?, 1, NOW(), NOW(), 0)",
                id, userId, username, encryptedPassword
        );
    }

    public Optional<LoginUserRow> findPasswordUser(String username) {
        return jdbcTemplate.query(
                "SELECT u.id AS user_id, u.username, u.nickname, ua.auth_secret FROM ly_user u "
                        + "JOIN ly_user_auth ua ON u.id = ua.user_id "
                        + "WHERE u.username = ? AND u.status = 1 AND u.is_deleted = 0 "
                        + "AND ua.auth_type = 'PASSWORD' AND ua.status = 1 AND ua.is_deleted = 0",
                rs -> rs.next()
                        ? Optional.of(new LoginUserRow(
                        rs.getLong("user_id"),
                        rs.getString("username"),
                        rs.getString("nickname"),
                        rs.getString("auth_secret")))
                        : Optional.empty(),
                username
        );
    }

    public Optional<UserProfileRow> findUserById(Long userId) {
        return jdbcTemplate.query(
                "SELECT id, username, nickname FROM ly_user WHERE id = ? AND status = 1 AND is_deleted = 0",
                rs -> rs.next()
                        ? Optional.of(new UserProfileRow(
                        rs.getLong("id"),
                        rs.getString("username"),
                        rs.getString("nickname")))
                        : Optional.empty(),
                userId
        );
    }

    public void updateLastLoginAt(Long userId) {
        jdbcTemplate.update(
                "UPDATE ly_user SET last_login_at = ?, updated_at = NOW() WHERE id = ?",
                LocalDateTime.now(),
                userId
        );
    }

    public record LoginUserRow(Long userId, String username, String nickname, String encryptedPassword) {
    }

    public record UserProfileRow(Long userId, String username, String nickname) {
    }
}

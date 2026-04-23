package com.lingyi.service.user.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "lingyi.bootstrap")
public class BootstrapProperties {

    private boolean enabled = true;
    private Account admin = new Account();
    private Account user = new Account();
    private Rbac rbac = new Rbac();

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Account getAdmin() {
        return admin;
    }

    public void setAdmin(Account admin) {
        this.admin = admin;
    }

    public Account getUser() {
        return user;
    }

    public void setUser(Account user) {
        this.user = user;
    }

    public Rbac getRbac() {
        return rbac;
    }

    public void setRbac(Rbac rbac) {
        this.rbac = rbac;
    }

    public static class Account {
        private String username;
        private String password;
        private String nickname;

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public String getNickname() {
            return nickname;
        }

        public void setNickname(String nickname) {
            this.nickname = nickname;
        }
    }

    public static class Rbac {
        private boolean seed = true;

        public boolean isSeed() {
            return seed;
        }

        public void setSeed(boolean seed) {
            this.seed = seed;
        }
    }
}

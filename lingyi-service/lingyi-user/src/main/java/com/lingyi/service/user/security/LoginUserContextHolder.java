package com.lingyi.service.user.security;

public final class LoginUserContextHolder {

    private static final ThreadLocal<LoginUserContext> HOLDER = new ThreadLocal<>();

    private LoginUserContextHolder() {
    }

    public static void set(LoginUserContext context) {
        HOLDER.set(context);
    }

    public static LoginUserContext get() {
        return HOLDER.get();
    }

    public static LoginUserContext require() {
        LoginUserContext context = HOLDER.get();
        if (context == null) {
            throw new IllegalStateException("Login context not initialized");
        }
        return context;
    }

    public static void clear() {
        HOLDER.remove();
    }
}

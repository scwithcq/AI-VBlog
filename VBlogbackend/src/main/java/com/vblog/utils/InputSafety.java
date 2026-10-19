package com.vblog.utils;

import java.net.URI;
import java.nio.charset.StandardCharsets;

/** Shared validation for untrusted profile and authentication input. */
public final class InputSafety {
    private InputSafety() {}

    private static int classes(String value) {
        return (value.chars().anyMatch(Character::isLetter) ? 1 : 0)
                + (value.chars().anyMatch(Character::isDigit) ? 1 : 0)
                + (value.chars().anyMatch(c -> !Character.isLetterOrDigit(c)) ? 1 : 0);
    }

    public static com.vblog.entity.User publicUser(com.vblog.entity.User source) {
        var copy = new com.vblog.entity.User();
        org.springframework.beans.BeanUtils.copyProperties(source, copy);
        copy.setPassword(null); copy.setPhone(null); copy.setEmail(null);
        return copy;
    }

    public static void password(String value) {
        if (value == null || value.length() < 10 || value.getBytes(StandardCharsets.UTF_8).length > 72
                || classes(value) < 2) {
            throw new IllegalArgumentException("密码需至少10位，包含至少两类字符，且不超过72字节");
        }
    }

    public static void length(String value, int max) {
        if (value != null && value.length() > max) throw new IllegalArgumentException("输入内容过长");
    }

    public static void website(String value) {
        if (value == null || value.isBlank()) return;
        length(value, 200);
        try {
            URI uri = URI.create(value);
            if (!("https".equalsIgnoreCase(uri.getScheme()) || "http".equalsIgnoreCase(uri.getScheme()))
                    || uri.getHost() == null || uri.getUserInfo() != null) throw new IllegalArgumentException();
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("个人网站仅支持有效的 http/https 链接");
        }
    }

    public static void media(String value) {
        if (value == null || value.isBlank()) return;
        if (!value.matches("/(?:api/)?uploads/(?:images|videos)/[a-zA-Z0-9_-]+\\.(?:jpg|jpeg|png|gif|webp|mp4|mov|avi|wmv|flv|webm)")
                && !value.equals("/default-avatar.jpg")
                && !value.matches("/avatars/[a-zA-Z0-9_-]+\\.(?:png|jpg|svg|webp)")) {
            throw new IllegalArgumentException("请使用本站上传的头像或背景图片");
        }
    }
}

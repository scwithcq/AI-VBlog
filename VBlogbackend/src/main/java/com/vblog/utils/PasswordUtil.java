package com.vblog.utils;

import cn.hutool.crypto.digest.BCrypt;
import org.springframework.stereotype.Component;

/**
 * Password Utility Class
 * Handle password encryption and verification using BCrypt
 * 
 * @author V-Blog Team
 */
@Component
public class PasswordUtil {

    /**
     * Encrypt password using BCrypt
     * 
     * @param plainPassword Plain text password
     * @return Encrypted password
     */
    public String encrypt(String plainPassword) {
        return BCrypt.hashpw(plainPassword);
    }

    /**
     * Verify password
     * 
     * @param plainPassword Plain text password
     * @param hashedPassword Hashed password from database
     * @return true if password matches, false otherwise
     */
    public boolean verify(String plainPassword, String hashedPassword) {
        return BCrypt.checkpw(plainPassword, hashedPassword);
    }
}


package com.vblog.service;

import com.vblog.entity.User;

/**
 * User Service Interface
 * 
 * @author V-Blog Team
 */
public interface UserService {

    /**
     * User registration
     * 
     * @param username Username
     * @param password Plain text password
     * @return Registered user
     */
    User register(String username, String password);

    /**
     * User login
     * 
     * @param username Username
     * @param password Plain text password
     * @return User info if login successful, null otherwise
     */
    User login(String username, String password);

    /**
     * Get user info by user ID
     * 
     * @param userId User ID
     * @return User info
     */
    User getUserById(Long userId);

    /**
     * Get user info by username
     * 
     * @param username Username
     * @return User info
     */
    User getUserByUsername(String username);

    /**
     * Update user info
     * 
     * @param user User entity with updated info
     * @return Updated user
     */
    User updateUserInfo(User user);

    /**
     * Change user password
     * 
     * @param userId      User ID
     * @param oldPassword Old password
     * @param newPassword New password
     * @return True if password changed successfully, false otherwise
     */
    boolean changePassword(Long userId, String oldPassword, String newPassword);

    /**
     * Update user avatar
     * 
     * @param userId    User ID
     * @param avatarUrl Avatar URL
     * @return Updated user
     */
    User updateAvatar(Long userId, String avatarUrl);

    /**
     * Update user background image
     * 
     * @param userId        User ID
     * @param backgroundUrl Background image URL
     * @return Updated user
     */
    User updateBackground(Long userId, String backgroundUrl);

    /**
     * Get user statistics
     * 
     * @param userId User ID
     * @return Statistics object containing followerCount, followingCount, postCount
     */
    UserStats getUserStats(Long userId);

    /**
     * Get all users
     * 
     * @return List of all users
     */
    java.util.List<User> getAllUsers();

    /**
     * User statistics DTO
     */
    record UserStats(Long followerCount, Long followingCount, Long postCount) {
    }
}

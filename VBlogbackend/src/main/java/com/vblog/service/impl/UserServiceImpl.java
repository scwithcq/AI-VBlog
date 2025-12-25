package com.vblog.service.impl;

import com.vblog.entity.User;
import com.vblog.mapper.UserMapper;
import com.vblog.service.FollowService;
import com.vblog.service.PostService;
import com.vblog.service.UserService;
import com.vblog.utils.PasswordUtil;
import com.vblog.utils.XssUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 用户服务实现类
 * 
 * @author V-Blog团队
 */
@Slf4j
@Service
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final PasswordUtil passwordUtil;
    private final XssUtil xssUtil;
    private FollowService followService;
    private PostService postService;

    public UserServiceImpl(UserMapper userMapper, PasswordUtil passwordUtil, XssUtil xssUtil) {
        this.userMapper = userMapper;
        this.passwordUtil = passwordUtil;
        this.xssUtil = xssUtil;
    }

    @Autowired
    @Lazy
    public void setFollowService(FollowService followService) {
        this.followService = followService;
    }

    @Autowired
    @Lazy
    public void setPostService(PostService postService) {
        this.postService = postService;
    }

    @Override
    @Transactional
    public User register(String username, String password) {
        // 检查用户名是否已存在
        User existingUser = userMapper.selectByUsername(username);
        if (existingUser != null) {
            throw new RuntimeException("用户名已存在");
        }

        // 过滤XSS
        username = xssUtil.filter(username);

        // 创建新用户
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordUtil.encrypt(password));
        user.setAvatar("/uploads/default-avatar.png"); // 默认头像
        user.setBio("");

        userMapper.insert(user);
        log.info("User registered: {}", username);

        // 返回前清除密码
        user.setPassword(null);
        return user;
    }

    @Override
    public User login(String username, String password) {
        // 根据用户名查找用户
        User user = userMapper.selectByUsername(username);

        if (user == null) {
            throw new RuntimeException("用户名或密码错误");
        }

        // 验证密码
        if (!passwordUtil.verify(password, user.getPassword())) {
            throw new RuntimeException("用户名或密码错误");
        }

        // 返回前清除密码
        user.setPassword(null);
        log.info("User logged in: {}", username);
        return user;
    }

    @Override
    public User getUserById(Long userId) {
        User user = userMapper.selectById(userId);
        if (user != null) {
            user.setPassword(null); // 永远不返回密码
        }
        return user;
    }

    @Override
    public User getUserByUsername(String username) {
        User user = userMapper.selectByUsername(username);
        if (user != null) {
            user.setPassword(null); // 永远不返回密码
        }
        return user;
    }

    @Override
    @Transactional
    public User updateUserInfo(User user) {
        // 过滤个人简介中的XSS
        if (user.getBio() != null) {
            user.setBio(xssUtil.filter(user.getBio()));
        }

        // 不要在这里更新密码（应该有单独的密码更新接口）
        user.setPassword(null);
        userMapper.update(user);
        log.info("User info updated: {}", user.getUserId());

        return getUserById(user.getUserId());
    }

    @Override
    public UserStats getUserStats(Long userId) {
        Long followerCount = followService.getFollowerCount(userId);
        Long followingCount = followService.getFollowingCount(userId);

        // 获取帖子数量（使用getUserPosts获取总数）
        Long postCount = postService.getUserPosts(userId, 1, 1).getTotal();

        return new UserStats(followerCount, followingCount, postCount);
    }

    @Override
    public boolean changePassword(Long userId, String oldPassword, String newPassword) {
        // 获取用户信息
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        // 验证旧密码
        if (!passwordUtil.verify(oldPassword, user.getPassword())) {
            throw new RuntimeException("旧密码错误");
        }

        // 更新密码
        user.setPassword(passwordUtil.encrypt(newPassword));
        userMapper.update(user);
        log.info("User password changed: {}", userId);
        return true;
    }

    @Override
    public User updateAvatar(Long userId, String avatarUrl) {
        User updateUser = new User();
        updateUser.setUserId(userId);
        updateUser.setAvatar(avatarUrl);
        userMapper.update(updateUser);
        log.info("User avatar updated: {}", userId);
        return getUserById(userId);
    }

    @Override
    public User updateBackground(Long userId, String backgroundUrl) {
        User updateUser = new User();
        updateUser.setUserId(userId);
        updateUser.setBackground(backgroundUrl);
        userMapper.update(updateUser);
        log.info("User background updated: {}", userId);
        return getUserById(userId);
    }

    @Override
    public List<User> getAllUsers() {
        List<User> users = userMapper.selectAll();
        // 清除所有用户的密码信息
        users.forEach(user -> user.setPassword(null));
        log.info("Retrieved all users, count: {}", users.size());
        return users;
    }
}

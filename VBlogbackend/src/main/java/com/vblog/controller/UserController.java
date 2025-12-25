package com.vblog.controller;

import com.vblog.common.Result;
import com.vblog.entity.User;
import com.vblog.service.UserService;
import com.vblog.utils.JwtUtil;
import com.vblog.utils.FileUploadUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * 用户控制器
 * 
 * @author V-Blog团队
 */
@Slf4j
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
@Validated
public class UserController extends BaseController {

    private final UserService userService;
    private final JwtUtil jwtUtil;
    private final FileUploadUtil fileUploadUtil;

    /**
     * 用户注册
     */
    @PostMapping("/register")
    public Result<Map<String, Object>> register(
            @RequestParam @NotBlank(message = "用户名不能为空") String username,
            @RequestParam @NotBlank(message = "密码不能为空") String password) {

        User user = userService.register(username, password);

        // 生成JWT令牌
        String token = jwtUtil.generateToken(user.getUserId(), user.getUsername());

        Map<String, Object> result = new HashMap<>();
        result.put("user", user);
        result.put("token", token);

        return Result.success("注册成功", result);
    }

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public Result<Map<String, Object>> login(
            @RequestParam @NotBlank(message = "用户名不能为空") String username,
            @RequestParam @NotBlank(message = "密码不能为空") String password) {

        User user = userService.login(username, password);

        // 生成JWT令牌
        String token = jwtUtil.generateToken(user.getUserId(), user.getUsername());

        Map<String, Object> result = new HashMap<>();
        result.put("user", user);
        result.put("token", token);

        return Result.success("登录成功", result);
    }

    /**
     * 获取当前用户信息（需要认证）
     */
    @GetMapping("/info")
    public Result<User> getCurrentUserInfo(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        User user = userService.getUserById(userId);
        return Result.success(user);
    }

    /**
     * 更新用户信息（需要认证）
     */
    @PutMapping("/info")
    public Result<User> updateUserInfo(
            @RequestBody User user,
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        user.setUserId(userId);
        User updatedUser = userService.updateUserInfo(user);
        return Result.success("更新成功", updatedUser);
    }

    /**
     * 根据ID获取用户信息
     */
    @GetMapping("/{userId}")
    public Result<User> getUserInfo(@PathVariable Long userId) {
        User user = userService.getUserById(userId);
        if (user == null) {
            return Result.error(404, "用户不存在");
        }
        return Result.success(user);
    }

    /**
     * 获取用户统计信息
     */
    @GetMapping("/{userId}/stats")
    public Result<UserService.UserStats> getUserStats(@PathVariable Long userId) {
        UserService.UserStats stats = userService.getUserStats(userId);
        return Result.success(stats);
    }

    /**
     * 修改密码
     */
    @PutMapping("/password")
    public Result<?> changePassword(
            @RequestParam @NotBlank(message = "旧密码不能为空") String oldPassword,
            @RequestParam @NotBlank(message = "新密码不能为空") String newPassword,
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        userService.changePassword(userId, oldPassword, newPassword);
        return Result.success("密码修改成功");
    }

    /**
     * 上传用户头像
     */
    @PostMapping("/avatar")
    public Result<User> uploadAvatar(
            @RequestParam("file") MultipartFile file,
            HttpServletRequest request,
            @Autowired FileUploadUtil fileUploadUtil) throws IOException {
        Long userId = (Long) request.getAttribute("userId");
        String url = fileUploadUtil.uploadImage(file);
        User user = userService.updateAvatar(userId, url);
        return Result.success("头像上传成功", user);
    }

    /**
     * 上传用户背景图片
     */
    @PostMapping("/background")
    public Result<User> uploadBackground(
            @RequestParam("file") MultipartFile file,
            HttpServletRequest request,
            @Autowired FileUploadUtil fileUploadUtil) throws IOException {
        Long userId = (Long) request.getAttribute("userId");
        String url = fileUploadUtil.uploadImage(file);
        User user = userService.updateBackground(userId, url);
        return Result.success("背景图片上传成功", user);
    }

    /**
     * 获取所有用户
     */
    @GetMapping("/all")
    public Result<java.util.List<User>> getAllUsers() {
        java.util.List<User> users = userService.getAllUsers();
        return Result.success(users);
    }

}

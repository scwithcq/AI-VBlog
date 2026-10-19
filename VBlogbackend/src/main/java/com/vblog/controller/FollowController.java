package com.vblog.controller;

import com.vblog.common.Page;
import com.vblog.common.Result;
import com.vblog.entity.User;
import com.vblog.service.FollowService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Follow Controller
 *
 * @author V-Blog Team
 */
@Slf4j
@RestController
@RequestMapping("/follow")
@RequiredArgsConstructor
@Validated
public class FollowController extends BaseController {

    private final FollowService followService;

    /**
     * Follow a user
     */
    @PostMapping("/{userId}")
    public Result<Map<String, Object>> followUser(
            @PathVariable @NotNull Long userId,
            HttpServletRequest request) {

        Long followerId = (Long) request.getAttribute("userId");

        boolean followed = followService.followUser(followerId, userId);
        Long followerCount = followService.getFollowerCount(userId);

        Map<String, Object> result = new HashMap<>();
        result.put("followed", followed);
        result.put("followerCount", followerCount);

        if (!followed) {
            return Result.error(40001, "已经关注过该用户");
        }

        return Result.success("关注成功", result);
    }

    /**
     * Unfollow a user
     */
    @DeleteMapping("/{userId}")
    public Result<Map<String, Object>> unfollowUser(
            @PathVariable @NotNull Long userId,
            HttpServletRequest request) {

        Long followerId = (Long) request.getAttribute("userId");

        boolean unfollowed = followService.unfollowUser(followerId, userId);
        Long followerCount = followService.getFollowerCount(userId);

        Map<String, Object> result = new HashMap<>();
        result.put("unfollowed", unfollowed);
        result.put("followerCount", followerCount);

        if (!unfollowed) {
            return Result.error(40001, followService.isMutualFriend(followerId, userId)
                    ? "好友默认互相关注，请先解除好友关系" : "没有关注该用户");
        }

        return Result.success("取消关注成功", result);
    }

    /**
     * Check if current user is following a user
     */
    @GetMapping("/{userId}/status")
    public Result<Map<String, Object>> getFollowStatus(
            @PathVariable @NotNull Long userId,
            HttpServletRequest request) {

        Long followerId = (Long) request.getAttribute("userId");
        boolean isFollowing = followService.isFollowing(followerId, userId);

        Map<String, Object> result = new HashMap<>();
        result.put("isFollowing", isFollowing);
        result.put("isFriend", followService.isMutualFriend(followerId, userId));

        return Result.success(result);
    }

    /**
     * Get followers list
     */
    @GetMapping("/{userId}/followers")
    public Result<Page<User>> getFollowers(
            @PathVariable @NotNull Long userId,
            @RequestParam(defaultValue = "1") @Min(1) @jakarta.validation.constraints.Max(100000) int page,
            @RequestParam(defaultValue = "10") @Min(1) @jakarta.validation.constraints.Max(100) int size) {

        Page<User> followers = followService.getFollowers(userId, page, size);
        return Result.success(followers);
    }

    /**
     * Get following list
     */
    @GetMapping("/{userId}/following")
    public Result<Page<User>> getFollowing(
            @PathVariable @NotNull Long userId,
            @RequestParam(defaultValue = "1") @Min(1) @jakarta.validation.constraints.Max(100000) int page,
            @RequestParam(defaultValue = "10") @Min(1) @jakarta.validation.constraints.Max(100) int size) {

        Page<User> following = followService.getFollowing(userId, page, size);
        return Result.success(following);
    }
}

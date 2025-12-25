package com.vblog.controller;

import com.vblog.common.Result;
import com.vblog.service.PostLikeService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Post Like Controller
 * 
 * @author V-Blog Team
 */
@Slf4j
@RestController
@RequestMapping("/like")
@RequiredArgsConstructor
@Validated
public class PostLikeController extends BaseController {

    private final PostLikeService postLikeService;

    /**
     * Like a post
     */
    @PostMapping("/{postId}")
    public Result<Map<String, Object>> likePost(
            @PathVariable @NotNull Long postId,
            HttpServletRequest request) {
        
        Long userId = (Long) request.getAttribute("userId");
        
        boolean liked = postLikeService.likePost(userId, postId);
        Long likeCount = postLikeService.getLikeCount(postId);
        
        Map<String, Object> result = new HashMap<>();
        result.put("liked", liked);
        result.put("likeCount", likeCount);
        
        if (!liked) {
            return Result.error(40001, "已经点赞过该帖子");
        }
        
        return Result.success("点赞成功", result);
    }

    /**
     * Unlike a post
     */
    @DeleteMapping("/{postId}")
    public Result<Map<String, Object>> unlikePost(
            @PathVariable @NotNull Long postId,
            HttpServletRequest request) {
        
        Long userId = (Long) request.getAttribute("userId");
        
        boolean unliked = postLikeService.unlikePost(userId, postId);
        Long likeCount = postLikeService.getLikeCount(postId);
        
        Map<String, Object> result = new HashMap<>();
        result.put("unliked", unliked);
        result.put("likeCount", likeCount);
        
        if (!unliked) {
            return Result.error(40001, "没有点赞该帖子");
        }
        
        return Result.success("取消点赞成功", result);
    }

    /**
     * Check if post is liked by current user
     */
    @GetMapping("/{postId}/status")
    public Result<Map<String, Object>> getLikeStatus(
            @PathVariable @NotNull Long postId,
            HttpServletRequest request) {
        
        Long userId = (Long) request.getAttribute("userId");
        boolean isLiked = postLikeService.isLiked(userId, postId);
        Long likeCount = postLikeService.getLikeCount(postId);
        
        Map<String, Object> result = new HashMap<>();
        result.put("isLiked", isLiked);
        result.put("likeCount", likeCount);
        
        return Result.success(result);
    }

    /**
     * Get like count for a post
     */
    @GetMapping("/{postId}/count")
    public Result<Long> getLikeCount(@PathVariable @NotNull Long postId) {
        Long likeCount = postLikeService.getLikeCount(postId);
        return Result.success(likeCount);
    }
}



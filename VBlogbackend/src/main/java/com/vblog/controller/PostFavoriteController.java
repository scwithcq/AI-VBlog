package com.vblog.controller;

import com.vblog.common.Result;
import com.vblog.common.Page;
import com.vblog.entity.Post;
import com.vblog.mapper.PostFavoriteMapper;
import com.vblog.service.PostService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/favorite")
@RequiredArgsConstructor
public class PostFavoriteController {
    private final PostFavoriteMapper favoriteMapper;
    private final PostService postService;

    @GetMapping("/my")
    public Result<Page<Post>> myFavorites(@RequestParam(defaultValue = "1") int page,
                                          @RequestParam(defaultValue = "10") int size,
                                          HttpServletRequest request) {
        if (page < 1 || size < 1 || size > 50) {
            return Result.error(400, "分页参数不正确");
        }
        Long userId = (Long) request.getAttribute("userId");
        long total = favoriteMapper.countByUserId(userId);
        return Result.success(new Page<>(page, size, total,
                favoriteMapper.selectUserFavorites(userId, (long) (page - 1) * size, size)));
    }

    @GetMapping("/{postId}/status")
    public Result<Map<String, Object>> getStatus(@PathVariable Long postId, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(Map.of(
                "isFavorited", favoriteMapper.exists(postId, userId),
                "favoriteCount", favoriteMapper.countByPostId(postId)));
    }

    @PostMapping("/{postId}")
    @Transactional
    public Result<Map<String, Object>> add(@PathVariable Long postId, HttpServletRequest request) {
        if (!postService.postExists(postId)) {
            return Result.error(404, "帖子不存在");
        }
        Long userId = (Long) request.getAttribute("userId");
        favoriteMapper.add(postId, userId);
        return Result.success(Map.of("isFavorited", true, "favoriteCount", favoriteMapper.countByPostId(postId)));
    }

    @DeleteMapping("/{postId}")
    @Transactional
    public Result<Map<String, Object>> remove(@PathVariable Long postId, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        favoriteMapper.remove(postId, userId);
        return Result.success(Map.of("isFavorited", false, "favoriteCount", favoriteMapper.countByPostId(postId)));
    }
}

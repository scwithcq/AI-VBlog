package com.vblog.controller;

import com.vblog.common.Page;
import com.vblog.common.Result;
import com.vblog.entity.Post;
import com.vblog.entity.PostInteractionStatus;
import com.vblog.mapper.PostMapper;
import com.vblog.service.PostService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 帖子控制器
 *
 * @author V-Blog团队
 */
@Slf4j
@RestController
@RequestMapping("/post")
@RequiredArgsConstructor
@Validated
public class PostController extends BaseController {

    private final PostService postService;
    private final PostMapper postMapper;

    @GetMapping("/interactions")
    public Result<List<PostInteractionStatus>> getInteractionStatuses(
            @RequestParam List<Long> postIds, HttpServletRequest request) {
        if (postIds.isEmpty() || postIds.size() > 100 || postIds.stream().anyMatch(id -> id == null || id <= 0)) {
            return Result.error(400, "帖子 ID 数量须在 1 到 100 之间");
        }
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(postMapper.selectInteractionStatuses(userId, postIds));
    }

    /**
     * 发布新帖子
     */
    @PostMapping("/publish")
    public Result<Post> publishPost(
            @RequestParam(required = false) String content,
            @RequestParam(required = false) String images,
            @RequestParam(required = false) String videoUrl,
            @RequestParam(required = false) Long sourceId,
            HttpServletRequest request) {

        Long userId = (Long) request.getAttribute("userId");

        // 解析图片JSON字符串为List
        List<String> imageList = null;
        if (images != null && !images.isEmpty()) {
            try {
                imageList = com.alibaba.fastjson2.JSON.parseArray(images, String.class);
            } catch (Exception e) {
                log.warn("Failed to parse images JSON: {}", images);
                return Result.error(400, "图片格式不正确");
            }
        }

        Post post = postService.publishPost(userId, content, imageList, videoUrl, sourceId);
        return Result.success("发布成功", post);
    }

    /**
     * 获取帖子列表（信息流），带分页
     */
    @GetMapping("/list")
    public Result<Page<Post>> getPostList(
            @RequestParam(defaultValue = "1") @Min(1) @jakarta.validation.constraints.Max(100000) int page,
            @RequestParam(defaultValue = "10") @Min(1) @jakarta.validation.constraints.Max(100) int size) {

        Page<Post> postPage = postService.getPostList(page, size);
        return Result.success(postPage);
    }

    /**
     * 根据ID获取帖子
     */
    @GetMapping("/{postId}")
    public Result<Post> getPostById(@PathVariable Long postId) {
        Post post = postService.getPostById(postId);
        if (post == null) {
            return Result.error(404, "帖子不存在");
        }
        return Result.success(post);
    }

    /**
     * 根据用户ID获取帖子
     */
    @GetMapping("/user/{userId}")
    public Result<Page<Post>> getUserPosts(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "1") @Min(1) @jakarta.validation.constraints.Max(100000) int page,
            @RequestParam(defaultValue = "10") @Min(1) @jakarta.validation.constraints.Max(100) int size) {

        Page<Post> postPage = postService.getUserPosts(userId, page, size);
        return Result.success(postPage);
    }

    /**
     * 获取源帖子（用于转发显示）
     */
    @GetMapping("/{postId}/source")
    public Result<Post> getSourcePost(@PathVariable Long postId) {
        Post post = postService.getPostById(postId);
        if (post == null || post.getSourceId() == null) {
            return Result.error(404, "源帖子不存在");
        }

        Post sourcePost = postService.getSourcePost(post.getSourceId());
        if (sourcePost == null) {
            return Result.error(404, "源帖子不存在");
        }
        return Result.success(sourcePost);
    }

    /**
     * 转发帖子
     */
    @PostMapping("/repost")
    public Result<Post> repost(
            @RequestParam Long sourceId,
            @RequestParam(required = false) String content,
            HttpServletRequest request) {

        Long userId = (Long) request.getAttribute("userId");

        // 检查源帖子是否存在
        if (!postService.postExists(sourceId)) {
            return Result.error(404, "源帖子不存在");
        }

        Post repost = postService.publishPost(userId, content, null, null, sourceId);
        return Result.success("转发成功", repost);
    }

    /**
     * 删除帖子
     */
    @DeleteMapping("/{postId}")
    public Result<?> deletePost(
            @PathVariable Long postId,
            HttpServletRequest request) {

        Long userId = (Long) request.getAttribute("userId");

        boolean deleted = postService.deletePost(postId, userId);
        if (deleted) {
            return Result.success("删除成功");
        } else {
            return Result.error(400, "删除失败，帖子不存在或您没有权限");
        }
    }

    /**
     * 更新帖子
     */
    @PutMapping("/{postId}")
    public Result<Post> updatePost(
            @PathVariable Long postId,
            @RequestParam(required = false) String content,
            @RequestParam(required = false) String images,
            @RequestParam(required = false) String videoUrl,
            HttpServletRequest request) {

        Long userId = (Long) request.getAttribute("userId");

        // 解析图片JSON字符串为List
        List<String> imageList = null;
        if (images != null && !images.isEmpty()) {
            try {
                imageList = com.alibaba.fastjson2.JSON.parseArray(images, String.class);
            } catch (Exception e) {
                log.warn("Failed to parse images JSON: {}", images);
                return Result.error(400, "图片格式不正确");
            }
        }

        try {
            Post updatedPost = postService.updatePost(postId, userId, content, imageList, videoUrl);
            return Result.success("更新成功", updatedPost);
        } catch (RuntimeException e) {
            return Result.error(400, e.getMessage());
        }
    }

    /**
     * 根据话题ID获取帖子列表
     */
    @GetMapping("/topic/{topicId}")
    public Result<Page<Post>> getPostsByTopic(
            @PathVariable Long topicId,
            @RequestParam(defaultValue = "1") @Min(1) @jakarta.validation.constraints.Max(100000) int page,
            @RequestParam(defaultValue = "10") @Min(1) @jakarta.validation.constraints.Max(100) int size) {

        Page<Post> postPage = postService.getPostsByTopicId(topicId, page, size);
        return Result.success(postPage);
    }
}

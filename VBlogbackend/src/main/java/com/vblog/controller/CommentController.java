package com.vblog.controller;

import com.vblog.common.Result;
import com.vblog.entity.Comment;
import com.vblog.service.CommentService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 评论控制器
 * 
 * @author V-Blog团队
 */
@Slf4j
@RestController
@RequestMapping("/comment")
@RequiredArgsConstructor
@Validated
public class CommentController extends BaseController {

    private final CommentService commentService;

    /**
     * 添加评论
     */
    @PostMapping("/add")
    public Result<Comment> addComment(
            @RequestParam @NotNull(message = "帖子ID不能为空") Long postId,
            @RequestParam @NotBlank(message = "内容不能为空") String content,
            @RequestParam(required = false) Long parentId,
            HttpServletRequest request) {

        Long userId = (Long) request.getAttribute("userId");

        Comment comment = commentService.addComment(userId, postId, content, parentId);
        return Result.success("评论成功", comment);
    }

    /**
     * 根据帖子ID获取评论
     */
    @GetMapping("/post/{postId}")
    public Result<List<CommentService.CommentTree>> getCommentsByPostId(@PathVariable Long postId) {
        List<CommentService.CommentTree> comments = commentService.getCommentsByPostId(postId);
        return Result.success(comments);
    }

    /**
     * 根据ID获取评论
     */
    @GetMapping("/{commentId}")
    public Result<Comment> getCommentById(@PathVariable Long commentId) {
        Comment comment = commentService.getCommentById(commentId);
        if (comment == null) {
            return Result.error(404, "评论未找到");
        }
        return Result.success(comment);
    }

    /**
     * 删除评论
     */
    @DeleteMapping("/{commentId}")
    public Result<?> deleteComment(
            @PathVariable Long commentId,
            HttpServletRequest request) {

        Long userId = (Long) request.getAttribute("userId");

        boolean deleted = commentService.deleteComment(commentId, userId);
        if (deleted) {
            return Result.success("删除成功");
        } else {
            return Result.error(400, "删除失败，评论不存在或您没有权限");
        }
    }

    /**
     * 更新评论
     */
    @PutMapping("/{commentId}")
    public Result<Comment> updateComment(
            @PathVariable Long commentId,
            @RequestParam @NotBlank(message = "内容不能为空") String content,
            HttpServletRequest request) {

        Long userId = (Long) request.getAttribute("userId");

        try {
            Comment updatedComment = commentService.updateComment(commentId, userId, content);
            return Result.success("更新成功", updatedComment);
        } catch (RuntimeException e) {
            return Result.error(400, e.getMessage());
        }
    }

    /**
     * 点赞评论
     */
    @PostMapping("/like/{commentId}")
    public Result<?> likeComment(
            @PathVariable Long commentId,
            HttpServletRequest request) {

        Long userId = (Long) request.getAttribute("userId");

        boolean liked = commentService.likeComment(commentId, userId);
        Long likeCount = commentService.getCommentLikeCount(commentId);

        if (liked) {
            return Result.success("点赞成功", likeCount);
        } else {
            return Result.error(400, "已经点赞过该评论");
        }
    }

    /**
     * 取消点赞评论
     */
    @DeleteMapping("/like/{commentId}")
    public Result<?> unlikeComment(
            @PathVariable Long commentId,
            HttpServletRequest request) {

        Long userId = (Long) request.getAttribute("userId");

        boolean unliked = commentService.unlikeComment(commentId, userId);
        Long likeCount = commentService.getCommentLikeCount(commentId);

        if (unliked) {
            return Result.success("取消点赞成功", likeCount);
        } else {
            return Result.error(400, "没有点赞过该评论");
        }
    }

    /**
     * 获取评论点赞状态
     */
    @GetMapping("/like/{commentId}/status")
    public Result<?> getCommentLikeStatus(
            @PathVariable Long commentId,
            HttpServletRequest request) {

        Long userId = (Long) request.getAttribute("userId");

        boolean isLiked = commentService.isCommentLiked(commentId, userId);
        Long likeCount = commentService.getCommentLikeCount(commentId);

        return Result.success(new java.util.HashMap<String, Object>() {
            {
                put("isLiked", isLiked);
                put("likeCount", likeCount);
            }
        });
    }
}

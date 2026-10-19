package com.vblog.service.impl;

import com.vblog.entity.Comment;
import com.vblog.entity.CommentLike;
import com.vblog.entity.Post;
import com.vblog.mapper.CommentMapper;
import com.vblog.mapper.CommentLikeMapper;
import com.vblog.service.CommentService;
import com.vblog.service.PostService;
import com.vblog.service.SystemNotificationService;
import com.vblog.utils.XssUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 评论服务实现类
 *
 * @author V-Blog团队
 */
@Slf4j
@Service
public class CommentServiceImpl implements CommentService {

    private final CommentMapper commentMapper;
    private final CommentLikeMapper commentLikeMapper;
    private final XssUtil xssUtil;
    private PostService postService;
    private SystemNotificationService systemNotificationService;

    public CommentServiceImpl(CommentMapper commentMapper, CommentLikeMapper commentLikeMapper, XssUtil xssUtil) {
        this.commentMapper = commentMapper;
        this.commentLikeMapper = commentLikeMapper;
        this.xssUtil = xssUtil;
    }

    @Autowired
    @Lazy
    public void setPostService(PostService postService) {
        this.postService = postService;
    }

    @Autowired
    @Lazy
    public void setSystemNotificationService(SystemNotificationService systemNotificationService) {
        this.systemNotificationService = systemNotificationService;
    }

    @Override
    @Transactional
    public Comment addComment(Long userId, Long postId, String content, Long parentId) {
        if (!postService.postExists(postId)) {
            throw new IllegalArgumentException("帖子不存在");
        }
        if (parentId != null) {
            Comment parent = commentMapper.selectById(parentId);
            if (parent == null || !postId.equals(parent.getPostId())) {
                throw new IllegalArgumentException("回复的评论不属于此帖子");
            }
        }
        // 过滤XSS
        content = xssUtil.filter(content);

        // 创建评论
        Comment comment = new Comment();
        comment.setUserId(userId);
        comment.setPostId(postId);
        comment.setContent(content);
        comment.setParentId(parentId);

        commentMapper.insert(comment);
        log.info("Comment added: commentId={}, postId={}, userId={}", comment.getCommentId(), postId, userId);

        // 创建系统互动通知
        try {
            Post post = postService.getPostById(postId);
            if (post != null && !post.getUserId().equals(userId)) {
                systemNotificationService.createCommentNotification(
                        post.getUserId(), userId, postId, comment.getCommentId(), content);
            }

            // 如果是回复评论，也通知被回复的用户
            if (parentId != null) {
                Comment parentComment = commentMapper.selectById(parentId);
                if (parentComment != null && !parentComment.getUserId().equals(userId)
                        && (post == null || !parentComment.getUserId().equals(post.getUserId()))) {
                    systemNotificationService.createCommentNotification(
                            parentComment.getUserId(), userId, postId, comment.getCommentId(), content);
                }
            }
        } catch (Exception e) {
            log.error("Failed to create comment notification", e);
        }

        return comment;
    }

    @Override
    public List<CommentTree> getCommentsByPostId(Long postId) {
        // 获取该帖子的所有评论
        List<Comment> comments = commentMapper.selectByPostId(postId);

        // 按创建时间排序评论
        comments.sort(Comparator.comparing(Comment::getCreateTime));

        // 构建评论树结构
        Map<Long, CommentTree> treeMap = comments.stream()
                .collect(Collectors.toMap(
                        Comment::getCommentId,
                        CommentTree::new));

        List<CommentTree> rootComments = new ArrayList<>();

        for (Comment comment : comments) {
            CommentTree tree = treeMap.get(comment.getCommentId());
            if (comment.getParentId() == null) {
                // 顶层评论
                rootComments.add(tree);
            } else {
                // 回复其他评论
                CommentTree parentTree = treeMap.get(comment.getParentId());
                if (parentTree != null) {
                    parentTree.getReplies().add(tree);
                }
            }
        }

        return rootComments;
    }

    @Override
    public Comment getCommentById(Long commentId) {
        return commentMapper.selectById(commentId);
    }

    @Override
    @Transactional
    public boolean deleteComment(Long commentId, Long userId) {
        // 先检查评论是否存在
        Comment comment = commentMapper.selectById(commentId);
        if (comment == null) {
            return false;
        }

        // 检查是否是评论作者
        if (!comment.getUserId().equals(userId)) {
            return false;
        }

        // 只在当前帖子内清理此评论及其回复，防止留下不可见的孤立回复。
        List<Comment> postComments = commentMapper.selectByPostId(comment.getPostId());
        java.util.Set<Long> deletedIds = new java.util.HashSet<>();
        deletedIds.add(commentId);
        boolean changed;
        do {
            changed = false;
            for (Comment candidate : postComments) {
                if (deletedIds.contains(candidate.getParentId()) && deletedIds.add(candidate.getCommentId())) {
                    changed = true;
                }
            }
        } while (changed);
        for (Long id : deletedIds) {
            commentLikeMapper.deleteByCommentId(id);
            commentMapper.deleteById(id);
        }
        log.info("Comment deleted: commentId={}, userId={}", commentId, userId);
        return true;
    }

    @Override
    @Transactional
    public Comment updateComment(Long commentId, Long userId, String content) {
        // 先检查评论是否存在
        Comment comment = commentMapper.selectById(commentId);
        if (comment == null) {
            throw new RuntimeException("评论不存在");
        }

        // 检查是否是评论作者
        if (!comment.getUserId().equals(userId)) {
            throw new RuntimeException("无权修改该评论");
        }

        // 过滤XSS
        content = xssUtil.filter(content);

        // 更新评论内容
        comment.setContent(content);
        commentMapper.update(comment);
        log.info("Comment updated: commentId={}, userId={}", commentId, userId);
        return comment;
    }

    // TODO: 实现评论点赞相关方法
    @Override
    @Transactional
    public boolean likeComment(Long commentId, Long userId) {
        // 检查评论是否存在
        Comment comment = commentMapper.selectById(commentId);
        if (comment == null) {
            throw new RuntimeException("评论不存在");
        }

        // 检查是否已经点赞
        Map<String, Object> params = new HashMap<>();
        params.put("commentId", commentId);
        params.put("userId", userId);
        CommentLike existingLike = commentLikeMapper.selectByMap(params);

        if (existingLike != null) {
            log.warn("User {} has already liked comment {}", userId, commentId);
            return false;
        }

        // 创建点赞记录
        CommentLike commentLike = new CommentLike();
        commentLike.setCommentId(commentId);
        commentLike.setUserId(userId);
        if (commentLikeMapper.insert(commentLike) == 0) return false;

        log.info("Comment liked: commentId={}, userId={}", commentId, userId);
        return true;
    }

    @Override
    @Transactional
    public boolean unlikeComment(Long commentId, Long userId) {
        // 检查评论是否存在
        Comment comment = commentMapper.selectById(commentId);
        if (comment == null) {
            throw new RuntimeException("评论不存在");
        }

        // 查找点赞记录
        Map<String, Object> params = new HashMap<>();
        params.put("commentId", commentId);
        params.put("userId", userId);
        CommentLike existingLike = commentLikeMapper.selectByMap(params);

        if (existingLike == null) {
            log.warn("User {} has not liked comment {}", userId, commentId);
            return false;
        }

        // 删除点赞记录
        commentLikeMapper.deleteByMap(params);

        log.info("Comment unliked: commentId={}, userId={}", commentId, userId);
        return true;
    }

    @Override
    public boolean isCommentLiked(Long commentId, Long userId) {
        Map<String, Object> params = new HashMap<>();
        params.put("commentId", commentId);
        params.put("userId", userId);
        CommentLike existingLike = commentLikeMapper.selectByMap(params);
        return existingLike != null;
    }

    @Override
    public Long getCommentLikeCount(Long commentId) {
        return commentLikeMapper.countByCommentId(commentId);
    }
}

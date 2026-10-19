package com.vblog.service.impl;

import com.vblog.entity.Post;
import com.vblog.entity.PostLike;
import com.vblog.mapper.PostLikeMapper;
import com.vblog.service.PostLikeService;
import com.vblog.service.PostService;
import com.vblog.service.SystemNotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


/**
 * 帖子点赞服务实现类
 *
 * @author V-Blog团队
 */
@Slf4j
@Service
public class PostLikeServiceImpl implements PostLikeService {

    private final PostLikeMapper postLikeMapper;
    private PostService postService;
    private SystemNotificationService systemNotificationService;

    public PostLikeServiceImpl(PostLikeMapper postLikeMapper) {
        this.postLikeMapper = postLikeMapper;
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
    public boolean likePost(Long userId, Long postId) {
        if (!postService.postExists(postId)) {
            throw new IllegalArgumentException("帖子不存在");
        }
        // 检查是否已点赞
        PostLike existing = postLikeMapper.selectByUserAndPostId(postId, userId);

        if (existing != null) {
            log.debug("User {} already liked post {}", userId, postId);
            return false; // 已点赞
        }

        // 创建新的点赞记录
        PostLike postLike = new PostLike();
        postLike.setUserId(userId);
        postLike.setPostId(postId);
        if (postLikeMapper.insert(postLike) == 0) return false;
        log.info("Post liked: postId={}, userId={}", postId, userId);

        // 创建系统互动通知
        try {
            Post post = postService.getPostById(postId);
            if (post != null && !post.getUserId().equals(userId)) {
                systemNotificationService.createLikeNotification(post.getUserId(), userId, postId);
            }
        } catch (Exception e) {
            log.error("Failed to create like notification", e);
        }

        return true;
    }

    @Override
    @Transactional
    public boolean unlikePost(Long userId, Long postId) {
        // 删除点赞记录
        int result = postLikeMapper.deleteByUserAndPostId(postId, userId);

        if (result > 0) {
            log.info("Post unliked: postId={}, userId={}", postId, userId);
            return true;
        } else {
            log.debug("User {} has not liked post {}", userId, postId);
            return false; // 未点赞
        }
    }

    @Override
    public boolean isLiked(Long userId, Long postId) {
        PostLike existing = postLikeMapper.selectByUserAndPostId(postId, userId);
        return existing != null;
    }

    @Override
    public Long getLikeCount(Long postId) {
        return postLikeMapper.countByPostId(postId);
    }
}

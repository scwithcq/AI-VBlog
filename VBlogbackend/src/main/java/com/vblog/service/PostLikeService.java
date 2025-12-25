package com.vblog.service;

/**
 * Post Like Service Interface
 * 
 * @author V-Blog Team
 */
public interface PostLikeService {

    /**
     * Like a post
     * 
     * @param userId User ID
     * @param postId Post ID
     * @return true if liked, false if already liked
     */
    boolean likePost(Long userId, Long postId);

    /**
     * Unlike a post
     * 
     * @param userId User ID
     * @param postId Post ID
     * @return true if unliked, false if not liked
     */
    boolean unlikePost(Long userId, Long postId);

    /**
     * Check if user has liked the post
     * 
     * @param userId User ID
     * @param postId Post ID
     * @return true if liked, false otherwise
     */
    boolean isLiked(Long userId, Long postId);

    /**
     * Get like count for a post
     * 
     * @param postId Post ID
     * @return Like count
     */
    Long getLikeCount(Long postId);
}



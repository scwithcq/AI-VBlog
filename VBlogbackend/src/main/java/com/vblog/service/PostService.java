package com.vblog.service;

import com.vblog.common.Page;
import com.vblog.entity.Post;

import java.util.List;

/**
 * Post Service Interface
 * 
 * @author V-Blog Team
 */
public interface PostService {

    /**
     * Publish a new post
     * 
     * @param userId User ID
     * @param content Post content
     * @param images Image URLs (JSON array)
     * @param videoUrl Video URL
     * @param sourceId Source post ID (for repost, null for original post)
     * @return Created post
     */
    Post publishPost(Long userId, String content, List<String> images, String videoUrl, Long sourceId);

    /**
     * Get post list (feed) with pagination
     * 
     * @param page Page number
     * @param size Page size
     * @return Page of posts
     */
    Page<Post> getPostList(int page, int size);

    /**
     * Get post by ID
     * 
     * @param postId Post ID
     * @return Post entity
     */
    Post getPostById(Long postId);

    /**
     * Get posts by user ID
     * 
     * @param userId User ID
     * @param page Page number
     * @param size Page size
     * @return Page of posts
     */
    Page<Post> getUserPosts(Long userId, int page, int size);

    /**
     * Check if post exists
     * 
     * @param postId Post ID
     * @return true if exists, false otherwise
     */
    boolean postExists(Long postId);

    /**
     * Get source post (for repost display)
     * 
     * @param sourceId Source post ID
     * @return Source post, or null if not exists
     */
    Post getSourcePost(Long sourceId);
    
    /**
     * Delete a post
     * 
     * @param postId Post ID
     * @param userId User ID
     * @return true if deleted, false otherwise
     */
    boolean deletePost(Long postId, Long userId);
    
    /**
     * Update a post
     * 
     * @param postId Post ID
     * @param userId User ID
     * @param content Post content
     * @param images Image URLs (JSON array)
     * @param videoUrl Video URL
     * @return Updated post
     */
    Post updatePost(Long postId, Long userId, String content, List<String> images, String videoUrl);
    
    /**
     * Get posts by topic with pagination
     * 
     * @param topicId Topic ID
     * @param page Page number
     * @param size Page size
     * @return Page of posts
     */
    Page<Post> getPostsByTopicId(Long topicId, int page, int size);
}


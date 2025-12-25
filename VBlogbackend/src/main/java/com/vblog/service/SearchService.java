package com.vblog.service;

import com.vblog.common.Page;
import com.vblog.entity.Post;
import com.vblog.entity.Topic;
import com.vblog.entity.User;

import java.util.List;

/**
 * Search Service Interface
 * 
 * @author V-Blog Team
 */
public interface SearchService {

    /**
     * Search users by username (fuzzy match)
     * 
     * @param keyword Search keyword
     * @param page Page number
     * @param size Page size
     * @return Page of users
     */
    Page<User> searchUsers(String keyword, int page, int size);

    /**
     * Search posts by content (fuzzy match)
     * 
     * @param keyword Search keyword
     * @param page Page number
     * @param size Page size
     * @return Page of posts
     */
    Page<Post> searchPosts(String keyword, int page, int size);

    /**
     * Get hot topics (Top N)
     * 
     * @param limit Number of topics to return
     * @return List of hot topics
     */
    List<Topic> getHotTopics(int limit);
}



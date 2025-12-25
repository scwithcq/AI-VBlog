package com.vblog.controller;

import com.vblog.common.Page;
import com.vblog.common.Result;
import com.vblog.entity.Post;
import com.vblog.entity.Topic;
import com.vblog.entity.User;
import com.vblog.service.SearchService;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Search Controller
 * 
 * @author V-Blog Team
 */
@Slf4j
@RestController
@RequestMapping("/search")
@RequiredArgsConstructor
@Validated
public class SearchController extends BaseController {

    private final SearchService searchService;

    /**
     * Search users
     */
    @GetMapping("/users")
    public Result<Page<User>> searchUsers(
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "1") @Min(1) int page,
            @RequestParam(defaultValue = "10") @Min(1) int size) {
        
        Page<User> users = searchService.searchUsers(q, page, size);
        return Result.success(users);
    }

    /**
     * Search posts
     */
    @GetMapping("/posts")
    public Result<Page<Post>> searchPosts(
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "1") @Min(1) int page,
            @RequestParam(defaultValue = "10") @Min(1) int size) {
        
        Page<Post> posts = searchService.searchPosts(q, page, size);
        return Result.success(posts);
    }

    /**
     * Get hot topics
     */
    @GetMapping("/topics/hot")
    public Result<List<Topic>> getHotTopics(
            @RequestParam(defaultValue = "10") @Min(1) int limit) {
        
        List<Topic> topics = searchService.getHotTopics(limit);
        return Result.success(topics);
    }
}



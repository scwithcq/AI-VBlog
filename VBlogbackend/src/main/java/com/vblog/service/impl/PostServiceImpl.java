package com.vblog.service.impl;

import com.alibaba.fastjson2.JSON;
import com.vblog.common.Page;
import com.vblog.entity.Post;
import com.vblog.entity.Topic;
import com.vblog.mapper.PostMapper;
import com.vblog.service.PostService;
import com.vblog.service.TopicService;
import com.vblog.utils.XssUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 帖子服务实现类
 * 
 * @author V-Blog团队
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PostServiceImpl implements PostService {

    private final PostMapper postMapper;
    private final TopicService topicService;
    private final XssUtil xssUtil;

    @Override
    @Transactional
    public Post publishPost(Long userId, String content, List<String> images, String videoUrl, Long sourceId) {
        // 验证内容长度（最大2000字符）
        if (content != null && content.length() > 2000) {
            throw new RuntimeException("内容超过2000字符限制");
        }

        // 过滤XSS
        content = xssUtil.filter(content);

        // 解析并更新话题
        List<String> topics = topicService.parseTopics(content);
        for (String topicName : topics) {
            topicService.createOrUpdateTopic(topicName);
        }

        // 创建帖子
        Post post = new Post();
        post.setUserId(userId);
        post.setContent(content);
        post.setVideoUrl(videoUrl);
        post.setSourceId(sourceId);

        // 将图片列表转换为JSON字符串
        if (images != null && !images.isEmpty()) {
            post.setImages(JSON.toJSONString(images));
        }

        postMapper.insert(post);
        log.info("Post published: postId={}, userId={}", post.getPostId(), userId);

        return post;
    }

    @Override
    public Page<Post> getPostList(int page, int size) {
        // 获取所有帖子并按创建时间排序
        List<Post> allPosts = postMapper.selectAll();
        allPosts.sort(Comparator.comparing(Post::getCreateTime).reversed());

        // 分页处理
        long total = allPosts.size();
        int offset = (page - 1) * size;
        int end = Math.min(offset + size, allPosts.size());
        List<Post> paginatedPosts = allPosts.subList(offset, end);

        return new Page<>(page, size, total, paginatedPosts);
    }

    @Override
    public Post getPostById(Long postId) {
        return postMapper.selectById(postId);
    }

    @Override
    public Page<Post> getUserPosts(Long userId, int page, int size) {
        Map<String, Object> map = new HashMap<>();
        map.put("user_id", userId);

        List<Post> allPosts = postMapper.selectByMap(map);
        allPosts.sort(Comparator.comparing(Post::getCreateTime).reversed());

        // 分页处理
        long total = allPosts.size();
        int offset = (page - 1) * size;
        int end = Math.min(offset + size, allPosts.size());
        List<Post> paginatedPosts = allPosts.subList(offset, end);

        return new Page<>(page, size, total, paginatedPosts);
    }

    @Override
    public boolean postExists(Long postId) {
        return postMapper.selectById(postId) != null;
    }

    @Override
    public Post getSourcePost(Long sourceId) {
        if (sourceId == null) {
            return null;
        }
        return postMapper.selectById(sourceId);
    }

    @Override
    @Transactional
    public boolean deletePost(Long postId, Long userId) {
        // 检查帖子是否存在
        Post post = postMapper.selectById(postId);
        if (post == null) {
            return false;
        }

        // 检查是否是帖子作者
        if (!post.getUserId().equals(userId)) {
            return false;
        }

        // 删除帖子
        postMapper.deleteById(postId);
        log.info("Post deleted: postId={}, userId={}", postId, userId);
        return true;
    }

    @Override
    @Transactional
    public Post updatePost(Long postId, Long userId, String content, List<String> images, String videoUrl) {
        // 检查帖子是否存在
        Post post = postMapper.selectById(postId);
        if (post == null) {
            throw new RuntimeException("帖子不存在");
        }

        // 检查是否是帖子作者
        if (!post.getUserId().equals(userId)) {
            throw new RuntimeException("无权修改此帖子");
        }

        // 验证内容长度（最大2000字符）
        if (content != null && content.length() > 2000) {
            throw new RuntimeException("内容超过2000字符限制");
        }

        // 过滤XSS
        content = xssUtil.filter(content);

        // 解析并更新话题
        List<String> topics = topicService.parseTopics(content);
        for (String topicName : topics) {
            topicService.createOrUpdateTopic(topicName);
        }

        // 更新帖子内容
        post.setContent(content);
        post.setVideoUrl(videoUrl);

        // 更新图片列表
        if (images != null && !images.isEmpty()) {
            post.setImages(JSON.toJSONString(images));
        } else {
            post.setImages(null);
        }

        postMapper.update(post);
        log.info("Post updated: postId={}, userId={}", postId, userId);

        return post;
    }

    @Override
    public Page<Post> getPostsByTopicId(Long topicId, int page, int size) {
        // 根据话题ID获取话题信息
        Topic topic = topicService.getTopicById(topicId);
        if (topic == null) {
            log.warn("Topic not found: topicId={}", topicId);
            return new Page<>(page, size, 0L, new ArrayList<>());
        }

        // 根据话题名称搜索包含该话题的帖子
        List<Post> posts = postMapper.selectByTopicName(topic.getName());

        // 按创建时间排序（从新到旧）
        posts.sort(Comparator.comparing(Post::getCreateTime).reversed());

        // 分页处理
        long total = posts.size();
        int offset = (page - 1) * size;
        int end = Math.min(offset + size, posts.size());

        if (offset >= posts.size()) {
            return new Page<>(page, size, total, new ArrayList<>());
        }

        List<Post> paginatedPosts = posts.subList(offset, end);

        log.info("Retrieved {} posts for topic: {} (total: {})", paginatedPosts.size(), topic.getName(), total);
        return new Page<>(page, size, total, paginatedPosts);
    }
}

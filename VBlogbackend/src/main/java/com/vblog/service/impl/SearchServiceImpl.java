package com.vblog.service.impl;

import com.vblog.common.Page;
import com.vblog.entity.Post;
import com.vblog.entity.Topic;
import com.vblog.entity.User;
import com.vblog.mapper.PostMapper;
import com.vblog.mapper.UserMapper;
import com.vblog.service.SearchService;
import com.vblog.service.TopicService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 搜索服务实现类
 *
 * @author V-Blog团队
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SearchServiceImpl implements SearchService {

    private final UserMapper userMapper;
    private final PostMapper postMapper;
    private final TopicService topicService;

    @Override
    public Page<User> searchUsers(String keyword, int page, int size) {
        // 获取所有用户
        List<User> allUsers = userMapper.selectAll();
        allUsers = allUsers.stream().map(com.vblog.utils.InputSafety::publicUser).toList();

        // 根据关键词过滤用户
        List<User> filteredUsers = allUsers.stream()
                .filter(user -> keyword == null || user.getUsername().contains(keyword))
                .sorted(Comparator.comparing(User::getCreateTime).reversed())
                .collect(Collectors.toList());

        // 分页处理
        long total = filteredUsers.size();
        int offset = (page - 1) * size;
        int end = Math.min(offset + size, filteredUsers.size());
        List<User> paginatedUsers = offset < end ? filteredUsers.subList(offset, end) : Collections.emptyList();

        return new Page<>(page, size, total, paginatedUsers);
    }

    @Override
    public Page<Post> searchPosts(String keyword, int page, int size) {
        // 获取所有帖子
        List<Post> allPosts = postMapper.selectAll();

        // 根据关键词过滤帖子
        List<Post> filteredPosts = allPosts.stream()
                .filter(post -> keyword == null ||
                        (post.getContent() != null && post.getContent().contains(keyword)))
                .sorted(Comparator.comparing(Post::getCreateTime).reversed())
                .collect(Collectors.toList());

        // 分页处理
        long total = filteredPosts.size();
        int offset = (page - 1) * size;
        int end = Math.min(offset + size, filteredPosts.size());
        List<Post> paginatedPosts = offset < end ? filteredPosts.subList(offset, end) : Collections.emptyList();

        return new Page<>(page, size, total, paginatedPosts);
    }

    @Override
    public List<Topic> getHotTopics(int limit) {
        return topicService.getHotTopics(limit);
    }
}

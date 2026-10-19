package com.vblog.controller;

import com.vblog.common.Page;
import com.vblog.common.Result;
import com.vblog.entity.Post;
import com.vblog.entity.Topic;
import com.vblog.service.PostService;
import com.vblog.service.TopicService;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 话题控制器
 *
 * @author V-Blog团队
 */
@Slf4j
@RestController
@RequestMapping("/topic")
@RequiredArgsConstructor
@Validated
public class TopicController extends BaseController {

    private final TopicService topicService;
    private final PostService postService;

    /**
     * 获取热门话题
     */
    @GetMapping("/hot")
    public Result<List<Topic>> getHotTopics(
            @RequestParam(defaultValue = "10") @Min(1) @jakarta.validation.constraints.Max(100) int limit) {

        List<Topic> hotTopics = topicService.getHotTopics(limit);
        return Result.success(hotTopics);
    }

    /**
     * 根据名称获取话题
     */
    @GetMapping("/name/{name}")
    public Result<Topic> getTopicByName(@PathVariable String name) {

        Topic topic = topicService.getTopicByName(name);
        if (topic == null) {
            return Result.error(404, "话题未找到");
        }
        return Result.success(topic);
    }

    /**
     * 根据话题名称获取帖子列表
     */
    @GetMapping("/name/{name}/posts")
    public Result<Page<Post>> getPostsByTopicName(
            @PathVariable String name,
            @RequestParam(defaultValue = "1") @Min(1) @jakarta.validation.constraints.Max(100000) int page,
            @RequestParam(defaultValue = "10") @Min(1) @jakarta.validation.constraints.Max(100) int size) {

        // 先获取话题信息，确保话题存在
        Topic topic = topicService.getTopicByName(name);
        if (topic == null) {
            return Result.error(404, "话题未找到");
        }

        Page<Post> postPage = postService.getPostsByTopicId(topic.getTopicId(), page, size);
        return Result.success(postPage);
    }
}

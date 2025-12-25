package com.vblog.service.impl;

import com.vblog.entity.Topic;
import com.vblog.mapper.TopicMapper;
import com.vblog.service.TopicService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 话题服务实现类
 * 
 * @author V-Blog团队
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TopicServiceImpl implements TopicService {

    private final TopicMapper topicMapper;

    /**
     * 匹配 #话题# 的正则表达式
     */
    private static final Pattern TOPIC_PATTERN = Pattern.compile("#([^#]+)#");

    @Override
    public List<String> parseTopics(String content) {
        if (content == null || content.isEmpty()) {
            return new ArrayList<>();
        }

        List<String> topics = new ArrayList<>();
        Matcher matcher = TOPIC_PATTERN.matcher(content);
        while (matcher.find()) {
            String topicName = matcher.group(1).trim();
            if (!topicName.isEmpty()) {
                topics.add(topicName);
            }
        }
        return topics;
    }

    @Override
    @Transactional
    public void createOrUpdateTopic(String topicName) {
        if (topicName == null || topicName.trim().isEmpty()) {
            return;
        }

        // 获取现有话题
        Topic existingTopic = topicMapper.selectByName(topicName);

        if (existingTopic != null) {
            // 更新热度：增加1
            BigDecimal newHeat = existingTopic.getHeat().add(BigDecimal.ONE);
            existingTopic.setHeat(newHeat);
            topicMapper.update(existingTopic);
            log.debug("话题热度已更新: {} -> {}", topicName, newHeat);
        } else {
            // 创建新话题
            Topic topic = new Topic();
            topic.setName(topicName);
            topic.setHeat(BigDecimal.ONE);
            topicMapper.insert(topic);
            log.info("已创建新话题: {}", topicName);
        }
    }

    @Override
    public List<Topic> getHotTopics(int limit) {
        // 获取所有话题并按热度排序
        List<Topic> allTopics = topicMapper.selectAll();
        allTopics.sort(Comparator.comparing(Topic::getHeat).reversed());

        // 返回前N个话题
        return allTopics.stream()
                .limit(limit)
                .collect(Collectors.toList());
    }

    @Override
    public Topic getTopicByName(String name) {
        return topicMapper.selectByName(name);
    }

    @Override
    public Topic getTopicById(Long topicId) {
        return topicMapper.selectById(topicId);
    }
}

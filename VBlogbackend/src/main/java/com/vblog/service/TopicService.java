package com.vblog.service;

import com.vblog.entity.Topic;

import java.util.List;

/**
 * Topic Service Interface
 * 
 * @author V-Blog Team
 */
public interface TopicService {

    /**
     * Parse topics from content
     * Extract #topic# patterns from content
     * 
     * @param content Post content
     * @return List of topic names
     */
    List<String> parseTopics(String content);

    /**
     * Create or update topic
     * If topic exists, increase heat; otherwise create new topic
     * 
     * @param topicName Topic name (without #)
     */
    void createOrUpdateTopic(String topicName);

    /**
     * Get hot topics (Top N)
     * 
     * @param limit Number of topics to return
     * @return List of hot topics
     */
    List<Topic> getHotTopics(int limit);

    /**
     * Get topic by name
     * 
     * @param name Topic name
     * @return Topic entity
     */
    Topic getTopicByName(String name);

    /**
     * Get topic by id
     * 
     * @param topicId Topic ID
     * @return Topic entity
     */
    Topic getTopicById(Long topicId);
}

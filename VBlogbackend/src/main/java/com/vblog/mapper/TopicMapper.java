package com.vblog.mapper;

import com.vblog.entity.Topic;

import java.util.List;
import java.util.Map;

/**
 * Topic Mapper Interface
 * 
 * @author V-Blog Team
 */
public interface TopicMapper {

    /**
     * Select by id
     *
     * @param topicId topic id
     * @return topic
     */
    Topic selectById(Long topicId);

    /**
     * Select all
     *
     * @return topic list
     */
    List<Topic> selectAll();

    /**
     * Select by map
     *
     * @param map query conditions
     * @return topic list
     */
    List<Topic> selectByMap(Map<String, Object> map);

    /**
     * Select by name
     *
     * @param name topic name
     * @return topic
     */
    Topic selectByName(String name);

    /**
     * Insert
     *
     * @param topic topic
     * @return affected rows
     */
    int insert(Topic topic);

    /**
     * Update
     *
     * @param topic topic
     * @return affected rows
     */
    int update(Topic topic);

    /**
     * Delete by id
     *
     * @param topicId topic id
     * @return affected rows
     */
    int deleteById(Long topicId);

    /**
     * Increase heat
     *
     * @param topicId topic id
     * @param amount  heat amount to increase
     * @return affected rows
     */
    int increaseHeat(Long topicId, int amount);

}

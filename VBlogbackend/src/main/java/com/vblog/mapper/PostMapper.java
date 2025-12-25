package com.vblog.mapper;

import com.vblog.entity.Post;

import java.util.List;
import java.util.Map;

/**
 * Post Mapper Interface
 * 
 * @author V-Blog Team
 */
public interface PostMapper {

    /**
     * Select by id
     *
     * @param postId post id
     * @return post
     */
    Post selectById(Long postId);

    /**
     * Select all
     *
     * @return post list
     */
    List<Post> selectAll();

    /**
     * Select by map
     *
     * @param map query conditions
     * @return post list
     */
    List<Post> selectByMap(Map<String, Object> map);

    /**
     * Select by user id
     *
     * @param userId user id
     * @return post list
     */
    List<Post> selectByUserId(Long userId);

    /**
     * Insert
     *
     * @param post post
     * @return affected rows
     */
    int insert(Post post);

    /**
     * Update
     *
     * @param post post
     * @return affected rows
     */
    int update(Post post);

    /**
     * Delete by id
     *
     * @param postId post id
     * @return affected rows
     */
    int deleteById(Long postId);

    /**
     * Delete by user id
     *
     * @param userId user id
     * @return affected rows
     */
    int deleteByUserId(Long userId);

    /**
     * Select posts by topic name (search content containing #topicName#)
     *
     * @param topicName topic name
     * @return post list
     */
    List<Post> selectByTopicName(String topicName);

}

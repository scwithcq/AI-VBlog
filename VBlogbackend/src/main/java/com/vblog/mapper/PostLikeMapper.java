package com.vblog.mapper;

import com.vblog.entity.PostLike;

import java.util.List;
import java.util.Map;

/**
 * PostLike Mapper Interface
 *
 * @author V-Blog Team
 */
public interface PostLikeMapper {

    /**
     * Select by id
     *
     * @param likeId like id
     * @return post like
     */
    PostLike selectById(Long likeId);

    /**
     * Select all
     *
     * @return post like list
     */
    List<PostLike> selectAll();

    /**
     * Select by map
     *
     * @param map query conditions
     * @return post like list
     */
    List<PostLike> selectByMap(Map<String, Object> map);

    /**
     * Insert
     *
     * @param postLike post like
     * @return affected rows
     */
    int insert(PostLike postLike);

    /**
     * Delete by id
     *
     * @param likeId like id
     * @return affected rows
     */
    int deleteById(Long likeId);

    /**
     * Delete by user and post id
     *
     * @param postId  post id
     * @param userId user id
     * @return affected rows
     */
    int deleteByUserAndPostId(Long postId, Long userId);

    /**
     * Select by user and post id
     *
     * @param postId  post id
     * @param userId user id
     * @return post like
     */
    PostLike selectByUserAndPostId(Long postId, Long userId);

    Long countByPostId(Long postId);

}

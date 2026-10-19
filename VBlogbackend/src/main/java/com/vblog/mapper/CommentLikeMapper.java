package com.vblog.mapper;

import com.vblog.entity.CommentLike;

import java.util.Map;

/**
 * Comment Like Mapper Interface
 *
 * @author V-Blog Team
 */
public interface CommentLikeMapper {

  /**
   * Select by id
   *
   * @param likeId like id
   * @return comment like
   */
  CommentLike selectById(Long likeId);

  /**
   * Select by comment id and user id
   *
   * @param map query conditions
   * @return comment like
   */
  CommentLike selectByMap(Map<String, Object> map);

  /**
   * Count by comment id
   *
   * @param commentId comment id
   * @return count
   */
  Long countByCommentId(Long commentId);

  /**
   * Insert
   *
   * @param commentLike comment like
   * @return affected rows
   */
  int insert(CommentLike commentLike);

  /**
   * Delete by id
   *
   * @param likeId like id
   * @return affected rows
   */
  int deleteById(Long likeId);

  int deleteByCommentId(Long commentId);

  /**
   * Delete by map
   *
   * @param map delete conditions
   * @return affected rows
   */
  int deleteByMap(Map<String, Object> map);
}

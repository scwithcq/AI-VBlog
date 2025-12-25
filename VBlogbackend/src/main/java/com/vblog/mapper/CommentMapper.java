package com.vblog.mapper;

import com.vblog.entity.Comment;

import java.util.List;
import java.util.Map;

/**
 * Comment Mapper Interface
 * 
 * @author V-Blog Team
 */
public interface CommentMapper {

    /**
     * Select by id
     *
     * @param commentId comment id
     * @return comment
     */
    Comment selectById(Long commentId);

    /**
     * Select all
     *
     * @return comment list
     */
    List<Comment> selectAll();

    /**
     * Select by map
     *
     * @param map query conditions
     * @return comment list
     */
    List<Comment> selectByMap(Map<String, Object> map);

    /**
     * Insert
     *
     * @param comment comment
     * @return affected rows
     */
    int insert(Comment comment);

    /**
     * Update
     *
     * @param comment comment
     * @return affected rows
     */
    int update(Comment comment);

    /**
     * Delete by id
     *
     * @param commentId comment id
     * @return affected rows
     */
    int deleteById(Long commentId);

}

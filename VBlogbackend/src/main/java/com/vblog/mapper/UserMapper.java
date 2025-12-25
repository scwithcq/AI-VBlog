package com.vblog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.vblog.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * User Mapper Interface
 * 
 * @author V-Blog Team
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {

    /**
     * Select by id
     *
     * @param userId user id
     * @return user
     */
    User selectById(Long userId);

    /**
     * Select all
     *
     * @return user list
     */
    List<User> selectAll();

    /**
     * Select by map
     *
     * @param map query conditions
     * @return user list
     */
    List<User> selectByMap(Map<String, Object> map);

    /**
     * Select by username
     *
     * @param username username
     * @return user
     */
    User selectByUsername(String username);

    /**
     * Insert
     *
     * @param user user
     * @return affected rows
     */
    int insert(User user);

    /**
     * Update
     *
     * @param user user
     * @return affected rows
     */
    int update(User user);

    /**
     * Delete by id
     *
     * @param userId user id
     * @return affected rows
     */
    int deleteById(Long userId);

    /**
     * 搜索用户（按用户名或昵称）
     *
     * @param keyword       搜索关键词
     * @param currentUserId 当前用户ID（排除自己）
     * @return 用户列表
     */
    List<User> searchUsers(@Param("keyword") String keyword, @Param("currentUserId") Long currentUserId);

}

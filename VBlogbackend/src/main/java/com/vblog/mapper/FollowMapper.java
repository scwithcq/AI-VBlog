package com.vblog.mapper;

import com.vblog.entity.Follow;

import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Param;

/**
 * Follow Mapper Interface
 *
 * @author V-Blog Team
 */
public interface FollowMapper {

    /**
     * Select by id
     *
     * @param followId follow id
     * @return follow
     */
    Follow selectById(Long followId);

    /**
     * Select all
     *
     * @return follow list
     */
    List<Follow> selectAll();

    /**
     * Select by map
     *
     * @param map query conditions
     * @return follow list
     */
    List<Follow> selectByMap(Map<String, Object> map);

    /**
     * Insert
     *
     * @param follow follow
     * @return affected rows
     */
    int insert(Follow follow);

    /**
     * Delete by id
     *
     * @param followId follow id
     * @return affected rows
     */
    int deleteById(Long followId);

    /**
     * Delete by user ids
     *
     * @param followerId follower id
     * @param followeeId followee id
     * @return affected rows
     */
    int deleteByUserIds(Long followerId, Long followeeId);

    List<Long> selectFollowerIds(@Param("userId") Long userId);

    List<Long> selectFollowingIds(@Param("userId") Long userId);

    boolean isMutualFriend(@Param("userId") Long userId, @Param("otherUserId") Long otherUserId);

    boolean existsByUsers(@Param("followerId") Long followerId, @Param("followeeId") Long followeeId);

}

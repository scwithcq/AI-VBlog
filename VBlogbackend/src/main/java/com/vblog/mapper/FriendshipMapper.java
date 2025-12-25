package com.vblog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.vblog.entity.Friendship;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 好友关系Mapper接口
 * 
 * @author V-Blog团队
 */
@Mapper
public interface FriendshipMapper extends BaseMapper<Friendship> {

  /**
   * 查询用户的所有好友ID列表
   */
  List<Long> selectFriendIdsByUserId(@Param("userId") Long userId);

  /**
   * 检查是否为好友关系
   */
  Integer checkFriendship(@Param("userId") Long userId, @Param("friendId") Long friendId);

  /**
   * 批量插入好友关系（双向）
   */
  int insertBatch(@Param("list") List<Friendship> friendships);
}

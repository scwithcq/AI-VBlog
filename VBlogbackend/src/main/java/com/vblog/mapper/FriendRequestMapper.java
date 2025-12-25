package com.vblog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.vblog.entity.FriendRequest;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 好友申请Mapper接口
 * 
 * @author V-Blog团队
 */
@Mapper
public interface FriendRequestMapper extends BaseMapper<FriendRequest> {

  /**
   * 查询用户收到的好友申请
   */
  List<FriendRequest> selectReceivedRequests(@Param("userId") Long userId, @Param("status") Integer status);

  /**
   * 查询用户发送的好友申请
   */
  List<FriendRequest> selectSentRequests(@Param("userId") Long userId, @Param("status") Integer status);

  /**
   * 检查是否存在待处理的申请
   */
  Integer checkPendingRequest(@Param("fromUserId") Long fromUserId, @Param("toUserId") Long toUserId);
}

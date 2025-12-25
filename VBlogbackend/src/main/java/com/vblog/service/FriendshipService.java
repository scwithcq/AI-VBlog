package com.vblog.service;

import com.vblog.entity.FriendRequest;
import com.vblog.entity.Friendship;
import com.vblog.entity.User;

import java.util.List;

/**
 * 好友服务接口
 * 
 * @author V-Blog团队
 */
public interface FriendshipService {

  /**
   * 搜索用户（按用户名、昵称）
   * 
   * @param keyword       搜索关键词
   * @param currentUserId 当前用户ID
   * @return 用户列表
   */
  List<User> searchUsers(String keyword, Long currentUserId);

  /**
   * 发送好友申请
   * 
   * @param fromUserId 发起者用户ID
   * @param toUserId   接收者用户ID
   * @param message    申请留言
   * @return 好友申请记录
   */
  FriendRequest sendFriendRequest(Long fromUserId, Long toUserId, String message);

  /**
   * 处理好友申请（同意/拒绝）
   * 
   * @param requestId 申请ID
   * @param userId    当前用户ID
   * @param accept    是否同意
   * @return 处理结果
   */
  boolean handleFriendRequest(Long requestId, Long userId, boolean accept);

  /**
   * 获取收到的好友申请列表
   * 
   * @param userId 用户ID
   * @param status 申请状态（null表示全部）
   * @return 好友申请列表
   */
  List<FriendRequest> getReceivedRequests(Long userId, Integer status);

  /**
   * 获取发送的好友申请列表
   * 
   * @param userId 用户ID
   * @param status 申请状态（null表示全部）
   * @return 好友申请列表
   */
  List<FriendRequest> getSentRequests(Long userId, Integer status);

  /**
   * 获取好友列表（包含用户详细信息）
   * 
   * @param userId 用户ID
   * @return 好友列表
   */
  List<User> getFriends(Long userId);

  /**
   * 删除好友
   * 
   * @param userId   用户ID
   * @param friendId 好友ID
   * @return 删除结果
   */
  boolean deleteFriend(Long userId, Long friendId);

  /**
   * 修改好友备注
   * 
   * @param userId   用户ID
   * @param friendId 好友ID
   * @param remark   备注名称
   * @return 修改结果
   */
  boolean updateFriendRemark(Long userId, Long friendId, String remark);

  /**
   * 检查是否为好友
   * 
   * @param userId   用户ID
   * @param friendId 好友ID
   * @return 是否为好友
   */
  boolean isFriend(Long userId, Long friendId);

  /**
   * 获取好友数量
   * 
   * @param userId 用户ID
   * @return 好友数量
   */
  int getFriendCount(Long userId);
}

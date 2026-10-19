package com.vblog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.vblog.entity.FriendRequest;
import com.vblog.entity.Friendship;
import com.vblog.entity.User;
import com.vblog.mapper.FriendRequestMapper;
import com.vblog.mapper.FriendshipMapper;
import com.vblog.mapper.UserMapper;
import com.vblog.service.FriendshipService;
import com.vblog.service.NotificationService;
import com.vblog.websocket.MessageWebSocketHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 好友服务实现类
 *
 * @author V-Blog团队
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FriendshipServiceImpl implements FriendshipService {

  private final FriendshipMapper friendshipMapper;
  private final FriendRequestMapper friendRequestMapper;
  private final UserMapper userMapper;
  private final NotificationService notificationService;
  private final MessageWebSocketHandler webSocketHandler;

  private static final int MAX_FRIEND_COUNT = 1000;

  @Override
  public List<User> searchUsers(String keyword, Long currentUserId) {
    if (keyword == null || keyword.trim().isEmpty()) {
      return new ArrayList<>();
    }

    // 搜索用户名或昵称匹配的用户，排除当前用户
    List<User> users = userMapper.searchUsers(keyword.trim(), currentUserId);
    users = users.stream().map(com.vblog.utils.InputSafety::publicUser).toList();
    return users;
  }

  @Override
  @Transactional
  public FriendRequest sendFriendRequest(Long fromUserId, Long toUserId, String message) {
    // 检查参数
    if (fromUserId.equals(toUserId)) {
      throw new RuntimeException("不能添加自己为好友");
    }

    // 检查目标用户是否存在
    User toUser = userMapper.selectById(toUserId);
    if (toUser == null) {
      throw new RuntimeException("用户不存在");
    }

    // 检查是否已经是好友
    if (isFriend(fromUserId, toUserId)) {
      throw new RuntimeException("对方已经是你的好友");
    }

    // 检查是否存在待处理的申请
    Integer pending = friendRequestMapper.checkPendingRequest(fromUserId, toUserId);
    if (pending != null && pending > 0) {
      throw new RuntimeException("已存在待处理的好友申请");
    }

    // 检查好友数量限制
    int friendCount = getFriendCount(fromUserId);
    if (friendCount >= MAX_FRIEND_COUNT) {
      throw new RuntimeException("好友数量已达上限");
    }

    // 创建好友申请
    FriendRequest request = new FriendRequest();
    request.setFromUserId(fromUserId);
    request.setToUserId(toUserId);
    request.setMessage(message);
    request.setStatus(0);

    friendRequestMapper.insert(request);
    webSocketHandler.sendEventAfterCommit(toUserId, "friend_request", request);

    // 暂时禁用通知功能，等好友功能测试通过后再启用
    // try {
    // notificationService.createNotification(
    // toUserId,
    // fromUserId,
    // 4, // 好友申请通知类型
    // request.getRequestId(),
    // "收到新的好友申请");
    // } catch (Exception e) {
    // log.warn("发送好友申请通知失败，但申请已创建成功: {}", e.getMessage());
    // }

    log.info("用户 {} 向用户 {} 发送好友申请", fromUserId, toUserId);
    return request;
  }

  @Override
  @Transactional
  public boolean handleFriendRequest(Long requestId, Long userId, boolean accept) {
    // 查询申请记录
    FriendRequest request = friendRequestMapper.selectById(requestId);
    if (request == null) {
      throw new RuntimeException("申请记录不存在");
    }

    // 验证权限
    if (!request.getToUserId().equals(userId)) {
      throw new RuntimeException("无权处理该申请");
    }

    // 检查申请状态
    if (request.getStatus() != 0) {
      throw new RuntimeException("该申请已被处理");
    }

    // 更新申请状态
    request.setStatus(accept ? 1 : 2);
    friendRequestMapper.updateById(request);

    // 如果同意，创建双向好友关系
    if (accept) {
      // 检查好友数量限制
      int count1 = getFriendCount(request.getFromUserId());
      int count2 = getFriendCount(request.getToUserId());
      if (count1 >= MAX_FRIEND_COUNT || count2 >= MAX_FRIEND_COUNT) {
        throw new RuntimeException("好友数量已达上限");
      }

      List<Friendship> friendships = new ArrayList<>();

      Friendship friendship1 = new Friendship();
      friendship1.setUserId(request.getFromUserId());
      friendship1.setFriendId(request.getToUserId());
      friendship1.setStatus(1);
      friendships.add(friendship1);

      Friendship friendship2 = new Friendship();
      friendship2.setUserId(request.getToUserId());
      friendship2.setFriendId(request.getFromUserId());
      friendship2.setStatus(1);
      friendships.add(friendship2);

      friendshipMapper.insertBatch(friendships);
      webSocketHandler.sendEventAfterCommit(request.getFromUserId(), "friend_accepted", request);
      webSocketHandler.sendEventAfterCommit(request.getToUserId(), "friend_accepted", request);

      // 暂时禁用通知功能，等好友功能测试通过后再启用
      // try {
      // notificationService.createNotification(
      // request.getFromUserId(),
      // request.getToUserId(),
      // 4,
      // requestId,
      // "好友申请已通过");
      // } catch (Exception e) {
      // log.warn("发送好友申请通过通知失败，但好友关系已建立成功: {}", e.getMessage());
      // }

      log.info("用户 {} 和用户 {} 成为好友", request.getFromUserId(), request.getToUserId());
    }

    return true;
  }

  @Override
  public List<FriendRequest> getReceivedRequests(Long userId, Integer status) {
    return friendRequestMapper.selectReceivedRequests(userId, status);
  }

  @Override
  public List<FriendRequest> getSentRequests(Long userId, Integer status) {
    return friendRequestMapper.selectSentRequests(userId, status);
  }

  @Override
  public List<User> getFriends(Long userId) {
    // 获取好友ID列表
    List<Long> friendIds = friendshipMapper.selectFriendIdsByUserId(userId);
    if (friendIds.isEmpty()) {
      return new ArrayList<>();
    }

    // 使用QueryWrapper批量查询好友信息
    QueryWrapper<User> wrapper = new QueryWrapper<>();
    wrapper.in("user_id", friendIds);
    List<User> friends = userMapper.selectList(wrapper);
    friends = friends.stream().map(com.vblog.utils.InputSafety::publicUser).toList();
    return friends;
  }

  @Override
  @Transactional
  public boolean deleteFriend(Long userId, Long friendId) {
    // 删除双向好友关系
    LambdaQueryWrapper<Friendship> wrapper1 = new LambdaQueryWrapper<>();
    wrapper1.eq(Friendship::getUserId, userId)
        .eq(Friendship::getFriendId, friendId);

    LambdaQueryWrapper<Friendship> wrapper2 = new LambdaQueryWrapper<>();
    wrapper2.eq(Friendship::getUserId, friendId)
        .eq(Friendship::getFriendId, userId);

    int count1 = friendshipMapper.delete(wrapper1);
    int count2 = friendshipMapper.delete(wrapper2);

    log.info("用户 {} 删除好友 {}", userId, friendId);
    return count1 > 0 || count2 > 0;
  }

  @Override
  public boolean updateFriendRemark(Long userId, Long friendId, String remark) {
    LambdaQueryWrapper<Friendship> wrapper = new LambdaQueryWrapper<>();
    wrapper.eq(Friendship::getUserId, userId)
        .eq(Friendship::getFriendId, friendId)
        .eq(Friendship::getStatus, 1);

    Friendship friendship = friendshipMapper.selectOne(wrapper);
    if (friendship == null) {
      return false;
    }

    friendship.setRemark(remark);
    return friendshipMapper.updateById(friendship) > 0;
  }

  @Override
  public boolean isFriend(Long userId, Long friendId) {
    Integer count = friendshipMapper.checkFriendship(userId, friendId);
    return count != null && count > 0;
  }

  @Override
  public int getFriendCount(Long userId) {
    LambdaQueryWrapper<Friendship> wrapper = new LambdaQueryWrapper<>();
    wrapper.eq(Friendship::getUserId, userId)
        .eq(Friendship::getStatus, 1);
    return friendshipMapper.selectCount(wrapper).intValue();
  }
}

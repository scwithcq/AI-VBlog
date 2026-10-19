package com.vblog.service.impl;

import com.vblog.common.Page;
import com.vblog.entity.SystemNotification;
import com.vblog.entity.User;
import com.vblog.mapper.SystemNotificationMapper;
import com.vblog.service.SystemNotificationService;
import com.vblog.service.UserService;
import com.vblog.websocket.MessageWebSocketHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 系统互动通知服务实现类
 *
 * @author V-Blog团队
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SystemNotificationServiceImpl implements SystemNotificationService {

  private final SystemNotificationMapper notificationMapper;
  private final UserService userService;
  private final MessageWebSocketHandler webSocketHandler;

  @Override
  @Transactional
  public SystemNotification createNotification(SystemNotification notification) {
    // 避免给自己发通知
    if (notification.getRecipientId().equals(notification.getSenderId())) {
      return null;
    }

    // 如果没有提供发送者信息，从数据库查询
    if (notification.getSenderUsername() == null || notification.getSenderAvatar() == null) {
      User sender = userService.getUserById(notification.getSenderId());
      if (sender != null) {
        notification.setSenderUsername(sender.getUsername());
        notification.setSenderAvatar(sender.getAvatar());
      }
    }

    notification.setIsRead(0);
    notification.setCreateTime(java.time.LocalDateTime.now());
    notificationMapper.insert(notification);
    webSocketHandler.sendEventAfterCommit(notification.getRecipientId(), "system_notification", notification);
    log.info("Created notification: type={}, recipient={}, sender={}",
        notification.getType(), notification.getRecipientId(), notification.getSenderId());
    return notification;
  }

  @Override
  public void createLikeNotification(Long recipientId, Long senderId, Long postId) {
    User sender = userService.getUserById(senderId);
    if (sender == null) {
      return;
    }

    SystemNotification notification = new SystemNotification();
    notification.setRecipientId(recipientId);
    notification.setSenderId(senderId);
    notification.setSenderUsername(sender.getUsername());
    notification.setSenderAvatar(sender.getAvatar());
    notification.setType(SystemNotification.NotificationType.LIKE.name());
    notification.setPostId(postId);
    notification.setContent(sender.getUsername() + " 赞了你的帖子");

    createNotification(notification);
  }

  @Override
  public void createCommentNotification(Long recipientId, Long senderId, Long postId, Long commentId, String content) {
    User sender = userService.getUserById(senderId);
    if (sender == null) {
      return;
    }

    SystemNotification notification = new SystemNotification();
    notification.setRecipientId(recipientId);
    notification.setSenderId(senderId);
    notification.setSenderUsername(sender.getUsername());
    notification.setSenderAvatar(sender.getAvatar());
    notification.setType(SystemNotification.NotificationType.COMMENT.name());
    notification.setPostId(postId);
    notification.setCommentId(commentId);

    // 截取内容预览
    String preview = content.length() > 50 ? content.substring(0, 50) + "..." : content;
    notification.setContent(sender.getUsername() + " 评论了你: " + preview);

    createNotification(notification);
  }

  @Override
  public void createFollowNotification(Long recipientId, Long senderId) {
    User sender = userService.getUserById(senderId);
    if (sender == null) {
      return;
    }

    SystemNotification notification = new SystemNotification();
    notification.setRecipientId(recipientId);
    notification.setSenderId(senderId);
    notification.setSenderUsername(sender.getUsername());
    notification.setSenderAvatar(sender.getAvatar());
    notification.setType(SystemNotification.NotificationType.FOLLOW.name());
    notification.setContent(sender.getUsername() + " 关注了你");

    createNotification(notification);
  }

  @Override
  public void createMentionNotification(Long recipientId, Long senderId, Long postId, String content) {
    User sender = userService.getUserById(senderId);
    if (sender == null) {
      return;
    }

    SystemNotification notification = new SystemNotification();
    notification.setRecipientId(recipientId);
    notification.setSenderId(senderId);
    notification.setSenderUsername(sender.getUsername());
    notification.setSenderAvatar(sender.getAvatar());
    notification.setType(SystemNotification.NotificationType.MENTION.name());
    notification.setPostId(postId);

    String preview = content.length() > 50 ? content.substring(0, 50) + "..." : content;
    notification.setContent(sender.getUsername() + " 在帖子中提到了你: " + preview);

    createNotification(notification);
  }

  @Override
  public Page<SystemNotification> getNotificationsByRecipient(Long recipientId, int page, int size) {
    Map<String, Object> params = new HashMap<>();
    params.put("recipientId", recipientId);

    List<SystemNotification> allNotifications = notificationMapper.selectByMap(params);

    // 按创建时间倒序排序（null值放在最后）
    allNotifications.sort(Comparator.comparing(SystemNotification::getCreateTime,
        Comparator.nullsLast(Comparator.naturalOrder())).reversed());

    // 手动分页
    long total = allNotifications.size();
    int offset = (page - 1) * size;
    int end = Math.min(offset + size, allNotifications.size());

    List<SystemNotification> list = offset < allNotifications.size()
        ? allNotifications.subList(offset, end)
        : Collections.emptyList();

    return new Page<>(page, size, total, list);
  }

  @Override
  public Page<SystemNotification> getUnreadNotifications(Long recipientId, int page, int size) {
    Map<String, Object> params = new HashMap<>();
    params.put("recipientId", recipientId);
    params.put("isRead", 0);

    List<SystemNotification> allNotifications = notificationMapper.selectByMap(params);

    // 按创建时间倒序排序（null值放在最后）
    allNotifications.sort(Comparator.comparing(SystemNotification::getCreateTime,
        Comparator.nullsLast(Comparator.naturalOrder())).reversed());

    // 手动分页
    long total = allNotifications.size();
    int offset = (page - 1) * size;
    int end = Math.min(offset + size, allNotifications.size());

    List<SystemNotification> list = offset < allNotifications.size()
        ? allNotifications.subList(offset, end)
        : Collections.emptyList();

    return new Page<>(page, size, total, list);
  }

  @Override
  public Page<SystemNotification> getNotificationsByType(Long recipientId, String type, int page, int size) {
    Map<String, Object> params = new HashMap<>();
    params.put("recipientId", recipientId);
    params.put("type", type);

    List<SystemNotification> allNotifications = notificationMapper.selectByMap(params);

    // 按创建时间倒序排序（null值放在最后）
    allNotifications.sort(Comparator.comparing(SystemNotification::getCreateTime,
        Comparator.nullsLast(Comparator.naturalOrder())).reversed());

    // 手动分页
    long total = allNotifications.size();
    int offset = (page - 1) * size;
    int end = Math.min(offset + size, allNotifications.size());

    List<SystemNotification> list = offset < allNotifications.size()
        ? allNotifications.subList(offset, end)
        : Collections.emptyList();

    return new Page<>(page, size, total, list);
  }

  @Override
  public int getUnreadCount(Long recipientId) {
    return notificationMapper.countUnread(recipientId);
  }

  @Override
  public Map<String, Integer> getUnreadCountByType(Long recipientId) {
    Map<String, Integer> result = new HashMap<>();

    for (SystemNotification.NotificationType type : SystemNotification.NotificationType.values()) {
      int count = notificationMapper.countUnreadByType(recipientId, type.name());
      result.put(type.name(), count);
    }

    return result;
  }

  @Override
  @Transactional
  public boolean markAsRead(Long notificationId, Long userId) {
    SystemNotification notification = notificationMapper.selectById(notificationId);
    if (notification == null) {
      throw new IllegalArgumentException("通知不存在");
    }

    // 验证权限
    if (!notification.getRecipientId().equals(userId)) {
      throw new IllegalArgumentException("无权操作此通知");
    }

    if (Integer.valueOf(1).equals(notification.getIsRead())) return true;
    notification.setIsRead(1);
    return notificationMapper.update(notification) > 0;
  }

  @Override
  @Transactional
  public boolean markAsReadBatch(List<Long> notificationIds, Long userId) {
    if (notificationIds == null || notificationIds.isEmpty()) {
      return false;
    }

    // 验证所有通知都属于该用户
    for (Long id : notificationIds) {
      SystemNotification notification = notificationMapper.selectById(id);
      if (notification != null && !notification.getRecipientId().equals(userId)) {
        throw new RuntimeException("无权操作此通知");
      }
    }

    return notificationMapper.markAsReadByIds(notificationIds) > 0;
  }

  @Override
  @Transactional
  public boolean markAllAsRead(Long recipientId) {
    notificationMapper.markAsReadByRecipient(recipientId, null);
    return true;
  }

  @Override
  @Transactional
  public boolean markAllAsReadByType(Long recipientId, String type) {
    notificationMapper.markAsReadByRecipient(recipientId, type);
    return true;
  }

  @Override
  @Transactional
  public boolean deleteNotification(Long notificationId, Long userId) {
    SystemNotification notification = notificationMapper.selectById(notificationId);
    if (notification == null) {
      throw new RuntimeException("通知不存在");
    }

    // 验证权限
    if (!notification.getRecipientId().equals(userId)) {
      throw new RuntimeException("无权操作此通知");
    }

    return notificationMapper.deleteById(notificationId) > 0;
  }

  @Override
  @Transactional
  public int cleanupReadNotifications(int days) {
    return notificationMapper.deleteReadNotificationsBeforeDays(days);
  }
}

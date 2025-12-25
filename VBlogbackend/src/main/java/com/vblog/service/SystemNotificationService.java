package com.vblog.service;

import com.vblog.common.Page;
import com.vblog.entity.SystemNotification;

import java.util.List;
import java.util.Map;

/**
 * 系统互动通知服务接口
 * 
 * @author V-Blog团队
 */
public interface SystemNotificationService {

  /**
   * 创建通知
   *
   * @param notification 通知对象
   * @return 创建的通知
   */
  SystemNotification createNotification(SystemNotification notification);

  /**
   * 创建点赞通知
   *
   * @param recipientId 接收者ID
   * @param senderId    发送者ID
   * @param postId      帖子ID
   */
  void createLikeNotification(Long recipientId, Long senderId, Long postId);

  /**
   * 创建评论通知
   *
   * @param recipientId 接收者ID
   * @param senderId    发送者ID
   * @param postId      帖子ID
   * @param commentId   评论ID
   * @param content     评论内容
   */
  void createCommentNotification(Long recipientId, Long senderId, Long postId, Long commentId, String content);

  /**
   * 创建关注通知
   *
   * @param recipientId 接收者ID（被关注者）
   * @param senderId    发送者ID（关注者）
   */
  void createFollowNotification(Long recipientId, Long senderId);

  /**
   * 创建提及通知
   *
   * @param recipientId 接收者ID
   * @param senderId    发送者ID
   * @param postId      帖子ID
   * @param content     内容
   */
  void createMentionNotification(Long recipientId, Long senderId, Long postId, String content);

  /**
   * 获取用户的通知列表（分页）
   *
   * @param recipientId 接收者ID
   * @param page        页码
   * @param size        每页大小
   * @return 通知分页对象
   */
  Page<SystemNotification> getNotificationsByRecipient(Long recipientId, int page, int size);

  /**
   * 获取用户的未读通知列表
   *
   * @param recipientId 接收者ID
   * @param page        页码
   * @param size        每页大小
   * @return 通知分页对象
   */
  Page<SystemNotification> getUnreadNotifications(Long recipientId, int page, int size);

  /**
   * 根据类型获取通知列表
   *
   * @param recipientId 接收者ID
   * @param type        通知类型
   * @param page        页码
   * @param size        每页大小
   * @return 通知分页对象
   */
  Page<SystemNotification> getNotificationsByType(Long recipientId, String type, int page, int size);

  /**
   * 获取未读通知数
   *
   * @param recipientId 接收者ID
   * @return 未读通知数
   */
  int getUnreadCount(Long recipientId);

  /**
   * 获取各类型未读通知数统计
   *
   * @param recipientId 接收者ID
   * @return 各类型未读通知数
   */
  Map<String, Integer> getUnreadCountByType(Long recipientId);

  /**
   * 标记通知为已读
   *
   * @param notificationId 通知ID
   * @param userId         用户ID（验证权限）
   * @return 是否成功
   */
  boolean markAsRead(Long notificationId, Long userId);

  /**
   * 批量标记为已读
   *
   * @param notificationIds 通知ID列表
   * @param userId          用户ID（验证权限）
   * @return 是否成功
   */
  boolean markAsReadBatch(List<Long> notificationIds, Long userId);

  /**
   * 标记所有通知为已读
   *
   * @param recipientId 接收者ID
   * @return 是否成功
   */
  boolean markAllAsRead(Long recipientId);

  /**
   * 标记某类型所有通知为已读
   *
   * @param recipientId 接收者ID
   * @param type        通知类型
   * @return 是否成功
   */
  boolean markAllAsReadByType(Long recipientId, String type);

  /**
   * 删除通知
   *
   * @param notificationId 通知ID
   * @param userId         用户ID（验证权限）
   * @return 是否成功
   */
  boolean deleteNotification(Long notificationId, Long userId);

  /**
   * 清理旧的已读通知
   *
   * @param days 保留天数
   * @return 清理数量
   */
  int cleanupReadNotifications(int days);
}

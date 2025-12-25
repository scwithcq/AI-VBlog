package com.vblog.service;

import com.vblog.common.Page;
import com.vblog.entity.Notification;

import java.util.List;

/**
 * 通知服务接口
 *
 * @author V-Blog团队
 */
public interface NotificationService {

    /**
     * 获取用户的通知列表
     *
     * @param userId 用户ID
     * @param page   页码
     * @param size   每页大小
     * @return 分页的通知列表
     */
    Page<Notification> getNotificationList(Long userId, int page, int size);

    /**
     * 获取用户未读通知数量
     *
     * @param userId 用户ID
     * @return 未读通知数量
     */
    Long getUnreadCount(Long userId);

    
    /**
     * 标记通知为已读
     *
     * @param notificationId 通知ID
     * @param userId         用户ID（确保是通知接收者）
     * @return 是否标记成功
     */
    boolean markAsRead(Long notificationId, Long userId);

    /**
     * 标记所有通知为已读
     *
     * @param userId 用户ID
     * @return 影响的行数
     */
    int markAllAsRead(Long userId);

    /**
     * 删除通知
     *
     * @param notificationId 通知ID
     * @param userId         用户ID（确保是通知接收者）
     * @return 是否删除成功
     */
    boolean deleteNotification(Long notificationId, Long userId);

    /**
     * 发送关注通知
     *
     * @param followerId 关注者ID
     * @param followeeId 被关注者ID
     */
    void sendFollowNotification(Long followerId, Long followeeId);

    /**
     * 发送点赞通知
     *
     * @param userId      点赞用户ID
     * @param postId      帖子ID
     * @param recipientId 接收通知的用户ID（帖子作者）
     */
    void sendLikeNotification(Long userId, Long postId, Long recipientId);

    /**
     * 发送评论通知
     *
     * @param userId      评论用户ID
     * @param postId      帖子ID
     * @param commentId   评论ID
     * @param recipientId 接收通知的用户ID（帖子作者或被回复的评论作者）
     */
    void sendCommentNotification(Long userId, Long postId, Long commentId, Long recipientId);

    /**
     * 创建通知（通用方法）
     *
     * @param recipientId 接收者ID
     * @param senderId    发送者ID
     * @param type        通知类型（1-关注，2-点赞，3-评论，4-好友申请，5-消息）
     * @param targetId    目标ID（帖子ID、评论ID、申请ID、消息ID等）
     * @param content     通知内容
     */
    void createNotification(Long recipientId, Long senderId, Integer type, Long targetId, String content);
}

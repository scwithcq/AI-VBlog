package com.vblog.service.impl;

import com.vblog.common.Page;
import com.vblog.entity.Notification;
import com.vblog.mapper.NotificationMapper;
import com.vblog.service.NotificationService;
import com.vblog.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 通知服务实现类
 *
 * @author V-Blog团队
 */
@Slf4j
@Service
public class NotificationServiceImpl implements NotificationService {

    private final NotificationMapper notificationMapper;
    private UserService userService;

    public NotificationServiceImpl(NotificationMapper notificationMapper) {
        this.notificationMapper = notificationMapper;
    }

    @Autowired
    @Lazy
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @Override
    public Page<Notification> getNotificationList(Long userId, int page, int size) {
        // 构建查询条件
        Map<String, Object> queryMap = new HashMap<>();
        queryMap.put("recipient_id", userId);

        // 获取所有通知
        List<Notification> notifications = notificationMapper.selectByMap(queryMap);

        // 按创建时间倒序排序
        notifications.sort(Comparator.comparing(Notification::getCreateTime).reversed());

        // 计算总数和分页
        long total = notifications.size();
        int offset = (page - 1) * size;
        int end = Math.min(offset + size, notifications.size());
        List<Notification> paginatedNotifications = notifications.subList(offset, end);

        return new Page<>(page, size, total, paginatedNotifications);
    }

    @Override
    public Long getUnreadCount(Long userId) {
        return notificationMapper.getUnreadCount(userId);
    }

    @Override
    @Transactional
    public boolean markAsRead(Long notificationId, Long userId) {
        // 检查通知是否存在且属于该用户
        Notification notification = notificationMapper.selectById(notificationId);
        if (notification == null || !notification.getRecipientId().equals(userId)) {
            return false;
        }

        // 更新为已读
        notification.setIsRead(1);
        int result = notificationMapper.update(notification);
        return result > 0;
    }

    @Override
    @Transactional
    public int markAllAsRead(Long userId) {
        return notificationMapper.markAllAsRead(userId);
    }

    @Override
    @Transactional
    public boolean deleteNotification(Long notificationId, Long userId) {
        // 检查通知是否存在且属于该用户
        Notification notification = notificationMapper.selectById(notificationId);
        if (notification == null || !notification.getRecipientId().equals(userId)) {
            return false;
        }

        // 删除通知
        int result = notificationMapper.deleteById(notificationId);
        return result > 0;
    }

    @Override
    @Transactional
    public void sendFollowNotification(Long followerId, Long followeeId) {
        // 不要给自己发通知
        if (followerId.equals(followeeId)) {
            return;
        }

        // 获取关注者信息
        String followerName = userService.getUserById(followerId).getUsername();

        // 创建通知
        Notification notification = new Notification();
        notification.setRecipientId(followeeId);
        notification.setSenderId(followerId);
        notification.setType(1); // 关注类型
        notification.setContent(followerName + " 关注了你");
        notification.setIsRead(0); // 未读
        notification.setCreateTime(LocalDateTime.now());

        notificationMapper.insert(notification);
        log.info("Follow notification sent: followerId={}, followeeId={}", followerId, followeeId);
    }

    @Override
    @Transactional
    public void sendLikeNotification(Long userId, Long postId, Long recipientId) {
        // 不要给自己发通知
        if (userId.equals(recipientId)) {
            return;
        }

        // 获取点赞者信息
        String userName = userService.getUserById(userId).getUsername();

        // 创建通知
        Notification notification = new Notification();
        notification.setRecipientId(recipientId);
        notification.setSenderId(userId);
        notification.setType(2); // 点赞类型
        notification.setPostId(postId);
        notification.setContent(userName + " 点赞了你的帖子");
        notification.setIsRead(0); // 未读
        notification.setCreateTime(LocalDateTime.now());

        notificationMapper.insert(notification);
        log.info("Like notification sent: userId={}, postId={}, recipientId={}", userId, postId, recipientId);
    }

    @Override
    @Transactional
    public void sendCommentNotification(Long userId, Long postId, Long commentId, Long recipientId) {
        // 不要给自己发通知
        if (userId.equals(recipientId)) {
            return;
        }

        // 获取评论者信息
        String userName = userService.getUserById(userId).getUsername();

        // 创建通知
        Notification notification = new Notification();
        notification.setRecipientId(recipientId);
        notification.setSenderId(userId);
        notification.setType(3); // 评论类型
        notification.setPostId(postId);
        notification.setCommentId(commentId);
        notification.setContent(userName + " 评论了你的帖子");
        notification.setIsRead(0); // 未读
        notification.setCreateTime(LocalDateTime.now());

        notificationMapper.insert(notification);
        log.info("Comment notification sent: userId={}, postId={}, commentId={}, recipientId={}",
                userId, postId, commentId, recipientId);
    }

    @Override
    @Transactional
    public void createNotification(Long recipientId, Long senderId, Integer type, Long targetId, String content) {
        // 创建通知
        Notification notification = new Notification();
        notification.setRecipientId(recipientId);
        notification.setSenderId(senderId);
        notification.setType(type);
        notification.setContent(content);
        notification.setIsRead(0); // 未读
        notification.setCreateTime(LocalDateTime.now()); // 设置创建时间

        // 根据类型设置目标ID
        switch (type) {
            case 1: // 关注
                break;
            case 2: // 点赞
                notification.setPostId(targetId);
                break;
            case 3: // 评论
                notification.setCommentId(targetId);
                break;
            case 4: // 好友申请
                // 可以添加一个requestId字段，这里暂时用postId字段存储
                notification.setPostId(targetId);
                break;
            case 5: // 消息
                // 可以添加一个messageId字段，这里暂时用commentId字段存储
                notification.setCommentId(targetId);
                break;
        }

        notificationMapper.insert(notification);
        log.info("Notification created: recipientId={}, senderId={}, type={}, targetId={}",
                recipientId, senderId, type, targetId);
    }
}

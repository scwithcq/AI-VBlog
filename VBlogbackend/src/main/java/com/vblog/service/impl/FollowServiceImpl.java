package com.vblog.service.impl;

import com.vblog.common.Page;
import com.vblog.entity.Follow;
import com.vblog.entity.User;
import com.vblog.mapper.FollowMapper;
import com.vblog.mapper.UserMapper;
import com.vblog.service.FollowService;
import com.vblog.service.NotificationService;
import com.vblog.service.SystemNotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 关注服务实现类
 *
 * @author V-Blog团队
 */
@Slf4j
@Service
public class FollowServiceImpl implements FollowService {

    private final FollowMapper followMapper;
    private final UserMapper userMapper;
    private NotificationService notificationService;
    private SystemNotificationService systemNotificationService;

    public FollowServiceImpl(FollowMapper followMapper, UserMapper userMapper) {
        this.followMapper = followMapper;
        this.userMapper = userMapper;
    }

    @Autowired
    @Lazy
    public void setNotificationService(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @Autowired
    @Lazy
    public void setSystemNotificationService(SystemNotificationService systemNotificationService) {
        this.systemNotificationService = systemNotificationService;
    }

    @Override
    @Transactional
    public boolean followUser(Long followerId, Long followeeId) {
        // 不能关注自己
        if (followerId.equals(followeeId)) {
            throw new RuntimeException("不能关注自己");
        }
        if (isFollowing(followerId, followeeId)) {
            return false;
        }

        // 创建新的关注关系
        Follow follow = new Follow();
        follow.setFollowerId(followerId);
        follow.setFolloweeId(followeeId);
        followMapper.insert(follow);
        log.info("用户 {} 关注了用户 {}", followerId, followeeId);

        // 发送关注通知
        notificationService.sendFollowNotification(followerId, followeeId);

        // 创建系统互动通知
        try {
            systemNotificationService.createFollowNotification(followeeId, followerId);
        } catch (Exception e) {
            log.error("Failed to create follow notification", e);
        }

        return true;
    }

    @Override
    @Transactional
    public boolean unfollowUser(Long followerId, Long followeeId) {
        if (isMutualFriend(followerId, followeeId)) {
            return false;
        }
        // 删除关注关系
        int result = followMapper.deleteByUserIds(followerId, followeeId);

        if (result > 0) {
            log.info("用户 {} 取消关注了用户 {}", followerId, followeeId);
            return true;
        } else {
            log.debug("用户 {} 没有关注用户 {}", followerId, followeeId);
            return false; // 没有关注
        }
    }

    @Override
    public boolean isFollowing(Long followerId, Long followeeId) {
        if (isMutualFriend(followerId, followeeId)) {
            return true;
        }
        return followMapper.existsByUsers(followerId, followeeId);
    }

    @Override
    public boolean isMutualFriend(Long userId, Long otherUserId) {
        return followMapper.isMutualFriend(userId, otherUserId);
    }

    @Override
    public Long getFollowerCount(Long userId) {
        return (long) followMapper.selectFollowerIds(userId).size();
    }

    @Override
    public Long getFollowingCount(Long userId) {
        return (long) followMapper.selectFollowingIds(userId).size();
    }

    @Override
    public Page<User> getFollowers(Long userId, int page, int size) {
        // 获取粉丝ID列表
        List<Long> followerIds = followMapper.selectFollowerIds(userId);

        // 计算总数和分页
        long total = followerIds.size();
        int offset = (page - 1) * size;
        int end = Math.min(offset + size, followerIds.size());

        if (offset >= end) {
            return new Page<>(page, size, total, List.of());
        }

        // 获取分页后的粉丝列表
        List<Long> paginatedFollowers = followerIds.subList(offset, end);

        // 获取每个粉丝的用户信息
        List<User> userList = paginatedFollowers.stream()
                .map(userMapper::selectById)
                .filter(user -> user != null)
                .map(com.vblog.utils.InputSafety::publicUser)
                .collect(Collectors.toList());

        // 创建并返回Page对象
        return new Page<>(page, size, total, userList);
    }

    @Override
    public Page<User> getFollowing(Long userId, int page, int size) {
        // 获取关注的用户ID列表
        List<Long> followingIds = followMapper.selectFollowingIds(userId);

        // 计算总数和分页
        long total = followingIds.size();
        int offset = (page - 1) * size;
        int end = Math.min(offset + size, followingIds.size());

        if (offset >= end) {
            return new Page<>(page, size, total, List.of());
        }

        // 获取分页后的关注用户列表
        List<Long> paginatedFollowing = followingIds.subList(offset, end);

        // 获取每个关注用户的信息
        List<User> userList = paginatedFollowing.stream()
                .map(userMapper::selectById)
                .filter(user -> user != null)
                .map(com.vblog.utils.InputSafety::publicUser)
                .collect(Collectors.toList());

        // 创建并返回Page对象
        return new Page<>(page, size, total, userList);
    }
}

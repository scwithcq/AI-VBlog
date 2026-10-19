package com.vblog.service;

import com.vblog.common.Page;
import com.vblog.entity.User;

import java.util.List;

/**
 * Follow Service Interface
 *
 * @author V-Blog Team
 */
public interface FollowService {

    /**
     * Follow a user
     *
     * @param followerId Follower ID (current user)
     * @param followeeId Followee ID (user to follow)
     * @return true if followed, false if already following
     */
    boolean followUser(Long followerId, Long followeeId);

    /**
     * Unfollow a user
     *
     * @param followerId Follower ID (current user)
     * @param followeeId Followee ID (user to unfollow)
     * @return true if unfollowed, false if not following
     */
    boolean unfollowUser(Long followerId, Long followeeId);

    /**
     * Check if user is following another user
     *
     * @param followerId Follower ID
     * @param followeeId Followee ID
     * @return true if following, false otherwise
     */
    boolean isFollowing(Long followerId, Long followeeId);

    boolean isMutualFriend(Long userId, Long otherUserId);

    /**
     * Get follower count for a user
     *
     * @param userId User ID
     * @return Follower count
     */
    Long getFollowerCount(Long userId);

    /**
     * Get following count for a user
     *
     * @param userId User ID
     * @return Following count
     */
    Long getFollowingCount(Long userId);

    /**
     * Get followers list (paginated)
     *
     * @param userId User ID
     * @param page Page number
     * @param size Page size
     * @return Page of followers
     */
    Page<User> getFollowers(Long userId, int page, int size);

    /**
     * Get following list (paginated)
     *
     * @param userId User ID
     * @param page Page number
     * @param size Page size
     * @return Page of following users
     */
    Page<User> getFollowing(Long userId, int page, int size);
}

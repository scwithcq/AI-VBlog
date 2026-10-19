package com.vblog;

import com.vblog.entity.Post;
import com.vblog.entity.User;
import com.vblog.mapper.PostFavoriteMapper;
import com.vblog.service.FollowService;
import com.vblog.service.PostLikeService;
import com.vblog.service.PostService;
import com.vblog.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@EnabledIfEnvironmentVariable(named = "VBLOG_INTEGRATION_DB", matches = "vblog_e2e_20261018")
class LocalFeatureIntegrationTest {
    @Autowired UserService users;
    @Autowired PostService posts;
    @Autowired PostLikeService likes;
    @Autowired PostFavoriteMapper favorites;
    @Autowired FollowService follows;
    @Autowired JdbcTemplate jdbc;
    @Autowired SqlSessionTemplate sqlSession;

    @Test
    @Transactional
    void registrationPostFavoriteLikeAndMutualFriendStayIndependent() {
        User alice = users.register("e2e_alice_20261018", "test-password");
        User bob = users.register("e2e_bob_20261018", "test-password");
        assertEquals("/default-avatar.jpg", alice.getAvatar());
        assertEquals("/default-avatar.jpg", users.getUserById(alice.getUserId()).getAvatar());

        Post first = posts.publishPost(alice.getUserId(), "first", null, null, null);
        Post second = posts.publishPost(alice.getUserId(), "second", null, null, null);
        assertEquals(0L, favorites.countByPostId(first.getPostId()));
        assertEquals(0L, likes.getLikeCount(first.getPostId()));
        assertEquals(2L, users.getUserStats(alice.getUserId()).postCount());

        favorites.add(first.getPostId(), bob.getUserId());
        assertEquals(1L, favorites.countByPostId(first.getPostId()));
        assertEquals(0L, favorites.countByPostId(second.getPostId()));
        assertEquals(0L, likes.getLikeCount(first.getPostId()));
        assertEquals(1L, favorites.countByUserId(bob.getUserId()));
        assertEquals(first.getPostId(), favorites.selectUserFavorites(bob.getUserId(), 0, 10).get(0).getPostId());

        assertTrue(likes.likePost(bob.getUserId(), second.getPostId()));
        assertEquals(1L, likes.getLikeCount(second.getPostId()));
        assertEquals(0L, likes.getLikeCount(first.getPostId()));
        assertEquals(1L, favorites.countByPostId(first.getPostId()));

        jdbc.update("INSERT INTO tb_friendship(user_id, friend_id, status) VALUES (?, ?, 1)", alice.getUserId(), bob.getUserId());
        assertFalse(follows.isMutualFriend(alice.getUserId(), bob.getUserId()));
        assertEquals(0L, follows.getFollowingCount(alice.getUserId()));
        jdbc.update("INSERT INTO tb_friendship(user_id, friend_id, status) VALUES (?, ?, 1)", bob.getUserId(), alice.getUserId());
        sqlSession.clearCache();
        assertTrue(follows.isMutualFriend(alice.getUserId(), bob.getUserId()));
        assertEquals(1L, follows.getFollowingCount(alice.getUserId()));
        assertEquals(1L, follows.getFollowerCount(alice.getUserId()));

        favorites.remove(first.getPostId(), bob.getUserId());
        assertEquals(0L, favorites.countByPostId(first.getPostId()));
        assertEquals(1L, likes.getLikeCount(second.getPostId()));
    }
}

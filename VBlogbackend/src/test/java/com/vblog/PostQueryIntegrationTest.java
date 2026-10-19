package com.vblog;

import com.vblog.entity.Post;
import com.vblog.entity.PostInteractionStatus;
import com.vblog.mapper.PostMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@EnabledIfEnvironmentVariable(named = "VBLOG_READONLY_DB_TEST", matches = "true")
class PostQueryIntegrationTest {
    @Autowired PostMapper posts;
    @Autowired JdbcTemplate jdbc;

    @Test
    void paginatedPostsCountsAndBatchStatusMatchDatabase() {
        long total = jdbc.queryForObject("SELECT COUNT(*) FROM tb_post", Long.class);
        assertEquals(total, posts.countAllPosts());
        List<Post> page = posts.selectPage(0, 10);
        assertEquals(Math.min(total, 10), page.size());
        if (page.isEmpty()) return;

        Post first = page.get(0);
        long userTotal = jdbc.queryForObject("SELECT COUNT(*) FROM tb_post WHERE user_id = ?",
                Long.class, first.getUserId());
        assertEquals(userTotal, posts.countByUserId(first.getUserId()));
        assertEquals(first.getPostId(), posts.selectUserPage(first.getUserId(), 0, 10).get(0).getPostId());

        List<PostInteractionStatus> statuses = posts.selectInteractionStatuses(
                first.getUserId(), page.stream().map(Post::getPostId).toList());
        assertEquals(page.size(), statuses.size());
        for (PostInteractionStatus status : statuses) {
            assertEquals(jdbc.queryForObject("SELECT COUNT(*) FROM tb_post_like WHERE post_id = ?",
                    Long.class, status.getPostId()), status.getLikeCount());
            assertEquals(jdbc.queryForObject("SELECT COUNT(*) FROM tb_post_favorite WHERE post_id = ?",
                    Long.class, status.getPostId()), status.getFavoriteCount());
        }
    }
}

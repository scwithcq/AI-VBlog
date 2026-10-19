package com.vblog;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vblog.entity.User;
import com.vblog.service.UserService;
import com.vblog.utils.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@EnabledIfEnvironmentVariable(named = "VBLOG_INTEGRATION_DB", matches = "vblog_e2e_20261018")
class PostInteractionRegressionTest {
    @Autowired MockMvc http;
    @Autowired ObjectMapper json;
    @Autowired UserService users;
    @Autowired JwtUtil jwt;
    @Autowired DataSource dataSource;
    User alice;
    User bob;
    String aliceToken;
    String bobToken;

    @BeforeEach
    void actors() throws Exception {
        try (var connection = dataSource.getConnection()) {
            assertTrue(connection.getMetaData().getURL().contains("/vblog_e2e_20261018"),
                    "Write regression tests must use the isolated schema");
        }
        alice = users.register("regression_alice", "local-test-password");
        bob = users.register("regression_bob", "local-test-password");
        aliceToken = jwt.generateToken(alice.getUserId(), alice.getUsername());
        bobToken = jwt.generateToken(bob.getUserId(), bob.getUsername());
    }

    JsonNode request(MockHttpServletRequestBuilder builder, String token) throws Exception {
        var response = http.perform(builder.header("Authorization", "Bearer " + token))
                .andReturn().getResponse();
        assertEquals(200, response.getStatus());
        return json.readTree(response.getContentAsString());
    }

    JsonNode success(MockHttpServletRequestBuilder builder, String token) throws Exception {
        JsonNode response = request(builder, token);
        assertEquals(0, response.path("code").asInt(), response.path("msg").asText());
        return response.path("data");
    }

    long publish(String content) throws Exception {
        return success(post("/post/publish").param("content", content), aliceToken)
                .path("postId").asLong();
    }

    @Test
    void eachPostKeepsItsOwnLikesAndBatchRouteIsNotParsedAsAnId() throws Exception {
        long first = publish("first");
        long second = publish("second");
        JsonNode initial = success(get("/post/interactions").param("postIds", first + "," + second), bobToken);
        assertEquals(2, initial.size());
        for (JsonNode status : initial) assertEquals(0, status.path("likeCount").asLong());

        assertEquals(1, success(post("/like/" + first), bobToken).path("likeCount").asLong());
        assertEquals(1, success(post("/like/" + first), bobToken).path("likeCount").asLong());
        assertEquals(0, success(get("/like/" + second + "/status"), bobToken).path("likeCount").asLong());
        assertEquals(2, success(post("/like/" + first), aliceToken).path("likeCount").asLong());
        assertEquals(1, success(post("/like/" + second), bobToken).path("likeCount").asLong());
        assertEquals(1, success(delete("/like/" + first), bobToken).path("likeCount").asLong());
        assertEquals(1, success(delete("/like/" + first), bobToken).path("likeCount").asLong());
        JsonNode statuses = success(get("/post/interactions").param("postIds", first + "," + second), bobToken);
        for (JsonNode status : statuses) {
            assertEquals(1, status.path("likeCount").asLong());
            assertEquals(status.path("postId").asLong() == second, status.path("liked").asBoolean());
        }
    }

    @Test
    void commentsRepliesAndCommentLikesCannotLeakAcrossPosts() throws Exception {
        long first = publish("first");
        long second = publish("second");
        long firstComment = success(post("/comment/add").param("postId", "" + first)
                .param("content", "only first"), bobToken).path("commentId").asLong();
        long secondComment = success(post("/comment/add").param("postId", "" + second)
                .param("content", "only second"), bobToken).path("commentId").asLong();
        success(post("/comment/add").param("postId", "" + first).param("parentId", "" + firstComment)
                .param("content", "first reply"), aliceToken);
        assertNotEquals(0, request(post("/comment/add").param("postId", "" + second)
                .param("parentId", "" + firstComment).param("content", "invalid cross post"), bobToken)
                .path("code").asInt());
        JsonNode firstTree = success(get("/comment/post/" + first), aliceToken);
        JsonNode secondTree = success(get("/comment/post/" + second), aliceToken);
        assertEquals(1, firstTree.size());
        assertEquals(firstComment, firstTree.get(0).path("comment").path("commentId").asLong());
        assertEquals(1, firstTree.get(0).path("replies").size());
        assertEquals(1, secondTree.size());
        assertEquals(secondComment, secondTree.get(0).path("comment").path("commentId").asLong());
        assertEquals(0, secondTree.get(0).path("replies").size());
        assertEquals(1, success(post("/comment/like/" + firstComment), aliceToken).asLong());
        assertEquals(1, success(post("/comment/like/" + firstComment), aliceToken).asLong());
        assertEquals(0, success(get("/comment/like/" + secondComment + "/status"), aliceToken)
                .path("likeCount").asLong());
        assertEquals(1, success(get("/comment/post/" + first), aliceToken).get(0)
                .path("comment").path("likeCount").asLong());
        success(delete("/comment/" + firstComment), bobToken);
        assertTrue(success(get("/comment/post/" + first), aliceToken).isEmpty());
        assertEquals(0, success(get("/comment/like/" + firstComment + "/status"), aliceToken)
                .path("likeCount").asLong());
        assertEquals(secondComment, success(get("/comment/post/" + second), aliceToken).get(0)
                .path("comment").path("commentId").asLong());
    }

    @Test
    void notificationTargetsAndRepeatedReadRemainValid() throws Exception {
        long postId = publish("notification target");
        success(post("/like/" + postId), bobToken);
        JsonNode list = success(get("/system-notification/list"), aliceToken).path("records");
        assertEquals(1, list.size());
        JsonNode notification = list.get(0);
        assertEquals(postId, notification.path("postId").asLong());
        assertEquals(bob.getUserId(), notification.path("senderId").asLong());
        assertEquals(postId, success(get("/post/" + postId), aliceToken).path("postId").asLong());
        assertEquals(bob.getUserId(), success(get("/user/" + bob.getUserId()), aliceToken).path("userId").asLong());
        String readPath = "/system-notification/" + notification.path("id").asLong() + "/read";
        success(put(readPath), aliceToken);
        success(put(readPath), aliceToken);
        assertEquals(0, success(get("/system-notification/unread/count"), aliceToken).asInt());
        success(put("/system-notification/read/all"), aliceToken);
        assertTrue(success(get("/system-notification/list"), bobToken).path("records").isEmpty());
    }

    @Test
    void mediaUploadPublicReadRangesAndValidation() throws Exception {
        String isolatedUploads = System.getenv("FILE_UPLOAD_PATH");
        assertNotNull(isolatedUploads, "FILE_UPLOAD_PATH must be an isolated directory under backend/target");
        assertTrue(java.nio.file.Path.of(isolatedUploads).toAbsolutePath().normalize()
                .startsWith(java.nio.file.Path.of("target").toAbsolutePath().normalize()), "Never upload test files into real media storage");
        byte[] image = java.nio.file.Files.readAllBytes(java.nio.file.Path.of("../image0001.jpg"));
        var file = new org.springframework.mock.web.MockMultipartFile("file", "avatar.jpg", "image/jpeg", image);
        String imageUrl = success(multipart("/file/upload/image").file(file), aliceToken).path("url").asText();
        var served = http.perform(get(imageUrl.substring(4))).andReturn().getResponse();
        assertEquals(200, served.getStatus());
        assertEquals("image/jpeg", served.getContentType());
        assertArrayEquals(image, served.getContentAsByteArray());
        String avatar = success(multipart("/user/avatar").file(file), aliceToken).path("avatar").asText();
        assertEquals(avatar, success(get("/user/info"), aliceToken).path("avatar").asText());
        var invalid = new org.springframework.mock.web.MockMultipartFile("file", "bad.txt", "text/plain", "bad".getBytes());
        assertNotEquals(0, request(multipart("/file/upload/image").file(invalid), aliceToken).path("code").asInt());
        assertNotEquals(0, request(multipart("/file/upload/video").file(file), aliceToken).path("code").asInt());
        java.nio.file.Path video;
        try (var files = java.nio.file.Files.list(java.nio.file.Path.of("../资源测试"))) {
            video = files.filter(f -> f.toString().endsWith(".mp4"))
                    .min(java.util.Comparator.comparingLong(f -> f.toFile().length())).orElseThrow();
        }
        byte[] videoBytes = java.nio.file.Files.readAllBytes(video);
        String videoUrl = success(multipart("/file/upload/video").file(new org.springframework.mock.web.MockMultipartFile(
                "file", "sample.mp4", "video/mp4", videoBytes)), aliceToken).path("url").asText();
        var range = http.perform(get(videoUrl.substring(4)).header("Range", "bytes=0-1023"))
                .andReturn().getResponse();
        assertEquals(206, range.getStatus());
        assertEquals("video/mp4", range.getContentType());
        assertEquals("bytes 0-1023/" + videoBytes.length, range.getHeader("Content-Range"));
        assertArrayEquals(java.util.Arrays.copyOf(videoBytes, 1024), range.getContentAsByteArray());
        assertEquals(416, http.perform(get(videoUrl.substring(4)).header("Range", "bytes=999999999-"))
                .andReturn().getResponse().getStatus());
        java.nio.file.Files.deleteIfExists(java.nio.file.Path.of(System.getenv("FILE_UPLOAD_PATH"), imageUrl.substring("/api/uploads/".length())));
        java.nio.file.Files.deleteIfExists(java.nio.file.Path.of(System.getenv("FILE_UPLOAD_PATH"), avatar.substring("/uploads/".length())));
        java.nio.file.Files.deleteIfExists(java.nio.file.Path.of(System.getenv("FILE_UPLOAD_PATH"), videoUrl.substring("/api/uploads/".length())));
    }

    @Test
    void profilePasswordTopicSearchFavoritesAndPostPermissions() throws Exception {
        String details = "{\"nickname\":\"Regression Nick\",\"gender\":1,\"email\":\"local@example.test\",\"phone\":\"13800000000\",\"location\":\"Test City\",\"website\":\"https://example.test\",\"bio\":\"saved bio\"}";
        success(put("/user/info").contentType("application/json").content(details), aliceToken);
        JsonNode profile = success(get("/user/info"), aliceToken);
        assertEquals("Regression Nick", profile.path("nickname").asText());
        assertEquals("local@example.test", profile.path("email").asText());
        assertEquals("Test City", profile.path("location").asText());
        success(put("/user/password").param("oldPassword", "local-test-password").param("newPassword", "changed-test-password"), aliceToken);
        assertEquals(401, http.perform(get("/user/info").header("Authorization", "Bearer " + aliceToken)).andReturn().getResponse().getStatus());
        aliceToken = success(post("/user/login").param("username", alice.getUsername()).param("password", "changed-test-password"), aliceToken).path("token").asText();
        long postId = publish("#regression-topic# searchable-regression");
        assertEquals(postId, success(get("/topic/name/regression-topic/posts"), bobToken).path("records").get(0).path("postId").asLong());
        assertEquals(postId, success(get("/search/posts").param("q", "searchable-regression"), bobToken).path("records").get(0).path("postId").asLong());
        for (JsonNode user : success(get("/search/users").param("q", "regression"), bobToken).path("records")) {
            assertTrue(user.path("password").isNull() || user.path("password").isMissingNode());
        }
        assertNotEquals(0, request(delete("/post/" + postId), bobToken).path("code").asInt());
        assertNotEquals(0, request(put("/post/" + postId).param("content", "unauthorized"), bobToken).path("code").asInt());
        success(put("/post/" + postId).param("content", "edited"), aliceToken);
        assertEquals("edited", success(get("/post/" + postId), bobToken).path("content").asText());
        assertEquals(1, success(post("/favorite/" + postId), bobToken).path("favoriteCount").asInt());
        assertEquals(1, success(post("/favorite/" + postId), bobToken).path("favoriteCount").asInt());
        assertEquals(1, success(get("/favorite/my"), bobToken).path("total").asInt());
        assertEquals(0, success(get("/favorite/my"), aliceToken).path("total").asInt());
        success(delete("/favorite/" + postId), bobToken);
        long repostId = success(post("/post/repost").param("sourceId", "" + postId), bobToken).path("postId").asLong();
        assertEquals(postId, success(get("/post/" + repostId + "/source"), bobToken).path("postId").asLong());
        success(delete("/post/" + postId), aliceToken);
        assertEquals(404, request(get("/post/" + postId), aliceToken).path("code").asInt());
        assertEquals(40001, request(get("/post/list").param("page", "0"), aliceToken).path("code").asInt());
    }

    @Test
    void friendshipChatUnreadRecallDeleteAndAccessControl() throws Exception {
        assertEquals(0, success(get("/friendship/friends"), aliceToken).size());
        assertNotEquals(0, request(post("/message/send").param("toUserId", "" + bob.getUserId()).param("content", "no friendship"), aliceToken).path("code").asInt());
        long id = success(post("/friendship/request").param("toUserId", "" + bob.getUserId()), aliceToken).path("requestId").asLong();
        success(put("/friendship/request/" + id).param("accept", "true"), bobToken);
        assertEquals(1, success(get("/friendship/friends"), aliceToken).size());
        assertEquals(1, success(get("/friendship/friends"), bobToken).size());
        assertEquals(1, success(get("/user/" + alice.getUserId() + "/stats"), aliceToken).path("followerCount").asInt());
        assertEquals(1, success(get("/user/" + alice.getUserId() + "/stats"), aliceToken).path("followingCount").asInt());
        success(put("/friendship/friend/" + bob.getUserId() + "/remark").param("remark", "Buddy"), aliceToken);
        JsonNode message = success(post("/message/send").param("toUserId", "" + bob.getUserId()).param("content", "hello private"), aliceToken);
        long messageId = message.path("messageId").asLong();
        String conversation = message.path("conversationId").asText();
        assertEquals(1, success(get("/message/unread/count"), bobToken).path("count").asInt());
        assertEquals(0, success(get("/message/unread/count"), aliceToken).path("count").asInt());
        User outsider = users.register("regression_outsider", "local-test-password");
        String outsiderToken = jwt.generateToken(outsider.getUserId(), outsider.getUsername());
        assertEquals(403, request(get("/message/conversation/" + conversation), outsiderToken).path("code").asInt());
        assertEquals(0, success(get("/friendship/friends"), outsiderToken).size());
        assertEquals(0, success(get("/message/conversations"), outsiderToken).size());
        success(put("/message/conversation/" + conversation + "/read"), bobToken);
        success(put("/message/conversation/" + conversation + "/read"), bobToken);
        assertEquals(0, success(get("/message/unread/count"), bobToken).path("count").asInt());
        assertNotEquals(0, request(put("/message/" + messageId + "/recall"), bobToken).path("code").asInt());
        success(put("/message/" + messageId + "/recall"), aliceToken);
        assertEquals(2, success(get("/message/conversation/" + conversation), bobToken).path("records").get(0).path("status").asInt());
        success(delete("/message/" + messageId), bobToken);
        assertEquals(0, success(get("/message/conversation/" + conversation), bobToken).path("total").asInt());
        assertEquals(1, success(get("/message/conversation/" + conversation), aliceToken).path("total").asInt());
        success(delete("/friendship/friend/" + bob.getUserId()), aliceToken);
        assertEquals(0, success(get("/friendship/friends"), bobToken).size());
    }

    @Test
    void inputValidationMediaEditsAndChatPagination() throws Exception {
        assertEquals(40001, request(post("/post/publish").param("content", "  "), aliceToken).path("code").asInt());
        assertEquals(400, request(post("/post/publish").param("images", "not-json"), aliceToken).path("code").asInt());
        assertEquals(40001, request(post("/user/login").param("username", alice.getUsername()).param("password", "wrong"), aliceToken).path("code").asInt());
        long postId = success(post("/post/publish").param("content", "media edit").param("images", "[\"/api/uploads/images/test.jpg\"]")
                .param("videoUrl", "/api/uploads/videos/test.mp4"), aliceToken).path("postId").asLong();
        success(put("/post/" + postId).param("content", "edited only text"), aliceToken);
        JsonNode retained = success(get("/post/" + postId), aliceToken);
        assertEquals("/api/uploads/videos/test.mp4", retained.path("videoUrl").asText());
        assertTrue(retained.path("images").asText().contains("test.jpg"));
        success(put("/post/" + postId).param("images", "[]").param("videoUrl", ""), aliceToken);
        assertEquals("[]", success(get("/post/" + postId), aliceToken).path("images").asText());
        long id = success(post("/friendship/request").param("toUserId", "" + bob.getUserId()), aliceToken).path("requestId").asLong();
        success(put("/friendship/request/" + id).param("accept", "true"), bobToken);
        JsonNode first = success(post("/message/send").param("toUserId", "" + bob.getUserId()).param("content", "one"), aliceToken);
        JsonNode second = success(post("/message/send").param("toUserId", "" + bob.getUserId()).param("content", "two"), aliceToken);
        String conversation = first.path("conversationId").asText();
        JsonNode page = success(get("/message/conversation/" + conversation).param("size", "1"), bobToken);
        assertEquals(2, page.path("total").asInt());
        assertEquals(1, page.path("records").size());
        success(put("/message/" + second.path("messageId").asLong() + "/recall"), aliceToken);
        assertEquals(1, success(get("/message/unread/count"), bobToken).path("count").asInt());
        success(delete("/message/" + first.path("messageId").asLong()), bobToken);
        assertEquals(0, success(get("/message/unread/count"), bobToken).path("count").asInt());
        assertEquals(0, success(get("/message/conversations"), bobToken).get(0).path(
                alice.getUserId() < bob.getUserId() ? "user2UnreadCount" : "user1UnreadCount").asInt());
    }
}

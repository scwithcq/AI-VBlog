package com.vblog;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vblog.service.UserService;
import com.vblog.utils.JwtUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@EnabledIfEnvironmentVariable(named="VBLOG_INTEGRATION_DB", matches="vblog_e2e_20261018")
class SecurityRegressionTest {
    @Autowired MockMvc http;
    @Autowired UserService users;
    @Autowired JwtUtil jwt;
    @Autowired ObjectMapper json;
    @Autowired javax.sql.DataSource db;

    String actor() throws Exception {
        try (var c=db.getConnection()) { assertTrue(c.getMetaData().getURL().contains("/vblog_e2e_20261018")); }
        var user=users.register("security_actor", "Strong-pass-123");
        return jwt.generateToken(user.getUserId(),user.getUsername());
    }

    @Test void anonymousForgedTokensAndOversizedPagesAreRejected() throws Exception {
        assertEquals(401,http.perform(get("/post/list")).andReturn().getResponse().getStatus());
        assertEquals(401,http.perform(get("/user/info").header("Authorization","Bearer forged.token.value")).andReturn().getResponse().getStatus());
        var response=http.perform(get("/post/list").param("size","1000000").header("Authorization",actor())).andReturn().getResponse();
        assertNotEquals(0,json.readTree(response.getContentAsString()).path("code").asInt());
        assertEquals("nosniff",response.getHeader("X-Content-Type-Options"));
        assertEquals("no-store",response.getHeader("Cache-Control"));
    }

    @Test void privateContactDetailsStayPrivateAndIdentityCannotBeOverwritten() throws Exception {
        String token=actor();
        http.perform(put("/user/info").header("Authorization",token).contentType("application/json")
                .content("{\"username\":\"hijacked\",\"phone\":\"13800000000\",\"email\":\"private@example.test\"}"));
        var self=json.readTree(http.perform(get("/user/info").header("Authorization",token)).andReturn().getResponse().getContentAsString()).path("data");
        assertEquals("security_actor",self.path("username").asText());
        assertEquals("private@example.test",self.path("email").asText());
        assertFalse(self.has("password"));
        for (String path : new String[]{"/user/"+self.path("userId").asLong(),"/user/all","/search/users?q=security"}) {
            String body=http.perform(get(path).header("Authorization",token)).andReturn().getResponse().getContentAsString();
            assertFalse(body.contains("13800000000")); assertFalse(body.contains("private@example.test"));
            assertFalse(body.contains("\"password\""));
        }
    }

    @Test void dangerousLinksMediaAndSpoofedFilesAreRejected() throws Exception {
        String token=actor();
        for (String value:new String[]{"javascript:alert(1)","data:text/html,test","//evil.test","https://user:pass@example.test"}) {
            var result=http.perform(put("/user/info").header("Authorization",token).contentType("application/json")
                    .content(json.writeValueAsString(java.util.Map.of("website",value)))).andReturn().getResponse();
            assertNotEquals(0,json.readTree(result.getContentAsString()).path("code").asInt());
        }
        var result=http.perform(post("/post/publish").header("Authorization",token).param("content","test")
                .param("images","[\"//evil.test/tracker.png\"]")).andReturn().getResponse();
        assertNotEquals(0,json.readTree(result.getContentAsString()).path("code").asInt());
        var fake=new org.springframework.mock.web.MockMultipartFile("file","fake.jpg","image/jpeg","<script>alert(1)</script>".getBytes());
        var upload=http.perform(multipart("/file/upload/image").file(fake).header("Authorization",token)).andReturn().getResponse();
        assertNotEquals(0,json.readTree(upload.getContentAsString()).path("code").asInt());
    }

    @Test void newPasswordsRespectBcryptLimitsAndDemoNamespaceIsReserved() throws Exception {
        actor();
        for(String pass:new String[]{"123456","abcdefghijklm","1234567890000","!".repeat(12),"Aa1".repeat(30)}) {
            assertThrows(IllegalArgumentException.class,()->users.register("weak_password",pass));
        }
        assertThrows(IllegalArgumentException.class,()->users.register("demo_intruder","Strong-pass-123"));
    }
}

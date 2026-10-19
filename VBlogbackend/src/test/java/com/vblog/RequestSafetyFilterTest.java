package com.vblog;

import com.vblog.config.RequestSafetyFilter;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import static org.junit.jupiter.api.Assertions.*;

class RequestSafetyFilterTest {
    @Test void loginFloodIsBlockedEvenIfForwardingHeaderChanges() throws Exception {
        var filter=new RequestSafetyFilter();
        for(int i=0;i<21;i++) {
            var request=new MockHttpServletRequest("POST","/api/user/login");
            request.setServletPath("/user/login");
            request.setParameter("username","SameAccount");
            request.addHeader("X-Forwarded-For","203.0.113."+i);
            var response=new MockHttpServletResponse();
            filter.doFilter(request,response,(req,res)->{});
            assertEquals(i<20?200:429,response.getStatus());
            if(i==20) assertEquals("600",response.getHeader("Retry-After"));
        }
    }
}

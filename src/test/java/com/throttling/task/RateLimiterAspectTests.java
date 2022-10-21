package com.throttling.task;

import com.throttling.ThrottlingApplication;
import com.throttling.task.config.AppConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = ThrottlingApplication.class)
@ContextConfiguration(classes = RateLimiterAspectTests.Config.class)
@AutoConfigureMockMvc
class RateLimiterAspectTests {

    @Value("${rate.limit}")
    private Integer rateLimit;

    @Autowired
    private MockMvc mvc;

    @Autowired
    @Qualifier("TestClass")
    TestClass testClass;

    @Test
    void testMvcRateLimits() throws Exception {
        for (int i = 0; i < 20; i++) {
            for (int j = 0; j < rateLimit; j++) {
                mvc.perform(get("/").with(remoteHost("remoteHost" + i))).andExpect(status().isOk());
            }
        }
    }

    @Test
    void testMvcRateLimit() throws Exception {
        for (int i = 0; i <= rateLimit; i++) {
            mvc.perform(get("/")).andExpect(i < rateLimit ? status().isOk() : status().isBadGateway());
        }
    }

    @Test()
    void testMoreThenRateLimit() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setParameter("X-FORWARDED-FOR", "192.168.1.1");
        AppConfig.CURRENT_REQUEST.set(request);

        assertThrows(RuntimeException.class, () -> {
            for (int i = 0; i < rateLimit + 1; i++) {
                testClass.testMethod();
            }
        });
    }

    private static RequestPostProcessor remoteHost(final String remoteHost) {
        return request -> {
            request.setRemoteAddr(remoteHost);
            return request;
        };
    }

    @Configuration
    @EnableAspectJAutoProxy
    public static class Config {

        @Bean
        @Qualifier("TestClass")
        TestClass getTestClass() {
            return new TestClass();
        }
    }

}

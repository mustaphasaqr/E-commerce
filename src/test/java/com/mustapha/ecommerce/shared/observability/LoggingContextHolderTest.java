package com.mustapha.ecommerce.shared.observability;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("LoggingContextHolder Tests")
class LoggingContextHolderTest {

    @BeforeEach
    void setUp() {
        MDC.clear();
    }

    @AfterEach
    void tearDown() {
        MDC.clear();
    }

    @Test
    @DisplayName("Should set userId in MDC")
    void setUserId() {
        String userId = "user-123";

        LoggingContextHolder.setUserId(userId);

        assertThat(MDC.get("userId")).isEqualTo(userId);
    }

    @Test
    @DisplayName("Should not set userId when null")
    void ignoreNullUserId() {
        LoggingContextHolder.setUserId(null);

        assertThat(MDC.get("userId")).isNull();
    }

    @Test
    @DisplayName("Should not set userId when empty")
    void ignoreEmptyUserId() {
        LoggingContextHolder.setUserId("   ");

        assertThat(MDC.get("userId")).isNull();
    }

    @Test
    @DisplayName("Should set sessionId in MDC")
    void setSessionId() {
        String sessionId = "session-abc-def";

        LoggingContextHolder.setSessionId(sessionId);

        assertThat(MDC.get("sessionId")).isEqualTo(sessionId);
    }

    @Test
    @DisplayName("Should set ipAddress in MDC")
    void setIpAddress() {
        String ipAddress = "192.168.1.100";

        LoggingContextHolder.setIpAddress(ipAddress);

        assertThat(MDC.get("ipAddress")).isEqualTo(ipAddress);
    }

    @Test
    @DisplayName("Should set httpMethod in MDC")
    void setHttpMethod() {
        String httpMethod = "POST";

        LoggingContextHolder.setHttpMethod(httpMethod);

        assertThat(MDC.get("httpMethod")).isEqualTo(httpMethod);
    }

    @Test
    @DisplayName("Should set requestPath in MDC")
    void setRequestPath() {
        String requestPath = "/api/orders/123";

        LoggingContextHolder.setRequestPath(requestPath);

        assertThat(MDC.get("requestPath")).isEqualTo(requestPath);
    }

    @Test
    @DisplayName("Should set userRole in MDC")
    void setUserRole() {
        String userRole = "ADMIN";

        LoggingContextHolder.setUserRole(userRole);

        assertThat(MDC.get("userRole")).isEqualTo(userRole);
    }

    @Test
    @DisplayName("Should clear all MDC values")
    void clearAllMdcValues() {
        LoggingContextHolder.setUserId("user-123");
        LoggingContextHolder.setSessionId("session-abc");
        LoggingContextHolder.setIpAddress("127.0.0.1");
        LoggingContextHolder.setHttpMethod("GET");
        LoggingContextHolder.setRequestPath("/api/test");
        LoggingContextHolder.setUserRole("USER");

        LoggingContextHolder.clear();

        assertThat(MDC.get("userId")).isNull();
        assertThat(MDC.get("sessionId")).isNull();
        assertThat(MDC.get("ipAddress")).isNull();
        assertThat(MDC.get("httpMethod")).isNull();
        assertThat(MDC.get("requestPath")).isNull();
        assertThat(MDC.get("userRole")).isNull();
    }

    @Test
    @DisplayName("Should set multiple MDC values independently")
    void setMultipleMdcValues() {
        LoggingContextHolder.setUserId("user-456");
        LoggingContextHolder.setIpAddress("10.0.0.1");
        LoggingContextHolder.setUserRole("SELLER");

        assertThat(MDC.get("userId")).isEqualTo("user-456");
        assertThat(MDC.get("ipAddress")).isEqualTo("10.0.0.1");
        assertThat(MDC.get("userRole")).isEqualTo("SELLER");
        assertThat(MDC.get("sessionId")).isNull();
    }

    @Test
    @DisplayName("Should prevent instantiation")
    void preventInstantiation() {
        assertThatThrownBy(() -> {
            var constructor = LoggingContextHolder.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            constructor.newInstance();
        }).hasCauseInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    @DisplayName("Should be thread-safe with MDC")
    void threadSafeMdc() throws InterruptedException {
        Thread thread1 = new Thread(() -> {
            LoggingContextHolder.setUserId("user-thread-1");
            assertThat(MDC.get("userId")).isEqualTo("user-thread-1");
        });

        Thread thread2 = new Thread(() -> {
            LoggingContextHolder.setUserId("user-thread-2");
            assertThat(MDC.get("userId")).isEqualTo("user-thread-2");
        });

        thread1.start();
        thread2.start();

        thread1.join();
        thread2.join();

        assertThat(MDC.get("userId")).isNull();
    }
}

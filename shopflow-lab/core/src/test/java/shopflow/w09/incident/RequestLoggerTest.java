package shopflow.w09.incident;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import shopflow.w09.LogCapture;

/** INC-09-A: "a customer's login token showed up in our log search". */
@Tag("w09")
class RequestLoggerTest {

    private static HttpRequest request() {
        Map<String, String> headers = new LinkedHashMap<>();
        headers.put("Authorization", "Bearer abc123");
        headers.put("Cookie", "session=s3cr3t");
        headers.put("X-Request-Id", "req-42");
        headers.put("Content-Type", "application/json");
        return new HttpRequest("POST", "/orders", headers, "{\"email\":\"sara@example.com\",\"password\":\"hunter2\"}");
    }

    private String logged() {
        try (LogCapture logs = new LogCapture(RequestLogger.class)) {
            new RequestLogger().logIncoming(request());
            assertThat(logs.messages()).hasSize(1);
            return logs.messages().get(0);
        }
    }

    @Test
    @DisplayName("guard: support can still see which endpoint was called")
    void mentionsMethodAndPath() {
        assertThat(logged()).contains("POST").contains("/orders");
    }

    @Test
    @DisplayName("guard: support can still correlate by request id")
    void keepsTheRequestId() {
        assertThat(logged()).contains("req-42");
    }

    @Test
    @DisplayName("the bearer token is never logged")
    void noBearerToken() {
        assertThat(logged()).doesNotContain("abc123");
    }

    @Test
    @DisplayName("session cookies are never logged")
    void noSessionCookie() {
        assertThat(logged()).doesNotContain("s3cr3t");
    }

    @Test
    @DisplayName("passwords in the body are never logged")
    void noPassword() {
        assertThat(logged()).doesNotContain("hunter2");
    }
}

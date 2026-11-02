package shopflow.api.w11;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.web.servlet.MockMvc;

/**
 * INC-39/40: "The web UI shows the order on the wrong day and some totals have lost cents."
 * The server clock is fixed at 2026-11-02T23:30:00Z.
 */
@Tag("w11")
@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("INC-39/40 dates and money in JSON")
class Inc39MoneyAndDatesTest {

    @TestConfiguration
    static class FixedClock {
        @Bean
        @Primary
        Clock fixedClock() {
            return Clock.fixed(Instant.parse("2026-11-02T23:30:00Z"), ZoneOffset.UTC);
        }
    }

    @Autowired
    MockMvc mvc;

    @Test
    @DisplayName("GUARD: the summary endpoint answers with the id")
    void basics() throws Exception {
        mvc.perform(get("/api/order-summaries/1")).andExpect(status().isOk()).andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @DisplayName("createdAt is an instant with a zone: 2026-11-02T23:30:00Z")
    void createdAtHasZone() throws Exception {
        mvc.perform(get("/api/order-summaries/1")).andExpect(jsonPath("$.createdAt").value("2026-11-02T23:30:00Z"));
    }

    @Test
    @DisplayName("total keeps its scale: the string \"19.90\", not the number 19.9")
    void totalAsString() throws Exception {
        // raw JSON on purpose: a JSON number would be read back as the double 19.9
        mvc.perform(get("/api/order-summaries/1")).andExpect(content().string(containsString("\"total\":\"19.90\"")));
    }

    @Test
    @DisplayName("a very large total survives exactly: \"12345678901234567.89\"")
    void largeTotal() throws Exception {
        mvc.perform(get("/api/order-summaries/2"))
                .andExpect(content().string(containsString("\"total\":\"12345678901234567.89\"")));
    }
}

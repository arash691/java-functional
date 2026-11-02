package shopflow.api.w11;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

import shopflow.api.w11.ping.PingController;

/**
 * WORKED EXAMPLE (green from the start). A "web slice" test: only the web layer for one controller is started.
 * Read it, run it, then change the controller and watch the test react.
 */
@Tag("w11")
@WebMvcTest(PingController.class)
@DisplayName("Worked example: GET /api/ping")
class PingControllerTest {

    @Autowired
    MockMvc mvc;

    @Test
    @DisplayName("returns {\"status\":\"ok\"}")
    void ping() throws Exception {
        mvc.perform(get("/api/ping")).andExpect(status().isOk()).andExpect(jsonPath("$.status").value("ok"));
    }
}

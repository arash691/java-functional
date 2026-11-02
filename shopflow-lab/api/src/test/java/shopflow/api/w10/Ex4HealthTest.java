package shopflow.api.w10;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

/** EX-10-04: Actuator health with a custom indicator. */
@Tag("w10")
@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("EX-10-04 /actuator/health reports the shop")
class Ex4HealthTest {

    @Autowired
    MockMvc mvc;

    @Test
    @DisplayName("health endpoint is UP")
    void up() throws Exception {
        mvc.perform(get("/actuator/health")).andExpect(status().isOk()).andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    @DisplayName("component 'shop' is UP and shows the shop name (needs the indicator AND show-details)")
    void shopComponent() throws Exception {
        mvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.components.shop.status").value("UP"))
                .andExpect(jsonPath("$.components.shop.details.name").value("ShopFlow"));
    }
}

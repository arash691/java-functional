package shopflow.api.w11;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import shopflow.api.w11.config.CatalogProperties;

/**
 * INC-38: "Production shows 10 products per page, but we configured 50 for production."
 * Ops starts production with SPRING_PROFILES_ACTIVE=production.
 */
@Tag("w11")
@DisplayName("INC-38 page size per environment")
class Inc38ConfigPrecedenceTest {

    @SpringBootTest
    @Nested
    @DisplayName("no profile")
    class NoProfile {
        @Autowired
        CatalogProperties props;

        @Test
        @DisplayName("GUARD: default page size is 10")
        void defaultSize() {
            assertThat(props.pageSize()).isEqualTo(10);
        }
    }

    @SpringBootTest
    @ActiveProfiles("production")
    @Nested
    @DisplayName("profile 'production'")
    class Production {
        @Autowired
        CatalogProperties props;

        @Test
        @DisplayName("page size is 50 (the production value)")
        void productionSize() {
            assertThat(props.pageSize()).as("page size with profile 'production'").isEqualTo(50);
        }
    }

    @SpringBootTest(properties = {
            "shopflow.catalog.page-size=20",
            "management.endpoints.web.exposure.include=env",
            "management.endpoint.env.show-values=ALWAYS"})
    @AutoConfigureMockMvc
    @ActiveProfiles("production")
    @Nested
    @DisplayName("profile 'production' plus an explicit property")
    class Override {
        @Autowired
        CatalogProperties props;

        @Autowired
        MockMvc mvc;

        @Test
        @DisplayName("GUARD: an explicit property beats the profile file")
        void explicitWins() {
            assertThat(props.pageSize()).isEqualTo(20);
        }

        @Test
        @DisplayName("GUARD: /actuator/env shows which value wins")
        void envEndpoint() throws Exception {
            mvc.perform(get("/actuator/env/shopflow.catalog.page-size"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.property.value").value("20"))
                    .andExpect(jsonPath("$.propertySources").isArray());
        }
    }
}

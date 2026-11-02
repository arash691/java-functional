package shopflow.api.w11;

import static org.hamcrest.Matchers.matchesPattern;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import shopflow.api.w11.catalog.InMemoryProductRepository;

/** EX-11-02: POST /api/products -> 201 Created + Location. */
@Tag("w11")
@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("EX-11-02 POST /api/products")
class Ex2CreateProductTest {

    @Autowired
    MockMvc mvc;

    @Autowired
    InMemoryProductRepository repository;

    @BeforeEach
    void seed() {
        repository.resetToSeed();
    }

    @Test
    @DisplayName("valid body -> 201, Location header, created product in the body")
    void created() throws Exception {
        mvc.perform(post("/api/products").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"sku\":\"NEW-1\",\"name\":\"Eraser\",\"price\":2.50}"))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", matchesPattern(".*/api/products/\\d+")))
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.sku").value("NEW-1"))
                .andExpect(jsonPath("$.price").value(2.50));
    }

    @Test
    @DisplayName("the Location header points at the new product")
    void locationWorks() throws Exception {
        MvcResult created = mvc.perform(post("/api/products").contentType(MediaType.APPLICATION_JSON)
                .content("{\"sku\":\"NEW-2\",\"name\":\"Ruler\",\"price\":3.00}")).andReturn();
        String location = created.getResponse().getHeader("Location");
        org.assertj.core.api.Assertions.assertThat(location).as("Location header").isNotNull();
        mvc.perform(get(location)).andExpect(status().isOk()).andExpect(jsonPath("$.sku").value("NEW-2"));
    }
}

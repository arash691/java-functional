package shopflow.api.w11;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
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

import shopflow.api.w11.catalog.InMemoryProductRepository;

/** EX-11-01: GET /api/products/{id} and the 404 problem response. */
@Tag("w11")
@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("EX-11-01 GET /api/products/{id}")
class Ex1GetProductTest {

    @Autowired
    MockMvc mvc;

    @Autowired
    InMemoryProductRepository repository;

    @BeforeEach
    void seed() {
        repository.resetToSeed();
    }

    @Test
    @DisplayName("existing product -> 200 with id, sku, name, price")
    void found() throws Exception {
        mvc.perform(get("/api/products/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.sku").value("SKU-1"))
                .andExpect(jsonPath("$.name").value("Notebook"))
                .andExpect(jsonPath("$.price").value(10.00));
    }

    @Test
    @DisplayName("unknown product -> 404 application/problem+json with title, status and a detail naming the id")
    void notFound() throws Exception {
        mvc.perform(get("/api/products/999"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith("application/problem+json"))
                .andExpect(jsonPath("$.title").value("Product not found"))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.detail", containsString("999")));
    }

    @Test
    @DisplayName("GUARD: a non-numeric id is a client error handled by Spring (400), not a 500")
    void badId() throws Exception {
        mvc.perform(get("/api/products/abc")).andExpect(status().isBadRequest());
    }
}

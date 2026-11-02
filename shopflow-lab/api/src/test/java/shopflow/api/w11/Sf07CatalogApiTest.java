package shopflow.api.w11;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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

/** SF-07: the complete Catalog API. (Needs EX-11-01..03 done first.) */
@Tag("w11")
@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("SF-07 Catalog API")
class Sf07CatalogApiTest {

    @Autowired
    MockMvc mvc;

    @Autowired
    InMemoryProductRepository repository;

    @BeforeEach
    void seed() {
        repository.resetToSeed();
    }

    private static String body(String sku, String name, String price) {
        return "{\"sku\":\"" + sku + "\",\"name\":\"" + name + "\",\"price\":" + price + "}";
    }

    @Test
    @DisplayName("PUT replaces a product")
    void update() throws Exception {
        mvc.perform(put("/api/products/1").contentType(MediaType.APPLICATION_JSON).content(body("SKU-1", "Notebook A5", "12.00")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Notebook A5"))
                .andExpect(jsonPath("$.price").value(12.00));
        mvc.perform(get("/api/products/1")).andExpect(jsonPath("$.name").value("Notebook A5"));
    }

    @Test
    @DisplayName("PUT on an unknown id -> 404 problem")
    void updateUnknown() throws Exception {
        mvc.perform(put("/api/products/999").contentType(MediaType.APPLICATION_JSON).content(body("SKU-9", "X", "1.00")))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith("application/problem+json"));
    }

    @Test
    @DisplayName("PUT validates the body like POST")
    void updateInvalid() throws Exception {
        mvc.perform(put("/api/products/1").contentType(MediaType.APPLICATION_JSON).content(body("x", "", "-5")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").isArray());
    }

    @Test
    @DisplayName("DELETE -> 204, then the product is gone (404)")
    void deleteRemovesProduct() throws Exception {
        mvc.perform(delete("/api/products/2")).andExpect(status().isNoContent());
        mvc.perform(get("/api/products/2")).andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE an unknown id -> 404 problem")
    void deleteUnknown() throws Exception {
        mvc.perform(delete("/api/products/999")).andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith("application/problem+json"));
    }

    @Test
    @DisplayName("POST with an existing SKU -> 409 problem titled 'Duplicate SKU'")
    void duplicateOnCreate() throws Exception {
        mvc.perform(post("/api/products").contentType(MediaType.APPLICATION_JSON).content(body("SKU-1", "Other", "1.00")))
                .andExpect(status().isConflict())
                .andExpect(content().contentTypeCompatibleWith("application/problem+json"))
                .andExpect(jsonPath("$.title").value("Duplicate SKU"))
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    @DisplayName("PUT that would take another product's SKU -> 409")
    void duplicateOnUpdate() throws Exception {
        mvc.perform(put("/api/products/1").contentType(MediaType.APPLICATION_JSON).content(body("SKU-2", "Other", "1.00")))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("GET /api/products uses shopflow.catalog.page-size (10) by default")
    void defaultPage() throws Exception {
        for (int i = 3; i <= 14; i++) {
            mvc.perform(post("/api/products").contentType(MediaType.APPLICATION_JSON).content(body("SKU-" + i, "P" + i, "1.00")))
                    .andExpect(status().isCreated());
        }
        mvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(10)))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(10))
                .andExpect(jsonPath("$.totalItems").value(14));
    }

    @Test
    @DisplayName("page and size parameters select a page")
    void explicitPage() throws Exception {
        for (int i = 3; i <= 14; i++) {
            mvc.perform(post("/api/products").contentType(MediaType.APPLICATION_JSON).content(body("SKU-" + i, "P" + i, "1.00")));
        }
        mvc.perform(get("/api/products").param("page", "1").param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(5)))
                .andExpect(jsonPath("$.items[0].sku").value("SKU-6"))
                .andExpect(jsonPath("$.page").value(1))
                .andExpect(jsonPath("$.size").value(5));
    }
}

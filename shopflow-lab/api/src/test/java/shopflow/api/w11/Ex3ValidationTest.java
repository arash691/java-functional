package shopflow.api.w11;

import static org.hamcrest.Matchers.hasItems;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
import org.springframework.test.web.servlet.ResultActions;

import shopflow.api.w11.catalog.InMemoryProductRepository;

/** EX-11-03: Bean Validation and a useful 400 body. */
@Tag("w11")
@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("EX-11-03 request validation")
class Ex3ValidationTest {

    @Autowired
    MockMvc mvc;

    @Autowired
    InMemoryProductRepository repository;

    @BeforeEach
    void seed() {
        repository.resetToSeed();
    }

    private ResultActions send(String json) throws Exception {
        return mvc.perform(post("/api/products").contentType(MediaType.APPLICATION_JSON).content(json));
    }

    @Test
    @DisplayName("all invalid fields are reported: 400 problem+json with errors[].field")
    void allFieldsReported() throws Exception {
        send("{\"sku\":\"ab\",\"name\":\"\",\"price\":-1}")
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith("application/problem+json"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors[*].field", hasItems("sku", "name", "price")))
                .andExpect(jsonPath("$.errors[0].message").isNotEmpty());
    }

    @Test
    @DisplayName("missing fields are reported too")
    void missingFields() throws Exception {
        send("{}").andExpect(status().isBadRequest()).andExpect(jsonPath("$.errors[*].field", hasItems("sku", "name", "price")));
    }

    @Test
    @DisplayName("price with more than 2 decimals is rejected")
    void tooManyDecimals() throws Exception {
        send("{\"sku\":\"NEW-1\",\"name\":\"Eraser\",\"price\":1.234}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[*].field", hasItems("price")));
    }

    @Test
    @DisplayName("sku must be 3-20 characters of A-Z, 0-9 and '-'")
    void skuPattern() throws Exception {
        send("{\"sku\":\"has space\",\"name\":\"Eraser\",\"price\":1.00}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[*].field", hasItems("sku")));
    }

    @Test
    @DisplayName("GUARD: malformed JSON is already a 400 problem response from Spring")
    void malformedJson() throws Exception {
        send("{not json").andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith("application/problem+json"));
    }
}

package shopflow.api.w12;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

/**
 * INC-42: "GET /api/orders/{id} returns 500 in production." (Fix INC-49 first: the controller should use the query service.)
 */
@Tag("w12")
@AutoConfigureMockMvc
@DisplayName("INC-42 the order endpoint returns a DTO, not an entity")
class Inc42EntitySerializationTest extends AbstractPostgresTest {

    @Autowired
    MockMvc mvc;

    @Autowired
    OrderTestData data;

    @Test
    @DisplayName("GUARD: unknown id -> 404")
    void unknown() throws Exception {
        mvc.perform(get("/api/orders/424242")).andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/orders/{id} -> 200 with the items' SKUs and the total")
    void ok() throws Exception {
        Long id = data.createOrders(1, 3);

        mvc.perform(get("/api/orders/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.status").value("NEW"))
                .andExpect(jsonPath("$.items.length()").value(3))
                .andExpect(jsonPath("$.items[0].sku").value("SKU-1"))
                .andExpect(jsonPath("$.total").value(33.50));
    }

    @Test
    @DisplayName("the response has no back-reference from an item to its order, and no Hibernate internals")
    void noCycles() throws Exception {
        Long id = data.createOrders(1, 2);

        mvc.perform(get("/api/orders/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].order").doesNotExist())
                .andExpect(jsonPath("$.items[0].product").doesNotExist())
                .andExpect(jsonPath("$.hibernateLazyInitializer").doesNotExist());
    }
}

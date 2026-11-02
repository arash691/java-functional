package shopflow.api.w10;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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

import shopflow.api.w10.cart.CartService;

/** INC-33: "customers see each other's cart totals". */
@Tag("w10")
@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("INC-33 carts must not leak between customers")
class Inc33SharedCartTest {

    @Autowired
    MockMvc mvc;

    @Autowired
    CartService carts;

    @BeforeEach
    void reset() {
        carts.reset();
    }

    private void add(String customer, String sku, int qty) throws Exception {
        mvc.perform(post("/api/carts/{c}/items", customer).contentType(MediaType.APPLICATION_JSON)
                .content("{\"sku\":\"" + sku + "\",\"quantity\":" + qty + "}")).andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("GUARD: a single customer sees the sum of their own lines")
    void singleCustomer() throws Exception {
        add("alice", "SKU-1", 2);
        add("alice", "SKU-2", 1);
        mvc.perform(get("/api/carts/alice/total")).andExpect(jsonPath("$.total").value(25.00));
    }

    @Test
    @DisplayName("two customers shopping at the same time keep separate totals")
    void twoCustomers() throws Exception {
        add("alice", "SKU-1", 2); // 20.00
        add("bob", "SKU-2", 1); //  5.00
        mvc.perform(get("/api/carts/alice/total")).andExpect(jsonPath("$.total").value(20.00));
        mvc.perform(get("/api/carts/bob/total")).andExpect(jsonPath("$.total").value(5.00));
    }

    @Test
    @DisplayName("a customer who has not added anything has an empty cart")
    void emptyCart() throws Exception {
        add("alice", "SKU-3", 1);
        mvc.perform(get("/api/carts/carol/total")).andExpect(jsonPath("$.total").value(0.00));
    }
}

package shopflow.api.w10;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

/** SF-06: bootstrap service. */
@Tag("w10")
@DisplayName("SF-06 shop info and pricing quote")
class Sf06ShopApiTest {

    @SpringBootTest
    @AutoConfigureMockMvc
    @Nested
    @DisplayName("default profile")
    class Default {

        @Autowired
        MockMvc mvc;

        @Test
        @DisplayName("GET /api/shop returns name and currency from configuration")
        void shopInfo() throws Exception {
            mvc.perform(get("/api/shop"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name").value("ShopFlow"))
                    .andExpect(jsonPath("$.currency").value("EUR"));
        }

        @Test
        @DisplayName("quote below the free-shipping threshold: 2x SKU-1 + 1x SKU-2 to DE")
        void quoteWithShipping() throws Exception {
            mvc.perform(quote("""
                    {"items":[{"sku":"SKU-1","quantity":2},{"sku":"SKU-2","quantity":1}],"country":"DE"}"""))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.subtotal").value(25.00))
                    .andExpect(jsonPath("$.tax").value(4.75))
                    .andExpect(jsonPath("$.shipping").value(5.00))
                    .andExpect(jsonPath("$.total").value(34.75));
        }

        @Test
        @DisplayName("quote at or above 50.00 ships free")
        void freeShipping() throws Exception {
            mvc.perform(quote("""
                    {"items":[{"sku":"SKU-3","quantity":2}],"country":"US"}"""))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.subtotal").value(50.00))
                    .andExpect(jsonPath("$.tax").value(0.00))
                    .andExpect(jsonPath("$.shipping").value(0.00))
                    .andExpect(jsonPath("$.total").value(50.00));
        }

        @Test
        @DisplayName("unknown SKU -> 400")
        void unknownSku() throws Exception {
            mvc.perform(quote("""
                    {"items":[{"sku":"NOPE","quantity":1}],"country":"DE"}"""))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("empty items -> 400 (validation)")
        void emptyItems() throws Exception {
            mvc.perform(quote("""
                    {"items":[],"country":"DE"}"""))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("zero quantity -> 400 (validation)")
        void zeroQuantity() throws Exception {
            mvc.perform(quote("""
                    {"items":[{"sku":"SKU-1","quantity":0}],"country":"DE"}"""))
                    .andExpect(status().isBadRequest());
        }
    }

    @SpringBootTest
    @AutoConfigureMockMvc
    @ActiveProfiles("express")
    @Nested
    @DisplayName("express profile")
    class Express {

        @Autowired
        MockMvc mvc;

        @Test
        @DisplayName("express shipping is 15.00 even above the threshold")
        void expressShipping() throws Exception {
            mvc.perform(quote("""
                    {"items":[{"sku":"SKU-3","quantity":2}],"country":"US"}"""))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.shipping").value(15.00))
                    .andExpect(jsonPath("$.total").value(65.00));
        }
    }

    private static org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder quote(String json) {
        return post("/api/pricing/quote").contentType(MediaType.APPLICATION_JSON).content(json);
    }
}

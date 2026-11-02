package shopflow.w08;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

public final class OrderJson {

    private static final ObjectMapper MAPPER = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    private OrderJson() {}

    public static String toJson(Order order) {
        try {
            return MAPPER.writeValueAsString(order);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Cannot write order " + order.id(), e);
        }
    }

    public static Order fromJson(String json) {
        try {
            return MAPPER.readValue(json, Order.class);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Bad order JSON", e);
        }
    }
}

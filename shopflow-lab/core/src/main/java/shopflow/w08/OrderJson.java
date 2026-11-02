package shopflow.w08;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * SF-05 persistence format: {@code {"id":"A-1","placedAt":"2025-03-09T07:00:00Z"}}.
 * The timestamp must be an ISO-8601 UTC string, never a number.
 */
public final class OrderJson {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private OrderJson() {}

    public static String toJson(Order order) {
        throw new UnsupportedOperationException("TODO SF-05 toJson");
    }

    public static Order fromJson(String json) {
        throw new UnsupportedOperationException("TODO SF-05 fromJson");
    }
}

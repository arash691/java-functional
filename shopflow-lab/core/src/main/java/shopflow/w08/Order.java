package shopflow.w08;

import java.time.Instant;

/** SF-05: an order knows WHEN it was placed as a point on the global timeline. */
public record Order(String id, Instant placedAt) {}

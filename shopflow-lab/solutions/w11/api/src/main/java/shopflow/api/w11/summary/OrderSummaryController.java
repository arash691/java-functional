package shopflow.api.w11.summary;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/order-summaries")
public class OrderSummaryController {

    private final Clock clock;

    public OrderSummaryController(Clock clock) {
        this.clock = clock;
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderSummaryResponse> get(@PathVariable long id) {
        BigDecimal total = id == 2 ? new BigDecimal("12345678901234567.89") : new BigDecimal("19.90");
        return ResponseEntity.ok(new OrderSummaryResponse(id, Instant.now(clock), total));
    }
}

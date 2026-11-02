package shopflow.api.w10.cart;

import java.math.BigDecimal;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/carts/{customerId}")
public class CartController {

    private final CartService carts;

    public CartController(CartService carts) {
        this.carts = carts;
    }

    @PostMapping("/items")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void add(@PathVariable String customerId, @RequestBody CartLine line) {
        carts.addItem(customerId, line.sku(), line.quantity());
    }

    @GetMapping("/total")
    public Map<String, Object> total(@PathVariable String customerId) {
        BigDecimal total = carts.total(customerId);
        return Map.of("customerId", customerId, "total", total);
    }
}

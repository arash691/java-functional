package shopflow.api.w10.cart;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.springframework.stereotype.Service;

import shopflow.api.w10.pricing.PriceList;

/** The service holds no per-customer state in fields of its own; state is keyed by customer id. */
@Service
public class CartService {

    private final PriceList prices;

    private final Map<String, List<CartLine>> linesByCustomer = new ConcurrentHashMap<>();

    public CartService(PriceList prices) {
        this.prices = prices;
    }

    public void addItem(String customerId, String sku, int quantity) {
        linesByCustomer.computeIfAbsent(customerId, id -> new CopyOnWriteArrayList<>())
                .add(new CartLine(sku, quantity));
    }

    public BigDecimal total(String customerId) {
        return linesByCustomer.getOrDefault(customerId, List.of()).stream()
                .map(l -> prices.priceOf(l.sku()).orElseThrow().multiply(BigDecimal.valueOf(l.quantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2);
    }

    /** Test hook: forget all carts. */
    public void reset() {
        linesByCustomer.clear();
    }
}

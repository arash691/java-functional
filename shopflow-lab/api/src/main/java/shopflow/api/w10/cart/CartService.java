package shopflow.api.w10.cart;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import shopflow.api.w10.pricing.PriceList;

/** Keeps the shopping cart and computes its total. */
@Service
public class CartService {

    private final PriceList prices;

    // the lines of the cart
    private final List<CartLine> lines = new ArrayList<>();

    public CartService(PriceList prices) {
        this.prices = prices;
    }

    public void addItem(String customerId, String sku, int quantity) {
        lines.add(new CartLine(sku, quantity));
    }

    public BigDecimal total(String customerId) {
        return lines.stream()
                .map(l -> prices.priceOf(l.sku()).orElseThrow().multiply(BigDecimal.valueOf(l.quantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2);
    }

    /** Test hook: forget all carts. */
    public void reset() {
        lines.clear();
    }
}

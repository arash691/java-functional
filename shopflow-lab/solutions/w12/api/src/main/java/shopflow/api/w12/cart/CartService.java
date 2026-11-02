package shopflow.api.w12.cart;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import shopflow.api.w12.catalog.ProductEntity;
import shopflow.api.w12.catalog.ProductRepository;

@Service
public class CartService {

    private final CartRepository carts;
    private final ProductRepository products;

    public CartService(CartRepository carts, ProductRepository products) {
        this.carts = carts;
        this.products = products;
    }

    @Transactional
    public void addItem(String customerId, String sku, int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("quantity must be positive: " + quantity);
        }
        ProductEntity product = products.findBySku(sku).orElseThrow(() -> new UnknownProductException(sku));
        CartEntity cart = carts.findByCustomerId(customerId).orElseGet(() -> carts.save(new CartEntity(customerId)));
        cart.getItems().stream()
                .filter(i -> i.getProduct().getId().equals(product.getId()))
                .findFirst()
                .ifPresentOrElse(
                        i -> i.setQuantity(i.getQuantity() + quantity),
                        () -> cart.addItem(new CartItemEntity(product, quantity)));
    }

    @Transactional(readOnly = true)
    public CartView getCart(String customerId) {
        List<CartLineView> lines = carts.findByCustomerId(customerId).map(CartEntity::getItems).orElse(List.of()).stream()
                .map(i -> {
                    BigDecimal unit = i.getProduct().getPrice();
                    return new CartLineView(i.getProduct().getSku(), i.getProduct().getName(), i.getQuantity(), unit,
                            unit.multiply(BigDecimal.valueOf(i.getQuantity())));
                })
                .toList();
        BigDecimal total = lines.stream().map(CartLineView::lineTotal).reduce(BigDecimal.ZERO, BigDecimal::add).setScale(2);
        return new CartView(customerId, lines, total);
    }

    @Transactional
    public void clear(String customerId) {
        carts.findByCustomerId(customerId).ifPresent(cart -> cart.getItems().clear());
    }
}

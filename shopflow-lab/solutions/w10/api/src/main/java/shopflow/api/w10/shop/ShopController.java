package shopflow.api.w10.shop;

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import shopflow.api.w10.config.ShopProperties;

@RestController
@RequestMapping("/api/shop")
public class ShopController {

    private final ShopProperties shop;

    public ShopController(ShopProperties shop) {
        this.shop = shop;
    }

    @GetMapping
    public Map<String, String> info() {
        return Map.of("name", shop.name(), "currency", shop.currency());
    }
}

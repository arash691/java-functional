package shopflow.api.w10.shop;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** SF-06: GET /api/shop returns {"name": ..., "currency": ...} taken from ShopProperties. */
@RestController
@RequestMapping("/api/shop")
public class ShopController {

    @GetMapping
    public ResponseEntity<Map<String, String>> info() {
        return ResponseEntity.status(501).build(); // TODO SF-06
    }
}

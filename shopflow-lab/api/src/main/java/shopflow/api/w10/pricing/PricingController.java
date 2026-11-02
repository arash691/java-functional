package shopflow.api.w10.pricing;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/** SF-06: POST /api/pricing/quote. Invalid body -> 400, unknown SKU -> 400. */
@RestController
@RequestMapping("/api/pricing")
public class PricingController {

    @PostMapping("/quote")
    public QuoteResponse quote(@RequestBody QuoteRequest request) {
        throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED, "TODO SF-06");
    }
}

package shopflow.api.w10.pricing;

import org.springframework.stereotype.Service;

/**
 * SF-06: subtotal = sum(price * quantity); tax from TaxCalculator; shipping from the active ShippingPolicy;
 * total = subtotal + tax + shipping. All amounts scale 2. Unknown SKU -> UnknownSkuException.
 */
@Service
public class PricingService {

    public QuoteResponse quote(QuoteRequest request) {
        throw new UnsupportedOperationException("TODO SF-06");
    }
}

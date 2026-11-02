package shopflow.api.w10.pricing;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;

import shopflow.api.w10.shipping.ShippingPolicy;
import shopflow.api.w10.tax.TaxCalculator;

@Service
public class PricingService {

    private final PriceList prices;
    private final TaxCalculator taxes;
    private final ShippingPolicy shipping;

    public PricingService(PriceList prices, TaxCalculator taxes, ShippingPolicy shipping) {
        this.prices = prices;
        this.taxes = taxes;
        this.shipping = shipping;
    }

    public QuoteResponse quote(QuoteRequest request) {
        BigDecimal subtotal = BigDecimal.ZERO;
        for (QuoteRequest.Line line : request.items()) {
            BigDecimal price = prices.priceOf(line.sku()).orElseThrow(() -> new UnknownSkuException(line.sku()));
            subtotal = subtotal.add(price.multiply(BigDecimal.valueOf(line.quantity())));
        }
        subtotal = subtotal.setScale(2);
        BigDecimal tax = taxes.taxFor(subtotal, request.country());
        BigDecimal ship = shipping.costFor(subtotal).setScale(2);
        return new QuoteResponse(subtotal, tax, ship, subtotal.add(tax).add(ship));
    }
}

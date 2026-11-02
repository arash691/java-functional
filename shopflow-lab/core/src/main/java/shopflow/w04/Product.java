package shopflow.w04;

/**
 * SF-03: the product model, as a record. Add validation in a compact constructor:
 *  - sku and name not null/blank, category not null
 *  - price not null and strictly positive (Money.isPositive)
 * and implement withPrice (returns a copy with a new price, validated too).
 */
public record Product(String sku, String name, Money price, Category category) {

    public Product withPrice(Money newPrice) {
        return this; // TODO SF-03
    }
}

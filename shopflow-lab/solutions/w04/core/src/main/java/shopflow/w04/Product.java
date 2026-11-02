package shopflow.w04;

public record Product(String sku, String name, Money price, Category category) {

    public Product {
        if (sku == null || sku.isBlank()) {
            throw new IllegalArgumentException("sku must not be blank");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("name must not be blank");
        }
        if (category == null) {
            throw new IllegalArgumentException("category must not be null");
        }
        if (price == null || !price.isPositive()) {
            throw new IllegalArgumentException("price must be positive: " + price);
        }
    }

    public Product withPrice(Money newPrice) {
        return new Product(sku, name, newPrice, category);
    }
}

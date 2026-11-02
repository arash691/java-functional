package shopflow.w04.incident;

public class ProductListing {

    private final String sku;
    private final String title;

    public ProductListing(String sku, String title) {
        this.sku = sku;
        this.title = title;
    }

    public String sku() {
        return sku;
    }

    public String title() {
        return title;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ProductListing other)) {
            return false;
        }
        return sku.equals(other.sku);
    }

    @Override
    public int hashCode() {
        return sku.hashCode();
    }
}

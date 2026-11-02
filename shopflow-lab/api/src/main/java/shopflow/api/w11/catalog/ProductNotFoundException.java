package shopflow.api.w11.catalog;

public class ProductNotFoundException extends RuntimeException {

    private final long id;

    public ProductNotFoundException(long id) {
        super("Product " + id + " does not exist");
        this.id = id;
    }

    public long id() {
        return id;
    }
}

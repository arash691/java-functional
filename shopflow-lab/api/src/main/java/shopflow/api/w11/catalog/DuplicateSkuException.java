package shopflow.api.w11.catalog;

public class DuplicateSkuException extends RuntimeException {

    public DuplicateSkuException(String sku) {
        super("A product with SKU " + sku + " already exists");
    }
}

package shopflow.w03.incident;

/** One line of a customer's cart. Quantity is boxed because the web layer may omit it. */
public record CartLine(String sku, Integer quantity) {}

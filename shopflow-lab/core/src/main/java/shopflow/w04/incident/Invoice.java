package shopflow.w04.incident;

import java.math.BigDecimal;

/** Collects invoice lines and reports the total that is charged to the customer's card. */
public class Invoice {

    private double total;

    public void addLine(BigDecimal unitPrice, int quantity) {
        total += unitPrice.doubleValue() * quantity;
    }

    /** The amount to charge, in cents. */
    public long totalCents() {
        return (long) (total * 100);
    }

    public BigDecimal total() {
        return BigDecimal.valueOf(total);
    }
}

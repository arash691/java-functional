package shopflow.w04.incident;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class Invoice {

    private BigDecimal total = BigDecimal.ZERO;

    public void addLine(BigDecimal unitPrice, int quantity) {
        total = total.add(unitPrice.multiply(BigDecimal.valueOf(quantity)));
    }

    public long totalCents() {
        return total.movePointRight(2).setScale(0, RoundingMode.HALF_EVEN).longValueExact();
    }

    public BigDecimal total() {
        return total;
    }
}

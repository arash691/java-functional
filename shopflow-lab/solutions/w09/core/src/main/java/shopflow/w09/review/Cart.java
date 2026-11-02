package shopflow.w09.review;

import java.util.ArrayList;
import java.util.List;

public class Cart {

    public record Line(Sku sku, int quantity) {}

    private final List<Line> lines = new ArrayList<>();

    public void add(Sku sku, int quantity) {
        lines.add(new Line(sku, quantity));
    }

    public List<Line> lines() {
        return List.copyOf(lines);
    }

    /** Removes every line whose quantity is zero. */
    public void removeEmptyLines() {
        lines.removeIf(line -> line.quantity() == 0);
    }

    public boolean sameQuantity(Line a, Line b) {
        return a.quantity() == b.quantity();
    }
}

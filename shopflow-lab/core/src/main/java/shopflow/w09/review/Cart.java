package shopflow.w09.review;

import java.util.ArrayList;
import java.util.List;

public class Cart {

    public record Line(Sku sku, Integer quantity) {}

    private final List<Line> lines = new ArrayList<>();

    public void add(Sku sku, int quantity) {
        lines.add(new Line(sku, quantity));
    }

    public List<Line> lines() {
        return lines;
    }

    /** Removes every line whose quantity is zero. */
    public void removeEmptyLines() {
        for (Line line : lines) {
            if (line.quantity() == 0) {
                lines.remove(line);
            }
        }
    }

    public boolean sameQuantity(Line a, Line b) {
        return a.quantity() == b.quantity();
    }
}

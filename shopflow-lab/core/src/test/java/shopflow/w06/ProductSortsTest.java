package shopflow.w06;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("w06")
@DisplayName("EX-06-03 Comparable vs Comparator")
class ProductSortsTest {

    private final Product mugB = new Product("B-2", "mug", "mugs", 900);
    private final Product mugA = new Product("A-1", "Mug", "mugs", 800);
    private final Product tee = new Product("C-3", "Tee", "shirts", 800);
    private final Product cap = new Product("D-4", "Cap", "hats", 1500);

    private List<Product> shuffled() {
        return new ArrayList<>(List.of(mugB, tee, cap, mugA));
    }

    @Test
    @DisplayName("natural order (Comparable) is by SKU")
    void naturalOrderIsBySku() {
        List<Product> list = shuffled();
        Collections.sort(list);
        assertThat(list).containsExactly(mugA, mugB, tee, cap);
    }

    @Test
    void byPriceIsCheapestFirstAndStable() {
        List<Product> list = new ArrayList<>(List.of(tee, mugB, mugA, cap));
        list.sort(ProductSorts.byPrice());
        // tee (800) came before mugA (800) in the input and must stay before it
        assertThat(list).containsExactly(tee, mugA, mugB, cap);
    }

    @Test
    void byNameThenPriceIgnoresCase() {
        List<Product> list = shuffled();
        list.sort(ProductSorts.byNameThenPrice());
        // Cap, Mug(800), mug(900), Tee
        assertThat(list).containsExactly(cap, mugA, mugB, tee);
    }

    @Test
    void byPriceDescThenSku() {
        List<Product> list = shuffled();
        list.sort(ProductSorts.byPriceDescThenSku());
        // 1500 cap, 900 mugB, then 800s by sku: A-1 before C-3
        assertThat(list).containsExactly(cap, mugB, mugA, tee);
    }
}

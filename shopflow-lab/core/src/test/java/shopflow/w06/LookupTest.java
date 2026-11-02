package shopflow.w06;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("w06")
@DisplayName("EX-06-05 Big-O you can measure")
class LookupTest {

    private static List<String> haystack(int n) {
        List<String> list = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            list.add("sku-" + i);
        }
        return list;
    }

    private static List<String> queries(int n, int present) {
        List<String> q = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            q.add(i % 2 == 0 ? "sku-" + ((i * 37) % present) : "missing-" + i);
        }
        return q;
    }

    @Test
    @DisplayName("same answers as the naive version")
    void sameAnswers() {
        List<String> h = haystack(1000);
        List<String> q = queries(200, 1000);
        assertThat(Lookup.fastContains(h, q)).containsExactly(Lookup.naiveContains(h, q));
    }

    @Test
    @DisplayName("at least 10x faster than the naive version on 100,000 items")
    void muchFasterThanNaive() {
        List<String> h = haystack(100_000);
        List<String> q = queries(6000, 100_000);

        long t0 = System.nanoTime();
        boolean[] expected = Lookup.naiveContains(h, q);
        long naive = System.nanoTime() - t0;

        Lookup.fastContains(h, q); // warm-up
        long fast = Long.MAX_VALUE;
        for (int i = 0; i < 3; i++) {
            long s = System.nanoTime();
            boolean[] actual = Lookup.fastContains(h, q);
            fast = Math.min(fast, System.nanoTime() - s);
            assertThat(actual).containsExactly(expected);
        }

        assertThat(fast * 10)
                .as("naive took %d ms, fast took %d ms: fast must be at least 10x quicker",
                        naive / 1_000_000, fast / 1_000_000)
                .isLessThan(naive);
    }
}

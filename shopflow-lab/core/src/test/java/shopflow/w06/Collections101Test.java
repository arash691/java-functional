package shopflow.w06;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("w06")
@DisplayName("EX-06-01 List, Set, Map")
class Collections101Test {

    @Nested
    class UniquePreserveOrder {
        @Test
        void removesDuplicatesKeepingFirstSeenOrder() {
            assertThat(Collections101.uniquePreserveOrder(List.of("b", "a", "b", "c", "a")))
                    .containsExactly("b", "a", "c");
        }

        @Test
        void emptyInputGivesEmptyOutput() {
            assertThat(Collections101.uniquePreserveOrder(List.of())).isEmpty();
        }
    }

    @Nested
    class CountBySku {
        @Test
        void countsOccurrences() {
            assertThat(Collections101.countBySku(List.of("a", "b", "a", "a")))
                    .containsExactlyInAnyOrderEntriesOf(Map.of("a", 3, "b", 1));
        }

        @Test
        void emptyInputGivesEmptyMap() {
            assertThat(Collections101.countBySku(List.of())).isEmpty();
        }
    }

    @Nested
    class GroupByCategory {
        private final Product mug = new Product("M-1", "Blue mug", "mugs", 800);
        private final Product shirt = new Product("S-1", "Tee", "shirts", 1500);
        private final Product mug2 = new Product("M-2", "Red mug", "mugs", 900);

        @Test
        void groupsInFirstSeenCategoryOrder() {
            Map<String, List<Product>> grouped = Collections101.groupByCategory(List.of(mug, shirt, mug2));
            assertThat(grouped.keySet()).containsExactly("mugs", "shirts");
        }

        @Test
        void keepsProductOrderInsideACategory() {
            Map<String, List<Product>> grouped = Collections101.groupByCategory(List.of(mug, shirt, mug2));
            assertThat(grouped.get("mugs")).containsExactly(mug, mug2);
            assertThat(grouped.get("shirts")).containsExactly(shirt);
        }
    }
}

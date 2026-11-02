package shopflow.w06;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("w06")
@DisplayName("EX-06-04 Generics and PECS")
class Generics101Test {

    @Nested
    class MaxOf {
        @Test
        void integers() {
            assertThat(Generics101.maxOf(List.of(3, 9, 2))).isEqualTo(9);
        }

        @Test
        @DisplayName("works for LocalDate, whose compareTo is declared on a supertype (? super T)")
        void localDates() {
            Set<LocalDate> dates = Set.of(LocalDate.of(2026, 1, 5), LocalDate.of(2026, 3, 1));
            assertThat(Generics101.maxOf(dates)).isEqualTo(LocalDate.of(2026, 3, 1));
        }

        @Test
        void emptyThrows() {
            assertThatThrownBy(() -> Generics101.maxOf(List.<Integer>of())).isInstanceOf(NoSuchElementException.class);
        }
    }

    @Nested
    class CopyAll {
        @Test
        @DisplayName("copies Integers into a List<Number> (consumer super, producer extends)")
        void intoSupertypeList() {
            List<Number> dst = new ArrayList<>(List.of(1.5));
            List<Integer> src = List.of(1, 2);
            Generics101.copyAll(dst, src);
            assertThat(dst).containsExactly(1.5, 1, 2);
        }

        @Test
        void sourceIsNotModified() {
            List<Integer> src = new ArrayList<>(List.of(1, 2));
            Generics101.copyAll(new ArrayList<Object>(), src);
            assertThat(src).containsExactly(1, 2);
        }
    }

    @Nested
    class KeyOfMaxValue {
        @Test
        void returnsTheKeyWithTheLargestValue() {
            assertThat(Generics101.keyOfMaxValue(Map.of("a", 3, "b", 7, "c", 5))).isEqualTo("b");
        }

        @Test
        void emptyThrows() {
            assertThatThrownBy(() -> Generics101.keyOfMaxValue(Map.<String, Integer>of()))
                    .isInstanceOf(NoSuchElementException.class);
        }
    }
}

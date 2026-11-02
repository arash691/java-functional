package shopflow.w08;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/** INC-08-B: "some orders in the nightly import end up on the wrong date, and not always the same ones". */
@Tag("w08")
class OrderDateParserTest {

    @Test
    @DisplayName("guard: one thread at a time always works")
    void singleThreadedParsingIsCorrect() {
        assertThat(OrderDateParser.parse("2025-03-09")).isEqualTo(LocalDate.of(2025, 3, 9));
        assertThat(OrderDateParser.parse("2024-02-29")).isEqualTo(LocalDate.of(2024, 2, 29));
    }

    @Test
    @DisplayName("guard: garbage is rejected with IllegalArgumentException")
    void rejectsGarbage() {
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> OrderDateParser.parse("not a date"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("many threads parsing at once all get the right answer")
    void parsingIsCorrectUnderConcurrency() throws Exception {
        int threads = 8;
        int iterationsPerThread = 20_000;
        ExecutorService pool = Executors.newFixedThreadPool(threads);
        CyclicBarrier startTogether = new CyclicBarrier(threads);
        AtomicInteger problems = new AtomicInteger();
        AtomicReference<String> firstProblem = new AtomicReference<>();

        for (int t = 0; t < threads; t++) {
            LocalDate base = LocalDate.of(2025, 1, 1).plusDays(t * 40L);
            pool.submit(() -> {
                try {
                    startTogether.await(5, TimeUnit.SECONDS);
                    for (int i = 0; i < iterationsPerThread; i++) {
                        LocalDate expected = base.plusDays(i % 30);
                        try {
                            LocalDate actual = OrderDateParser.parse(expected.toString());
                            if (!expected.equals(actual)) {
                                problems.incrementAndGet();
                                firstProblem.compareAndSet(null, "parsed " + expected + " but got " + actual);
                            }
                        } catch (RuntimeException e) {
                            problems.incrementAndGet();
                            firstProblem.compareAndSet(null, "parsing " + expected + " threw " + e);
                        }
                    }
                } catch (Exception e) {
                    problems.incrementAndGet();
                    firstProblem.compareAndSet(null, "worker failed: " + e);
                }
            });
        }
        pool.shutdown();
        assertThat(pool.awaitTermination(30, TimeUnit.SECONDS)).as("workers finished").isTrue();

        assertThat(problems.get())
                .as("wrong or failed parses out of %d (first problem: %s)", threads * iterationsPerThread, firstProblem.get())
                .isZero();
    }
}

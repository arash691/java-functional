package shopflow.w09;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;

@Tag("w09")
class LoggingExercisesTest {

    @Nested
    @DisplayName("EX-09-01 parameterised message")
    class Placed {
        @Test
        void logsOneInfoEvent() {
            try (LogCapture logs = new LogCapture(OrderLogger.class)) {
                new OrderLogger().logPlaced("A-1", 1250);

                assertThat(logs.events()).hasSize(1);
                assertThat(logs.events().get(0).getLevel()).isEqualTo(Level.INFO);
            }
        }

        @Test
        void messageReadsNaturally() {
            try (LogCapture logs = new LogCapture(OrderLogger.class)) {
                new OrderLogger().logPlaced("A-1", 1250);

                assertThat(logs.messages()).containsExactly("Order placed: id=A-1 cents=1250");
            }
        }

        @Test
        void usesPlaceholdersNotStringConcatenation() {
            try (LogCapture logs = new LogCapture(OrderLogger.class)) {
                new OrderLogger().logPlaced("A-1", 1250);

                assertThat(logs.events()).hasSize(1);
                ILoggingEvent event = logs.events().get(0);
                assertThat(event.getMessage()).isEqualTo("Order placed: id={} cents={}");
                assertThat(event.getArgumentArray()).containsExactly("A-1", 1250L);
            }
        }
    }

    @Nested
    @DisplayName("EX-09-02 pick the right level")
    class Levels {
        @Test
        void lowStockIsAWarning() {
            try (LogCapture logs = new LogCapture(StockAlerts.class)) {
                new StockAlerts().check("SKU-1", 2, 5);

                assertThat(logs.events()).hasSize(1);
                assertThat(logs.events().get(0).getLevel()).isEqualTo(Level.WARN);
                assertThat(logs.messages()).containsExactly("Low stock: sku=SKU-1 qty=2 threshold=5");
            }
        }

        @Test
        void healthyStockIsOnlyDebug() {
            try (LogCapture logs = new LogCapture(StockAlerts.class)) {
                new StockAlerts().check("SKU-1", 50, 5);

                assertThat(logs.events()).hasSize(1);
                assertThat(logs.events().get(0).getLevel()).isEqualTo(Level.DEBUG);
                assertThat(logs.messages()).containsExactly("Stock ok: sku=SKU-1 qty=50");
            }
        }

        @Test
        void exactlyAtTheThresholdIsNotLow() {
            try (LogCapture logs = new LogCapture(StockAlerts.class)) {
                new StockAlerts().check("SKU-1", 5, 5);

                assertThat(logs.events().get(0).getLevel()).isEqualTo(Level.DEBUG);
            }
        }
    }

    @Nested
    @DisplayName("EX-09-03 MDC context with guaranteed cleanup")
    class Mdc {
        @Test
        void requestIdIsVisibleInsideTheAction() {
            List<String> seen = new ArrayList<>();
            RequestScope.run("req-42", () -> seen.add(MDC.get("requestId")));
            assertThat(seen).containsExactly("req-42");
        }

        @Test
        void requestIdIsGoneAfterwards() {
            RequestScope.run("req-42", () -> {});
            assertThat(MDC.get("requestId")).isNull();
        }

        @Test
        void requestIdIsGoneEvenWhenTheActionFails() {
            assertThatThrownBy(() -> RequestScope.run("req-42", () -> {
                        throw new IllegalStateException("boom");
                    }))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("boom");
            assertThat(MDC.get("requestId")).isNull();
        }
    }

    @Nested
    @DisplayName("EX-09-04 keep the stack trace")
    class Failures {
        @Test
        void logsAtErrorWithTheOrderId() {
            try (LogCapture logs = new LogCapture(FailureLogger.class)) {
                new FailureLogger().logFailure("A-7", new IllegalStateException("payment declined"));

                assertThat(logs.events()).hasSize(1);
                assertThat(logs.events().get(0).getLevel()).isEqualTo(Level.ERROR);
                assertThat(logs.messages().get(0)).contains("A-7");
            }
        }

        @Test
        void attachesTheExceptionToTheEvent() {
            try (LogCapture logs = new LogCapture(FailureLogger.class)) {
                new FailureLogger().logFailure("A-7", new IllegalStateException("payment declined"));

                assertThat(logs.events().get(0).getThrowableProxy())
                        .as("the exception must be passed as the LAST argument so the stack trace is kept")
                        .isNotNull();
                assertThat(logs.events().get(0).getThrowableProxy().getMessage()).isEqualTo("payment declined");
            }
        }
    }
}

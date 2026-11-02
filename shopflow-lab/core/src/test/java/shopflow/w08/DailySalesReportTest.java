package shopflow.w08;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/** INC-08-A: the daily sales report for a New York shop is wrong on servers in other zones. */
@Tag("w08")
class DailySalesReportTest {

    private static final ZoneId NEW_YORK = ZoneId.of("America/New_York");

    private final List<DailySalesReport.Sale> sales = List.of(
            // 19:30 EDT, March 9
            new DailySalesReport.Sale("A-1", Instant.parse("2025-03-09T23:30:00Z"), 1_000),
            // 22:00 EDT, March 9
            new DailySalesReport.Sale("A-2", Instant.parse("2025-03-10T02:00:00Z"), 500),
            // 01:00 EDT, March 10
            new DailySalesReport.Sale("A-3", Instant.parse("2025-03-10T05:00:00Z"), 200));

    private final Map<LocalDate, Long> expectedForTheShop =
            Map.of(LocalDate.of(2025, 3, 9), 1_500L, LocalDate.of(2025, 3, 10), 200L);

    private TimeZone originalDefault;

    @BeforeEach
    void rememberDefaultZone() {
        originalDefault = TimeZone.getDefault();
    }

    @AfterEach
    void restoreDefaultZone() {
        TimeZone.setDefault(originalDefault);
    }

    @Test
    @DisplayName("guard: works when the server happens to run in the shop's own zone")
    void worksWhenTheServerRunsInTheShopZone() {
        TimeZone.setDefault(TimeZone.getTimeZone(NEW_YORK));
        assertThat(new DailySalesReport(NEW_YORK).totalsByDay(sales)).isEqualTo(expectedForTheShop);
    }

    @ParameterizedTest(name = "server default zone = {0}")
    @ValueSource(strings = {"UTC", "Asia/Tokyo", "America/Los_Angeles", "Pacific/Kiritimati"})
    @DisplayName("a New York shop gets the same report wherever the server runs")
    void sameReportOnAnyServer(String serverZone) {
        TimeZone.setDefault(TimeZone.getTimeZone(serverZone));
        assertThat(new DailySalesReport(NEW_YORK).totalsByDay(sales))
                .as("report computed on a server whose default zone is %s", serverZone)
                .isEqualTo(expectedForTheShop);
    }

    @Test
    void titleNamesTheShopZone() {
        assertThat(new DailySalesReport(NEW_YORK).title()).isEqualTo("Daily sales (America/New_York)");
    }
}

package shopflow.w08;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.time.ZoneId;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/** Green from the start: this is the worked example's safety net. */
@Tag("w08")
class TimeTourTest {

    @Test
    void sameInstantReadsDifferentlyOnDifferentWallClocks() {
        Instant instant = Instant.parse("2025-03-09T07:00:00Z");

        assertThat(TimeTour.describe(instant, ZoneId.of("UTC"))).isEqualTo("2025-03-09T07:00Z[UTC]");
        assertThat(TimeTour.describe(instant, ZoneId.of("America/New_York")))
                .isEqualTo("2025-03-09T03:00-04:00[America/New_York]");
        assertThat(TimeTour.describe(instant, ZoneId.of("Asia/Tokyo")))
                .isEqualTo("2025-03-09T16:00+09:00[Asia/Tokyo]");
    }
}

package shopflow.w08;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Totals sales per calendar day. A "day" is a day on the SHOP's wall clock (the shop's zone),
 * no matter where the server runs.
 */
public class DailySalesReport {

    public record Sale(String orderId, Instant placedAt, long cents) {}

    private final ZoneId shopZone;

    public DailySalesReport(ZoneId shopZone) {
        this.shopZone = shopZone;
    }

    public String title() {
        return "Daily sales (" + shopZone + ")";
    }

    public Map<LocalDate, Long> totalsByDay(List<Sale> sales) {
        Map<LocalDate, Long> totals = new TreeMap<>();
        for (Sale sale : sales) {
            totals.merge(dayOf(sale), sale.cents(), Long::sum);
        }
        return totals;
    }

    private LocalDate dayOf(Sale sale) {
        return new java.sql.Date(sale.placedAt().toEpochMilli()).toLocalDate();
    }
}

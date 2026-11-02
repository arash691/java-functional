package shopflow.w06.promo;

import java.time.LocalDate;
import java.util.List;

public class PromotionCleanup {

    public int removeExpired(List<Promotion> promotions, LocalDate today) {
        int before = promotions.size();
        promotions.removeIf(p -> p.isExpiredOn(today));
        return before - promotions.size();
    }
}

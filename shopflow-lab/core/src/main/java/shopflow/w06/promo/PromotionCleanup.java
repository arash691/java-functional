package shopflow.w06.promo;

import java.time.LocalDate;
import java.util.List;

/** Nightly job: drops promotions that expired before {@code today}. */
public class PromotionCleanup {

    /** Removes expired promotions from the list and returns how many were removed. */
    public int removeExpired(List<Promotion> promotions, LocalDate today) {
        int removed = 0;
        for (Promotion p : promotions) {
            if (p.isExpiredOn(today)) {
                promotions.remove(p);
                removed++;
            }
        }
        return removed;
    }
}

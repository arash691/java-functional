package shopflow.w06.promo;

import java.time.LocalDate;

public record Promotion(String code, LocalDate expiresOn) {

    public boolean isExpiredOn(LocalDate today) {
        return expiresOn.isBefore(today);
    }
}

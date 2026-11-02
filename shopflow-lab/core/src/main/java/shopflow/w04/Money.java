package shopflow.w04;

import java.math.BigDecimal;

/**
 * SF-02: Money value object. Immutable. Amount is stored with scale 2 (rounded HALF_EVEN),
 * currency is a three-letter uppercase code.
 *
 * Implement everything MoneyTest asks for. (MoneyTest asks for equals and toString.
 * Whether it also needs hashCode is something you will discover in session 3.)
 */
public final class Money {

    private Money() {}

    public static Money of(String amount, String currencyCode) {
        throw new UnsupportedOperationException("TODO SF-02 of");
    }

    public static Money zero(String currencyCode) {
        throw new UnsupportedOperationException("TODO SF-02 zero");
    }

    public BigDecimal amount() {
        throw new UnsupportedOperationException("TODO SF-02 amount");
    }

    public String currency() {
        throw new UnsupportedOperationException("TODO SF-02 currency");
    }

    public Money add(Money other) {
        throw new UnsupportedOperationException("TODO SF-02 add");
    }

    public Money subtract(Money other) {
        throw new UnsupportedOperationException("TODO SF-02 subtract");
    }

    public Money multiply(int factor) {
        throw new UnsupportedOperationException("TODO SF-02 multiply");
    }

    public boolean isNegative() {
        throw new UnsupportedOperationException("TODO SF-02 isNegative");
    }

    public boolean isPositive() {
        throw new UnsupportedOperationException("TODO SF-02 isPositive");
    }
}

package shopflow.w03;

public final class Pricing {

    private Pricing() {}

    public static int lineTotalCents(int unitPriceCents, int quantity) {
        if (quantity < 0) {
            throw new IllegalArgumentException("quantity must be >= 0: " + quantity);
        }
        return unitPriceCents * quantity;
    }

    public static int applyDiscountPercent(int cents, int percent) {
        if (percent < 0 || percent > 100) {
            throw new IllegalArgumentException("percent must be 0..100: " + percent);
        }
        int discount = cents * percent / 100;
        return cents - discount;
    }

    public static String format(int cents) {
        int euros = cents / 100;
        int rest = cents % 100;
        String restText = rest < 10 ? "0" + rest : String.valueOf(rest);
        return euros + "." + restText;
    }

    public static String format(int cents, String currencyCode) {
        return currencyCode + " " + format(cents);
    }
}

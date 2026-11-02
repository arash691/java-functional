package shopflow.w07;

import java.util.List;

public final class Strings101 {
    private Strings101() {}

    public static String joinSkus(List<String> skus) {
        StringBuilder sb = new StringBuilder();
        for (String sku : skus) {
            if (!sb.isEmpty()) {
                sb.append(',');
            }
            sb.append(sku);
        }
        return sb.toString();
    }

    public static String maskCardNumber(String number) {
        if (number == null || number.length() < 4) {
            throw new IllegalArgumentException("card number must have at least 4 characters");
        }
        return "*".repeat(number.length() - 4) + number.substring(number.length() - 4);
    }

    public static String slugify(String title) {
        return title.toLowerCase()
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-+|-+$", "");
    }
}

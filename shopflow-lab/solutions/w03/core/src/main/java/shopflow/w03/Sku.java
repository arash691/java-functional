package shopflow.w03;

public final class Sku {

    private Sku() {}

    public static boolean isValid(String sku) {
        if (sku == null || sku.length() != 8) {
            return false;
        }
        for (int i = 0; i < 3; i++) {
            char c = sku.charAt(i);
            if (c < 'A' || c > 'Z') {
                return false;
            }
        }
        if (sku.charAt(3) != '-') {
            return false;
        }
        for (int i = 4; i < 8; i++) {
            char c = sku.charAt(i);
            if (c < '0' || c > '9') {
                return false;
            }
        }
        return true;
    }

    public static String prefix(String sku) {
        requireValid(sku);
        return sku.substring(0, 3);
    }

    public static int number(String sku) {
        requireValid(sku);
        return Integer.parseInt(sku.substring(4));
    }

    private static void requireValid(String sku) {
        if (!isValid(sku)) {
            throw new IllegalArgumentException("Invalid SKU: " + sku);
        }
    }
}

package shopflow.w03;

/**
 * EX-03-02: loops, chars and String methods (no regular expressions yet).
 * A valid SKU looks like "ABC-1234": three uppercase letters A-Z, a hyphen, four digits 0-9.
 */
public final class Sku {

    private Sku() {}

    public static boolean isValid(String sku) {
        throw new UnsupportedOperationException("TODO EX-03-02 isValid");
    }

    /** "ABC-1234" -> "ABC". Throws IllegalArgumentException if the SKU is not valid. */
    public static String prefix(String sku) {
        throw new UnsupportedOperationException("TODO EX-03-02 prefix");
    }

    /** "ABC-0042" -> 42. Throws IllegalArgumentException if the SKU is not valid. */
    public static int number(String sku) {
        throw new UnsupportedOperationException("TODO EX-03-02 number");
    }
}

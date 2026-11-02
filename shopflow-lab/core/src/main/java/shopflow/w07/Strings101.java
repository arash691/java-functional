package shopflow.w07;

import java.util.List;

/** EX-07-04: String and StringBuilder. */
public final class Strings101 {
    private Strings101() {}

    /** [A, B, C] -> "A,B,C"; empty list -> "". Build it with a StringBuilder (no String += in a loop). */
    public static String joinSkus(List<String> skus) {
        return "TODO";
    }

    /** "4111111111111111" -> "************1111". Throws IllegalArgumentException for null or fewer than 4 characters. */
    public static String maskCardNumber(String number) {
        return "TODO";
    }

    /** "  Blue Mug (Large)! " -> "blue-mug-large": lowercase, runs of non a-z0-9 become one '-', no leading/trailing '-'. */
    public static String slugify(String title) {
        return "TODO";
    }
}

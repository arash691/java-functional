package shopflow.w07.example;

/** Worked example (green): translate a low-level exception into a domain one, keeping the cause. */
public final class QuantityParser {
    private QuantityParser() {}

    public static int parse(String text) {
        int quantity;
        try {
            quantity = Integer.parseInt(text.trim());
        } catch (NumberFormatException e) {
            throw new InvalidQuantityException("Quantity is not a number: '" + text + "'", e);
        }
        if (quantity <= 0) {
            throw new InvalidQuantityException("Quantity must be positive but was " + quantity);
        }
        return quantity;
    }

    public static void main(String[] args) {
        System.out.println(parse(" 12 "));
        try {
            parse("twelve");
        } catch (InvalidQuantityException e) {
            System.out.println(e.getMessage());
            System.out.println("cause: " + e.getCause());
        }
    }
}

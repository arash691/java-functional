package shopflow.w08;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

public final class OrderDateParser {

    private OrderDateParser() {}

    public static LocalDate parse(String text) {
        try {
            return LocalDate.parse(text);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Not a yyyy-MM-dd date: " + text, e);
        }
    }
}

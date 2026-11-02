package shopflow.w08;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

/** Parses the {@code yyyy-MM-dd} dates that arrive in the nightly import file. */
public final class OrderDateParser {

    private static final SimpleDateFormat FORMAT = new SimpleDateFormat("yyyy-MM-dd");

    private OrderDateParser() {}

    public static LocalDate parse(String text) {
        try {
            Date date = FORMAT.parse(text);
            return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        } catch (ParseException e) {
            throw new IllegalArgumentException("Not a yyyy-MM-dd date: " + text, e);
        }
    }
}

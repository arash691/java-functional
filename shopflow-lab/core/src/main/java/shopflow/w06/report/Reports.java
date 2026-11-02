package shopflow.w06.report;

import java.util.LinkedList;
import java.util.List;

/** Daily totals report. */
public final class Reports {
    private Reports() {}

    /** The list the importer fills with order amounts. */
    public static List<Integer> newAmountsList() {
        // LinkedList: we append a lot, and inserts are O(1)
        return new LinkedList<>();
    }

    public static long sumAll(List<Integer> amounts) {
        long total = 0;
        for (int i = 0; i < amounts.size(); i++) {
            total += amounts.get(i);
        }
        return total;
    }
}

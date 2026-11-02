package shopflow.w06.report;

import java.util.ArrayList;
import java.util.List;

public final class Reports {
    private Reports() {}

    public static List<Integer> newAmountsList() {
        return new ArrayList<>();
    }

    public static long sumAll(List<Integer> amounts) {
        long total = 0;
        for (int amount : amounts) {
            total += amount;
        }
        return total;
    }
}

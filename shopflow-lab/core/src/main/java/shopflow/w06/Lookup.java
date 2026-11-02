package shopflow.w06;

import java.util.List;

/** EX-06-05: Big-O you can measure. */
public final class Lookup {
    private Lookup() {}

    /** Reference implementation: O(haystack) per query. Do not change. */
    public static boolean[] naiveContains(List<String> haystack, List<String> queries) {
        boolean[] result = new boolean[queries.size()];
        for (int i = 0; i < queries.size(); i++) {
            result[i] = haystack.contains(queries.get(i));
        }
        return result;
    }

    /** Same answers as naiveContains but at least 10x faster on large inputs. */
    public static boolean[] fastContains(List<String> haystack, List<String> queries) {
        return naiveContains(haystack, queries);
    }
}

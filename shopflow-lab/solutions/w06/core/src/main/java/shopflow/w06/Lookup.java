package shopflow.w06;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class Lookup {
    private Lookup() {}

    public static boolean[] naiveContains(List<String> haystack, List<String> queries) {
        boolean[] result = new boolean[queries.size()];
        for (int i = 0; i < queries.size(); i++) {
            result[i] = haystack.contains(queries.get(i));
        }
        return result;
    }

    public static boolean[] fastContains(List<String> haystack, List<String> queries) {
        Set<String> index = new HashSet<>(haystack);
        boolean[] result = new boolean[queries.size()];
        for (int i = 0; i < queries.size(); i++) {
            result[i] = index.contains(queries.get(i));
        }
        return result;
    }
}

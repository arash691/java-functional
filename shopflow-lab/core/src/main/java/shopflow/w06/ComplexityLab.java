package shopflow.w06;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * Worked example (green): measure instead of guessing. Predict which line is slowest, then run main.
 * This is a rough timing, not a benchmark (no JIT warm-up); it is good enough to see O(1) vs O(n) vs O(n^2).
 */
public class ComplexityLab {

    public static void main(String[] args) {
        int n = 50_000;
        List<Integer> array = new ArrayList<>();
        List<Integer> linked = new LinkedList<>();
        Set<Integer> hash = new HashSet<>();
        for (int i = 0; i < n; i++) {
            array.add(i);
            linked.add(i);
            hash.add(i);
        }
        System.out.printf("get(i) all, ArrayList : %6.1f ms%n", time(() -> getAll(array)));
        System.out.printf("get(i) all, LinkedList: %6.1f ms%n", time(() -> getAll(linked)));
        System.out.printf("contains x2000, ArrayList: %6.1f ms%n", time(() -> containsMany(array, 2000)));
        System.out.printf("contains x2000, HashSet  : %6.1f ms%n", time(() -> containsMany(hash, 2000)));
    }

    static void getAll(List<Integer> list) {
        long sum = 0;
        for (int i = 0; i < list.size(); i++) {
            sum += list.get(i);
        }
        if (sum < 0) throw new IllegalStateException();
    }

    static void containsMany(java.util.Collection<Integer> c, int queries) {
        int hits = 0;
        for (int i = 0; i < queries; i++) {
            if (c.contains(-i - 1)) hits++;
        }
        if (hits > 0) throw new IllegalStateException();
    }

    static double time(Runnable r) {
        long start = System.nanoTime();
        r.run();
        return (System.nanoTime() - start) / 1_000_000.0;
    }
}

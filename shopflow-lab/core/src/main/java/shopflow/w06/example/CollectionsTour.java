package shopflow.w06.example;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;

/** Worked example (green). Run main, predict each line BEFORE you look at the output. */
public class CollectionsTour {

    public static void main(String[] args) {
        List<String> list = List.of("b", "a", "b", "c");
        Set<String> set = new LinkedHashSet<>(list);
        Map<String, Integer> counts = new HashMap<>();
        for (String s : list) {
            counts.merge(s, 1, Integer::sum);
        }
        Queue<Integer> pq = new PriorityQueue<>(List.of(5, 1, 3));
        Deque<String> deque = new ArrayDeque<>();
        deque.addFirst("x");
        deque.addFirst("y");
        deque.addLast("z");

        System.out.println("list   = " + list);
        System.out.println("set    = " + set);
        System.out.println("counts = " + new java.util.TreeMap<>(counts));
        System.out.println("pq.poll= " + pq.poll() + " then " + pq.poll());
        System.out.println("deque  = " + deque);
    }

    public static Map<String, Integer> countLetters(List<String> letters) {
        Map<String, Integer> counts = new HashMap<>();
        for (String s : letters) {
            counts.merge(s, 1, Integer::sum);
        }
        return counts;
    }

    public static Set<String> dedupe(List<String> letters) {
        return new LinkedHashSet<>(letters);
    }
}

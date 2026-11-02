package shopflow.w06;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

public final class Generics101 {
    private Generics101() {}

    public static <T extends Comparable<? super T>> T maxOf(Collection<? extends T> items) {
        Iterator<? extends T> it = items.iterator();
        if (!it.hasNext()) {
            throw new NoSuchElementException("empty collection");
        }
        T best = it.next();
        while (it.hasNext()) {
            T candidate = it.next();
            if (candidate.compareTo(best) > 0) {
                best = candidate;
            }
        }
        return best;
    }

    public static <T> void copyAll(List<? super T> dst, List<? extends T> src) {
        for (T item : src) {
            dst.add(item);
        }
    }

    public static <K, V extends Comparable<? super V>> K keyOfMaxValue(Map<K, V> map) {
        Iterator<Map.Entry<K, V>> it = map.entrySet().iterator();
        if (!it.hasNext()) {
            throw new NoSuchElementException("empty map");
        }
        Map.Entry<K, V> best = it.next();
        while (it.hasNext()) {
            Map.Entry<K, V> e = it.next();
            if (e.getValue().compareTo(best.getValue()) > 0) {
                best = e;
            }
        }
        return best.getKey();
    }
}

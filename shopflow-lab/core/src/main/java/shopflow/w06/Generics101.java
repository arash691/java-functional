package shopflow.w06;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/** EX-06-04: generics, bounded types and PECS (Producer Extends, Consumer Super). */
public final class Generics101 {
    private Generics101() {}

    /**
     * Largest element by natural order. Must accept a {@code List<Integer>}, a {@code Set<Product>} and any
     * type whose compareTo is declared on a supertype. Throws NoSuchElementException when empty.
     */
    public static <T extends Comparable<? super T>> T maxOf(Collection<? extends T> items) {
        throw new UnsupportedOperationException("TODO EX-06-04");
    }

    /** Copies every element of src to the end of dst. dst may be a list of a supertype. */
    public static <T> void copyAll(List<? super T> dst, List<? extends T> src) {
        throw new UnsupportedOperationException("TODO EX-06-04");
    }

    /** The key whose value is largest. Throws NoSuchElementException for an empty map. */
    public static <K, V extends Comparable<? super V>> K keyOfMaxValue(Map<K, V> map) {
        throw new UnsupportedOperationException("TODO EX-06-04");
    }
}

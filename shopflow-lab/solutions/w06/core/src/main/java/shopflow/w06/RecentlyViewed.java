package shopflow.w06;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

public class RecentlyViewed {
    private final int capacity;
    private final Deque<String> deque = new ArrayDeque<>();

    public RecentlyViewed(int capacity) {
        this.capacity = capacity;
    }

    public void view(String sku) {
        deque.remove(sku);
        deque.addFirst(sku);
        if (deque.size() > capacity) {
            deque.removeLast();
        }
    }

    public List<String> items() {
        return new ArrayList<>(deque);
    }
}

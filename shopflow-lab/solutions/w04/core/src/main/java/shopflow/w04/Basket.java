package shopflow.w04;

import java.util.List;

public final class Basket {

    private final List<String> skus;

    public Basket(List<String> skus) {
        this.skus = List.copyOf(skus);
    }

    public List<String> skus() {
        return skus;
    }

    public int size() {
        return skus.size();
    }
}

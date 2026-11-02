package shopflow.w04;

import java.util.List;

/**
 * EX-04-04: defensive copies.
 * A Basket must not change just because the caller changes the list they passed in,
 * and callers must not be able to change the Basket through skus().
 */
public final class Basket {

    private final List<String> skus;

    public Basket(List<String> skus) {
        this.skus = skus; // TODO EX-04-04: this stores the caller's list. Is that safe?
    }

    public List<String> skus() {
        return skus; // TODO EX-04-04: this hands out the internal list. Is that safe?
    }

    public int size() {
        return skus.size();
    }
}

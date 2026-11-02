package shopflow.w05.loyalty;

import java.util.HashMap;
import java.util.Map;

/** Loyalty points per customer. */
public class LoyaltyLedger {
    private final Map<Customer, Integer> points = new HashMap<>();

    public void add(Customer customer, int amount) {
        points.merge(customer, amount, Integer::sum);
    }

    public int pointsOf(Customer customer) {
        return points.getOrDefault(customer, 0);
    }

    public int totalPoints() {
        return points.values().stream().mapToInt(Integer::intValue).sum();
    }

    public int customerCount() {
        return points.size();
    }
}

package shopflow.w09.review;

import java.util.HashMap;
import java.util.Map;

public class LoyaltyPoints {

    private final Map<Customer, Integer> points = new HashMap<>();

    public void add(Customer customer, int amount) {
        points.merge(customer, amount, Integer::sum);
    }

    public int pointsOf(Customer customer) {
        return points.getOrDefault(customer, 0);
    }
}

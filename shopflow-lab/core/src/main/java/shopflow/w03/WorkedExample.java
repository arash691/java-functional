package shopflow.w03;

/**
 * Worked example for Week 3, session 1. This code already works.
 * Run it, read it line by line, then change one thing at a time (see the lesson).
 *
 * Run from the repo root:
 *   java core/src/main/java/shopflow/w03/WorkedExample.java
 */
public class WorkedExample {

    public static void main(String[] args) {
        int stock = 3;                       // primitive int: no null, no methods
        String label = classify(stock);      // String: immutable reference type
        System.out.println("stock=" + stock + " -> " + label);

        for (int i = 0; i < 3; i++) {
            System.out.println("sum(" + i + ", 10) = " + add(i, 10));
        }
    }

    public static int add(int a, int b) {
        return a + b;
    }

    public static String classify(int stock) {
        if (stock == 0) {
            return "OUT";
        } else if (stock < 5) {
            return "LOW";
        }
        return "OK";
    }
}

package shopflow.w03;

public final class Shipping {

    private Shipping() {}

    public static int costCents(String zone, int weightGrams) {
        if (weightGrams <= 0) {
            throw new IllegalArgumentException("weight must be positive: " + weightGrams);
        }
        int base = switch (zone) {
            case "EU" -> 500;
            case "US" -> 1200;
            case "ASIA" -> 1500;
            default -> throw new IllegalArgumentException("Unknown zone: " + zone);
        };
        int surcharge;
        if (weightGrams <= 1000) {
            surcharge = 0;
        } else if (weightGrams <= 5000) {
            surcharge = 400;
        } else {
            surcharge = 1200;
        }
        return base + surcharge;
    }
}

package shopflow.w05;

public class StandardShipping extends ShippingMethod {
    @Override
    public String name() {
        return "Standard";
    }

    @Override
    protected long costCents(int weightGrams) {
        int startedKilos = (weightGrams + 999) / 1000;
        return 400 + 100L * startedKilos;
    }
}

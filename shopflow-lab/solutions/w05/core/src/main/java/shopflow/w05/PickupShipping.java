package shopflow.w05;

public class PickupShipping extends ShippingMethod {
    @Override
    public String name() {
        return "Pickup";
    }

    @Override
    protected long costCents(int weightGrams) {
        return 0;
    }
}

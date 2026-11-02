package shopflow.w05;

/** Customer collects the parcel: always free. */
public class PickupShipping extends ShippingMethod {
    @Override
    public String name() {
        return "Pickup";
    }

    @Override
    protected long costCents(int weightGrams) {
        return -1;
    }
}

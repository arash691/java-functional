package shopflow.w05;

/** 4.00 base + 1.00 per started kilogram. 1000 g -> 5.00, 1001 g -> 6.00. */
public class StandardShipping extends ShippingMethod {
    @Override
    public String name() {
        return "Standard";
    }

    @Override
    protected long costCents(int weightGrams) {
        return -1;
    }
}

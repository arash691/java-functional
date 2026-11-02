package shopflow.w05;

/** Twice the Standard price plus 3.00. Reuse StandardShipping instead of copying its formula. */
public class ExpressShipping extends ShippingMethod {
    @Override
    public String name() {
        return "Express";
    }

    @Override
    protected long costCents(int weightGrams) {
        return -1;
    }
}

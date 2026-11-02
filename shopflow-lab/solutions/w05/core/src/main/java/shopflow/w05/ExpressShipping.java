package shopflow.w05;

public class ExpressShipping extends ShippingMethod {
    private final StandardShipping standard = new StandardShipping();

    @Override
    public String name() {
        return "Express";
    }

    @Override
    protected long costCents(int weightGrams) {
        return 2 * standard.costCents(weightGrams) + 300;
    }
}

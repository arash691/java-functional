package shopflow.w05.example;

public class EmailNotifier extends BaseNotifier {
    @Override
    protected String channel() {
        return "email";
    }

    @Override
    protected String format(String message) {
        return "Subject: ShopFlow | " + message;
    }
}

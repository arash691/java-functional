package shopflow.w05.example;

public class SmsNotifier extends BaseNotifier {
    @Override
    protected String channel() {
        return "sms";
    }

    @Override
    protected String format(String message) {
        return message.length() <= 20 ? message : message.substring(0, 17) + "...";
    }
}

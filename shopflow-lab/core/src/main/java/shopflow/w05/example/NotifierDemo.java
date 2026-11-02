package shopflow.w05.example;

import java.util.List;

/** Run this main first (S1 step 1). Predict the output before you run it. */
public class NotifierDemo {
    public static void main(String[] args) {
        List<Notifier> notifiers = List.of(new EmailNotifier(), new SmsNotifier());
        for (Notifier n : notifiers) {
            System.out.print(n.sendAll(List.of("sara", "omid"), "Your order has shipped today"));
        }
    }
}

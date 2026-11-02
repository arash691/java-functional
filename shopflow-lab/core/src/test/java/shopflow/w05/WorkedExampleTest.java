package shopflow.w05;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import shopflow.w05.example.EmailNotifier;
import shopflow.w05.example.Notifier;
import shopflow.w05.example.SmsNotifier;

@Tag("w05")
@DisplayName("Worked example (green): interface + abstract class")
class WorkedExampleTest {

    @Test
    void emailFormatsWithSubject() {
        assertThat(new EmailNotifier().send("sara", "Hi")).isEqualTo("[email] to=sara Subject: ShopFlow | Hi");
    }

    @Test
    void smsTruncatesLongMessages() {
        assertThat(new SmsNotifier().send("omid", "Your order has shipped today"))
                .isEqualTo("[sms] to=omid Your order has sh...");
    }

    @Test
    void defaultMethodWorksOnAnyImplementation() {
        Notifier n = new SmsNotifier();
        assertThat(n.sendAll(List.of("a", "b"), "Hi")).isEqualTo("[sms] to=a Hi\n[sms] to=b Hi\n");
    }
}

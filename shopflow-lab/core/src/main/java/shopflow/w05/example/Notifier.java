package shopflow.w05.example;

/** Worked example (already green): an interface with a default method. */
public interface Notifier {
    String send(String to, String message);

    default String sendAll(Iterable<String> recipients, String message) {
        StringBuilder log = new StringBuilder();
        for (String r : recipients) {
            log.append(send(r, message)).append('\n');
        }
        return log.toString();
    }
}

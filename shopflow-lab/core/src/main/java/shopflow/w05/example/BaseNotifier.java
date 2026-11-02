package shopflow.w05.example;

/** Worked example: abstract class = shared skeleton + one hook ({@link #format}) for subclasses. */
public abstract class BaseNotifier implements Notifier {

    protected abstract String channel();

    protected abstract String format(String message);

    @Override
    public final String send(String to, String message) {
        return "[" + channel() + "] to=" + to + " " + format(message);
    }
}

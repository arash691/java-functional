package shopflow.w07;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

/** EX-07-02: use try-with-resources so resources are always closed, in reverse order. */
public final class Resources {
    private Resources() {}

    public static void useResource(Supplier<TrackingResource> opener, Consumer<TrackingResource> action) {
        TrackingResource resource = opener.get();
        action.accept(resource);
    }

    public static void useTwo(
            Supplier<TrackingResource> first,
            Supplier<TrackingResource> second,
            BiConsumer<TrackingResource, TrackingResource> action) {
        TrackingResource a = first.get();
        TrackingResource b = second.get();
        action.accept(a, b);
    }
}

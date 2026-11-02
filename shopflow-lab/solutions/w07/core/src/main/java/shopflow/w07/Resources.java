package shopflow.w07;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

public final class Resources {
    private Resources() {}

    public static void useResource(Supplier<TrackingResource> opener, Consumer<TrackingResource> action) {
        try (TrackingResource resource = opener.get()) {
            action.accept(resource);
        }
    }

    public static void useTwo(
            Supplier<TrackingResource> first,
            Supplier<TrackingResource> second,
            BiConsumer<TrackingResource, TrackingResource> action) {
        try (TrackingResource a = first.get();
                TrackingResource b = second.get()) {
            action.accept(a, b);
        }
    }
}

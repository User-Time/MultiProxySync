package top.timeblog.multiproxysync.api;

import java.util.Objects;

public final class MultiProxySyncProvider {
    private static volatile MultiProxySyncAPI api;

    private MultiProxySyncProvider() {}

    public static MultiProxySyncAPI get() {
        return Objects.requireNonNull(api, "MultiProxySync API is not available yet.");
    }

    public static MultiProxySyncAPI getOrNull() {
        return api;
    }

    public static boolean isAvailable() {
        return api != null;
    }

    public static void register(MultiProxySyncAPI instance) {
        api = Objects.requireNonNull(instance, "instance");
    }

    public static void unregister() {
        api = null;
    }
}

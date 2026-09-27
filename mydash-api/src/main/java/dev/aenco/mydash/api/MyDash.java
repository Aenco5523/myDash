package dev.aenco.mydash.api;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class MyDash {
    private static final Object LOCK = new Object();
    private static final List<DashboardExtension> PENDING = new ArrayList<DashboardExtension>();

    private static volatile MyDashApi api;

    private MyDash() {
    }

    public static Optional<MyDashApi> api() {
        return Optional.ofNullable(api);
    }

    public static void register(DashboardExtension extension) {
        if (extension == null) throw new IllegalArgumentException("extension");

        synchronized (LOCK) {
            MyDashApi current = api;
            if (current == null) {
                PENDING.add(extension);
                return;
            }
            current.registerExtension(extension);
        }
    }

    public static void bind(MyDashApi instance) {
        if (instance == null) throw new IllegalArgumentException("instance");

        synchronized (LOCK) {
            if (api != null && api != instance) {
                throw new IllegalStateException("A myDash API instance is already bound");
            }

            api = instance;
            for (DashboardExtension extension : PENDING) {
                instance.registerExtension(extension);
            }
            PENDING.clear();
        }
    }

    public static void unbind(MyDashApi instance) {
        synchronized (LOCK) {
            if (api == instance) api = null;
        }
    }
}

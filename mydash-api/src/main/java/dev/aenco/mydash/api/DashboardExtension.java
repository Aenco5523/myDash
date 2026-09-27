package dev.aenco.mydash.api;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

public final class DashboardExtension {
    private final String id;
    private final String displayName;
    private final String route;
    private final Set<String> permissions;
    private final DashboardAssetProvider assetProvider;

    public DashboardExtension(String id, String displayName, String route, Set<String> permissions) {
        this(id, displayName, route, permissions, null);
    }

    public DashboardExtension(
        String id,
        String displayName,
        String route,
        Set<String> permissions,
        DashboardAssetProvider assetProvider
    ) {
        this.id = requireId(id);
        this.displayName = requireText(displayName, "displayName");
        this.route = requireRoute(route);
        this.permissions = Collections.unmodifiableSet(new LinkedHashSet<String>(permissions));
        this.assetProvider = assetProvider;
    }

    public String id() { return id; }
    public String displayName() { return displayName; }
    public String route() { return route; }
    public Set<String> permissions() { return permissions; }
    public DashboardAssetProvider assetProvider() { return assetProvider; }

    private static String requireId(String value) {
        String id = requireText(value, "id");
        if (!id.matches("[a-z0-9][a-z0-9._-]{0,63}")) {
            throw new IllegalArgumentException("id must match [a-z0-9][a-z0-9._-]{0,63}");
        }
        return id;
    }

    private static String requireRoute(String value) {
        String route = requireText(value, "route");
        if (!route.startsWith("/") || route.contains("..") || route.indexOf('\\') >= 0) {
            throw new IllegalArgumentException("route must be an absolute safe myDash path");
        }
        return route;
    }

    private static String requireText(String value, String name) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
        return value;
    }
}

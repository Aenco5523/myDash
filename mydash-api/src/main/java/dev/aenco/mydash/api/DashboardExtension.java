package dev.aenco.mydash.api;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

public final class DashboardExtension {
    private final String id;
    private final String displayName;
    private final String route;
    private final Set<String> permissions;

    public DashboardExtension(String id, String displayName, String route, Set<String> permissions) {
        this.id = requireText(id, "id");
        this.displayName = requireText(displayName, "displayName");
        this.route = requireText(route, "route");
        this.permissions = Collections.unmodifiableSet(new LinkedHashSet<String>(permissions));
    }

    public String id() { return id; }
    public String displayName() { return displayName; }
    public String route() { return route; }
    public Set<String> permissions() { return permissions; }

    private static String requireText(String value, String name) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
        return value;
    }
}

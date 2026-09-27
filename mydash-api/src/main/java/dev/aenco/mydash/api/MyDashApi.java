package dev.aenco.mydash.api;

import java.util.Collection;

public interface MyDashApi {
    String API_VERSION = "1";

    void registerExtension(DashboardExtension extension);

    Collection<DashboardExtension> extensions();
}

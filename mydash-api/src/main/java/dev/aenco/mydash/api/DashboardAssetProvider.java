package dev.aenco.mydash.api;

import java.io.IOException;

public interface DashboardAssetProvider {
    DashboardAsset open(String relativePath) throws IOException;
}

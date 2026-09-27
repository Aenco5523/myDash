# Compatibility roadmap

The first buildable targets are used to validate the shared architecture before expanding the matrix.

| Platform | Minecraft | Status |
|---|---:|---|
| NeoForge | 1.21.1 | Initial implementation |
| Fabric | 1.21.1 | Initial implementation |
| Paper | 1.21.1 | Initial implementation |
| Forge | 1.16.5 | Build verified |
| Fabric | 1.16.5 | Build verified |
| Paper | 1.16.5 | Build verified |
| Forge | 1.18.2 / 1.19.2 / 1.20.1 | Planned |
| Fabric | 1.18.2 / 1.19.2 / 1.20.1 | Planned |
| Paper | 1.18.2 / 1.19.2 / 1.20.1 | Planned |
| NeoForge | later 1.21.x and 26.x | Planned |
| Fabric | later 1.21.x and 26.x | Planned |
| Paper | later 1.21.x and 26.x | Planned |

Each implemented target gets its own adapter module and CI matrix entry. This avoids pretending an untested version is supported.

| Forge | 26.1 / 26.2 / 26.3 | Initial implementation; CI validation pending |
| NeoForge | 26.1 / 26.2 / 26.3 | Initial implementation; CI validation pending |
| Fabric | 26.1 / 26.2 / 26.3 | Initial implementation; CI validation pending |
| Paper | 26.1 / 26.2 / 26.3 | Initial implementation; CI validation pending |

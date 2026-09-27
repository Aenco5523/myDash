# Compatibility roadmap

The first buildable targets are used to validate the shared architecture before expanding the matrix.

| Platform | Minecraft | Status |
|---|---:|---|
| NeoForge | 1.21.1 | Initial implementation |
| Fabric | 1.21.1 | Initial implementation |
| Forge | 1.16.5 | Planned |
| Fabric | 1.16.5 | Planned |
| Paper | 1.16.5 | Planned |
| Forge | 1.18.2 / 1.19.2 / 1.20.1 | Planned |
| Fabric | 1.18.2 / 1.19.2 / 1.20.1 | Planned |
| Paper | 1.18.2 / 1.19.2 / 1.20.1 | Planned |
| NeoForge | later 1.21.x and 26.x | Planned |
| Fabric | later 1.21.x and 26.x | Planned |
| Paper | later 1.21.x and 26.x | Planned |

Each implemented target gets its own adapter module and CI matrix entry. This avoids pretending an untested version is supported.

# Compatibility

Only targets that have completed the self-hosted CI build are listed as supported.

| Platform | Minecraft versions | Status |
| --- | --- | --- |
| Forge | 1.16.5, 1.18.2, 1.20.1, 26.1, 26.2, 26.3 | Build verified |
| NeoForge | 1.21.1, 26.1, 26.2, 26.3 | Build verified |
| Fabric | 1.16.5, 1.18.2, 1.20.1, 1.21.1, 26.1, 26.2, 26.3 | Build verified |
| Paper | 1.16.5, 1.18.2, 1.20.1, 1.21.1, 26.1, 26.2, 26.3 | Build verified |

## Policy

A Minecraft/platform combination is not advertised as supported until its adapter compiles successfully in CI.

The shared `mydash-api` and `mydash-core` code is reused across targets, while Minecraft/loader-specific calls remain in thin adapters.

## Future ports

Additional versions can be added when there is a concrete compatibility target. They should follow the same artifact naming scheme and receive their own CI verification before being added to this table.

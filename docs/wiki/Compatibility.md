# Compatibility

The following targets are build-verified by the self-hosted CI matrix.

| Platform | Supported Minecraft versions |
| --- | --- |
| Forge | 1.16.5, 1.18.2, 1.20.1, 26.1, 26.2, 26.3 |
| NeoForge | 1.21.1, 26.1, 26.2, 26.3 |
| Fabric | 1.16.5, 1.18.2, 1.20.1, 1.21.1, 26.1, 26.2, 26.3 |
| Paper | 1.16.5, 1.18.2, 1.20.1, 1.21.1, 26.1, 26.2, 26.3 |

## Java build generations

The CI selects the Java/Gradle generation required by each Minecraft/platform family rather than forcing every target onto one toolchain. The 26.x family builds with Java 25.

## Compatibility policy

A version is documented as supported only after its adapter compiles successfully in CI. New targets should reuse the shared API/core and keep Minecraft-specific calls inside the platform adapter.

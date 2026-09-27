# Building myDash

## Self-hosted runner

myDash CI intentionally uses only GitHub self-hosted Windows runners.

```yaml
runs-on: [self-hosted, Windows, X64]
```

No GitHub-hosted runner is used for compilation or release builds.

## Runner requirements

The runner needs:

- Network access to Maven Central, Forge, NeoForge, Fabric, Paper and Mojang resources.
- Git installed.
- Enough disk space for Minecraft artifacts, mappings and transformed dependencies.
- A writable home directory so `~/.gradle` persists between jobs.

The workflow provisions the required Temurin Java version per target family instead of forcing every target onto one JDK.

## Java/build generations

The matrix currently spans Java 8 through Java 25 depending on Minecraft/platform generation. In particular, the 26.x family builds with Java 25.

Gradle versions are also selected per target because older Forge/Paper builds and current 26.x builds require different Gradle generations.

## Cache strategy

GitHub Actions cache storage is deliberately disabled. The self-hosted runner keeps its local `~/.gradle` cache between jobs.

Project settings enable Gradle build caching and parallel execution where supported, and build commands use `--build-cache`.

Do not routinely delete the runner's Gradle caches unless a dependency/mapping cache is known to be corrupt.

## Matrix behavior

Each supported platform/version target is an independent Actions job.

- One self-hosted runner instance: jobs queue and execute sequentially.
- Multiple available runner instances: independent matrix jobs can execute concurrently.

## 26.x layout

Forge, NeoForge, Fabric and Paper 26.x use parameterized projects under `calver/`. The Minecraft version is supplied through the `targetMinecraft` Gradle project property, so 26.1/26.2/26.3 do not require three duplicated source trees per platform.

## Artifact naming

```text
myDash-{platform}-{minecraftVersion}-v2.1.jar
```

Examples:

```text
myDash-forge-1.16.5-v2.1.jar
myDash-neoforge-1.21.1-v2.1.jar
myDash-fabric-26.3-v2.1.jar
myDash-paper-26.3-v2.1.jar
```

Normal branch builds verify compilation. Tag builds rebuild the supported targets and attach release JARs. A one-time `[bundle]` main-branch merge can also upload short-lived Actions artifacts for direct validation/download.

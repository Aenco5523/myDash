# Building myDash

## Self-hosted runner

myDash CI intentionally uses only GitHub self-hosted runners.

The workflow uses:

```yaml
runs-on: self-hosted
```

No GitHub-hosted runner is used for compilation or release builds.

### Runner requirements

The runner needs:

- Network access to Maven Central, NeoForged Maven, Fabric Maven and Mojang resources.
- Enough disk space for Minecraft artifacts and mappings.
- Git installed.
- A writable home directory so `~/.gradle` survives between jobs.
- Java does not need to be preinstalled permanently; the workflow provisions Temurin 21.

## Cache strategy

GitHub Actions cache storage is deliberately disabled because a persistent self-hosted runner already has a local cache and does not need to upload Gradle caches to GitHub.

The important persistent directories are managed under the runner user's `~/.gradle`, including dependency and transformed-artifact caches.

Project settings also enable:

```properties
org.gradle.parallel=true
org.gradle.caching=true
org.gradle.daemon=true
org.gradle.vfs.watch=true
```

Build commands use:

```text
--build-cache --parallel
```

The first NeoForge/Fabric build downloads Minecraft artifacts, mappings and loader dependencies. Later builds reuse the local Gradle cache.

Do not routinely delete `~/.gradle/caches` on the self-hosted runner.

## Parallel builds

The workflow uses a matrix, so NeoForge, Fabric and future Forge/Paper/version targets are independent jobs.

- One registered self-hosted runner: jobs queue and execute one at a time.
- Two or more available self-hosted runners: matrix jobs can build concurrently.
- Future version targets can be added to the same matrix without changing the common source layout.

For genuine simultaneous 1.16.5-to-current multi-version builds, register multiple self-hosted runner instances that accept the `self-hosted` label.

## Artifact naming

```text
myDash-{platform}-{minecraftVersion}-v{myDashVersion}.jar
```

Current examples:

```text
myDash-neoforge-1.21.1-v2.1.jar
myDash-fabric-1.21.1-v2.1.jar
```

Normal branch builds only verify compilation. Tag builds such as `v2.1` rebuild all supported targets and upload the JARs directly to the GitHub Release, avoiding temporary Actions artifact storage.

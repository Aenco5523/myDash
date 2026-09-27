# Build and Porting

## Shared layout

- `mydash-api`: loader-neutral public extension API
- `mydash-core`: embedded HTTP server, security, REST API, web assets, settings, file manager, console buffer
- `platforms/*`: modern integrated targets
- `legacy/*`: standalone older Minecraft build generations
- `calver/*`: parameterized 26.x adapters

## Self-hosted CI

All compilation runs on Windows self-hosted GitHub Actions runners. The runner keeps `~/.gradle` persistent so Minecraft artifacts, mappings, loader transforms, and Gradle build cache data are reused.

GitHub-hosted build runners are intentionally not used.

## Porting rule

Keep loader/version-specific Minecraft calls inside the adapter. Do not introduce Forge, NeoForge, Fabric, Paper, or Minecraft classes into `mydash-api` or `mydash-core`.

## Artifact naming

```text
myDash-{platform}-{minecraftVersion}-v2.1.jar
```

## 26.x

The 26.x adapters are parameterized by `targetMinecraft` instead of duplicating one source tree per point release. Java 25 is used for this family.

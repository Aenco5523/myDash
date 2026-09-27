# myDash

Self-hosted Minecraft server management dashboard with a shared API and multi-loader architecture.

## Current targets

- NeoForge 1.21.1
- Fabric 1.21.1
- Forge / Paper and additional Minecraft versions are planned on the same adapter architecture.

## Current features

- embedded web dashboard
- self-hosted-only CI
- persistent Gradle cache on the self-hosted runner
- server overview
- authenticated command execution
- live authenticated console streaming with reconnect/resume
- online player list
- player kick action
- player OP/DEOP, ban and whitelist actions
- web server settings
- curated `server.properties` management
- sandboxed file browser/editor with backups
- first-run administrator token
- administrator token rotation
- versioned REST API under `/api/v1`
- extension registry for other mods/plugins
- same-origin extension HTML/CSS/JS asset hosting
- automatic extension sidebar discovery

## Security defaults

myDash listens on:

```text
127.0.0.1:8765
```

by default.

On first launch, a high-entropy administrator token is printed once to the Minecraft server console. The plaintext token is not stored by myDash.

The configuration file is stored at:

```text
config/mydash.properties
```

Remote binding must be explicitly enabled. Internet-facing installations should use HTTPS through a trusted reverse proxy or tunnel.

## API

See:

- `docs/API.md`
- `docs/EXTENSIONS.md`
- `docs/ARCHITECTURE.md`
- `docs/BUILDING.md`
- `docs/COMPATIBILITY.md`

## Extension example

Other mods/plugins can register a panel independently of the Minecraft loader:

```java
MyDash.register(new DashboardExtension(
    "economy",
    "경제",
    "/extensions/economy/index.html",
    Collections.singleton("economy.view"),
    assetProvider
));
```

myDash discovers the extension and adds it to the sidebar automatically.

## Artifact naming

```text
myDash-{platform}-{minecraftVersion}-v{myDashVersion}.jar
```

Examples:

```text
myDash-neoforge-1.21.1-v2.1.jar
myDash-fabric-1.21.1-v2.1.jar
```

## Build runner

The GitHub Actions workflow intentionally uses:

```yaml
runs-on: self-hosted
```

No GitHub-hosted runner is used for project builds or release builds.

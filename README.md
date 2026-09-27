# myDash

> Self-hosted web dashboard and extension API for Minecraft servers.

myDash adds a local web control panel directly to a Minecraft server. Server operators can monitor the server, stream the live console, run commands, manage players, edit selected server settings and files, and expose additional dashboard panels from other mods or plugins through a loader-neutral API.

## What myDash adds

- A self-hosted browser dashboard
- Server overview and uptime/player status
- Authenticated live console streaming
- Remote command execution through the dashboard
- Player kick, ban, OP/DEOP, and whitelist controls
- Curated `server.properties` editing
- Sandboxed server file browser and UTF-8 text editor
- Automatic backups before file edits
- First-run administrator token and token rotation
- Versioned REST API under `/api/v1`
- Extension API for other mods/plugins to add dashboard pages
- Forge, NeoForge, Fabric, and Paper support across multiple Minecraft generations

## Why use myDash?

myDash is intended for server owners who want a lightweight management panel without running a separate full server-management stack.

The dashboard is bundled inside the myDash JAR and served by the Minecraft server process. The core and API are loader-neutral, while thin platform adapters handle the differences between Forge, NeoForge, Fabric, and Paper.

## Before you download

myDash is **server-side software**. Players do not need to install myDash on their clients.

Download the JAR that matches both your server platform and Minecraft version. The filename always follows:

```text
myDash-{platform}-{minecraftVersion}-v{myDashVersion}.jar
```

Examples:

```text
myDash-forge-1.20.1-v2.1.jar
myDash-neoforge-1.21.1-v2.1.jar
myDash-fabric-26.3-v2.1.jar
myDash-paper-26.3-v2.1.jar
```

## Installation

### Forge / NeoForge / Fabric

1. Download the matching myDash JAR.
2. Put it in the server's `mods` folder.
3. Start the server.
4. Read the first-run administrator token printed to the server console.
5. Open the dashboard from the machine running the server at:

```text
http://127.0.0.1:8765
```

### Paper

1. Download the matching Paper JAR.
2. Put it in the server's `plugins` folder.
3. Start the server.
4. Read the first-run administrator token printed to the server console.
5. Open:

```text
http://127.0.0.1:8765
```

## Using myDash

The built-in dashboard currently includes:

- **Overview** — server state, player count, uptime, and platform information
- **Live Console** — authenticated console stream with reconnect/resume and command execution
- **Player Management** — kick, ban, OP/DEOP, and whitelist actions
- **Server Environment** — curated server settings plus the safe file manager
- **myDash Settings** — web listener settings and administrator token rotation
- **Extensions** — dashboard pages registered by compatible mods/plugins

## Security

myDash binds to:

```text
127.0.0.1:8765
```

by default.

Remote binding is disabled unless explicitly enabled. The first administrator token is generated with high entropy, printed once to the Minecraft console, and only a salted hash is stored by myDash.

For access over the internet, place myDash behind HTTPS using a trusted reverse proxy, VPN, or tunnel. Do not expose the embedded HTTP listener directly to the public internet without an appropriate secure access layer.

Configuration is stored in:

```text
config/mydash.properties
```

## Compatibility

Build-verified targets:

| Platform | Minecraft versions |
| --- | --- |
| Forge | 1.16.5, 1.18.2, 1.20.1, 26.1, 26.2, 26.3 |
| NeoForge | 1.21.1, 26.1, 26.2, 26.3 |
| Fabric | 1.16.5, 1.18.2, 1.20.1, 1.21.1, 26.1, 26.2, 26.3 |
| Paper | 1.16.5, 1.18.2, 1.20.1, 1.21.1, 26.1, 26.2, 26.3 |

Use the platform and Minecraft version encoded in the JAR filename.

## Extension API

Other mods/plugins can register their own myDash sidebar pages without depending on platform-specific Minecraft classes.

```java
MyDash.register(new DashboardExtension(
    "economy",
    "Economy",
    "/extensions/economy/index.html",
    Collections.singleton("economy.view"),
    assetProvider
));
```

myDash exposes registered extensions through `GET /api/v1/extensions` and serves their static assets from the same origin as the built-in dashboard.

## Developer documentation

User-facing information stays in this README. Architecture, API, security, extension, build, and porting details live in the project documentation and Wiki source:

- [Wiki home](docs/wiki/Home.md)
- [API reference](docs/API.md)
- [Extension API](docs/EXTENSIONS.md)
- [Architecture](docs/ARCHITECTURE.md)
- [Build and CI](docs/BUILDING.md)
- [Compatibility](docs/COMPATIBILITY.md)

The repository Wiki is enabled at `https://github.com/Aenco5523/myDash/wiki`. The Markdown under `docs/wiki/` is kept as the source copy for those pages.

## Builds and CI

myDash builds only on self-hosted GitHub Actions runners. Persistent local Gradle caches are reused between jobs.

Supported artifacts use:

```text
myDash-{platform}-{minecraftVersion}-v2.1.jar
```

## License

myDash is distributed under a custom **All Rights Reserved** license. Official unmodified releases may be downloaded and used for normal Minecraft gameplay and server operation. Modification, redistribution, repackaging, or commercial use is not permitted unless the license or copyright holder explicitly allows it.

See [LICENSE](LICENSE) for the full terms.

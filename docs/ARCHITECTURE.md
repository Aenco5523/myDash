# myDash architecture

myDash is intentionally split into loader-neutral code and thin platform adapters.

```text
Browser
  │
  ├─ Web UI
  └─ /api/v1/*
        │
        ▼
   mydash-core
        │
        ▼
   ServerBridge
   ├─ Forge adapter
   ├─ NeoForge adapter
   ├─ Fabric adapter
   └─ Paper adapter
```

## Project layout

- `mydash-api` — loader-neutral public extension API
- `mydash-core` — HTTP server, security, REST API, settings, live console, file manager and bundled web UI
- `platforms/*` — modern integrated targets
- `legacy/*` — standalone older Minecraft build generations
- `calver/*` — parameterized 26.x adapters shared across 26.1, 26.2 and 26.3

## Rules

1. Loader/version-specific Minecraft calls stay inside platform adapters.
2. `mydash-api` stays independent from Minecraft and loader classes.
3. `mydash-core` stays independent from loader classes.
4. Server mutations are scheduled through the platform bridge onto the Minecraft server thread.
5. API paths are versioned under `/api/v1`.
6. Web assets are bundled in the JAR and served by the embedded HTTP server.
7. The default listener is loopback-only and authenticated operations require the administrator token.

## Artifact naming

```text
myDash-{platform}-{minecraftVersion}-v{myDashVersion}.jar
```

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
   ├─ NeoForge adapter
   ├─ Fabric adapter
   ├─ Forge adapter   (planned)
   └─ Paper adapter   (planned)
```

## Rules

1. Loader-specific Minecraft calls stay inside `platforms/*`.
2. `mydash-api` must stay independent from Minecraft classes.
3. `mydash-core` must stay independent from loader classes.
4. API paths are versioned under `/api/v1`.
5. Web assets are bundled in the JAR and served by the embedded HTTP server.
6. The initial HTTP listener is loopback-only until authentication is implemented.

## File naming

`myDash-{platform}-{minecraftVersion}-v{myDashVersion}.jar`

Examples:

- `myDash-neoforge-1.21.1-v2.1.jar`
- `myDash-fabric-1.21.1-v2.1.jar`

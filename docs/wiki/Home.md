# myDash Wiki

myDash is a self-hosted web dashboard and extension API for Minecraft servers.

This Wiki covers the technical details that are intentionally kept out of the main README.

## Start here

- [Installation](Installation.md)
- [Compatibility](Compatibility.md)
- [Security](Security.md)
- [API](API.md)
- [Extensions](Extensions.md)
- [Build and Porting](Build-and-Porting.md)

## Architecture at a glance

```text
Browser
  |
  +-- bundled myDash web UI
  +-- /api/v1/*
          |
          v
      mydash-core
          |
          v
      ServerBridge
       /   |   |   \
  Forge NeoForge Fabric Paper
```

`mydash-api` and `mydash-core` are designed to stay independent from loader-specific Minecraft classes. Platform adapters schedule server mutations on the Minecraft server thread and translate platform APIs into the common `ServerBridge`.

## Dashboard

The bundled dashboard exposes server overview, live console, player management, server settings, a safe file editor, myDash settings, and extension pages.

The default listener is `127.0.0.1:8765`.

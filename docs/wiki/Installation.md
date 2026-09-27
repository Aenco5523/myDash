# Installation

## Choose the correct JAR

Artifact names follow:

```text
myDash-{platform}-{minecraftVersion}-v{myDashVersion}.jar
```

Use a JAR matching both the Minecraft version and server platform.

## Forge / NeoForge / Fabric

Place the JAR in the server `mods` directory and start the server.

## Paper

Place the JAR in the server `plugins` directory and start the server.

## First launch

On the first successful start, myDash creates `config/mydash.properties` and prints the administrator token once to the Minecraft server console.

Open:

```text
http://127.0.0.1:8765
```

Enter the token when prompted.

## Remote access

The embedded server is intentionally loopback-only by default. For remote administration, explicitly enable remote binding and put myDash behind a trusted HTTPS reverse proxy, VPN, or secure tunnel.

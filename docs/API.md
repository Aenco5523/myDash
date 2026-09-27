# myDash API v1

Base path: `/api/v1`

All endpoints except `GET /api/v1/health` require the administrator Bearer token.

## Authentication

Send:

```http
Authorization: Bearer mydash_xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
```

The first administrator token is generated on first launch and printed once to the Minecraft server console. The plaintext token is not stored by myDash.

The configuration file is:

```text
config/mydash.properties
```

It contains the token salt/hash and web server settings.

## Endpoints

### GET /api/v1/health

Public liveness endpoint.

Example response:

```json
{
  "ok": true,
  "apiVersion": "1",
  "authentication": "bearer"
}
```

### GET /api/v1/server

Returns the current server snapshot.

Example:

```json
{
  "platform": "NeoForge",
  "minecraftVersion": "1.21.1",
  "onlinePlayers": 3,
  "maxPlayers": 20,
  "uptimeMillis": 918221
}
```

### POST /api/v1/console

Executes one Minecraft server command.

The preferred request body is JSON:

```json
{
  "command": "say Hello from myDash"
}
```

Plain-text request bodies are also accepted.

Commands are queued onto the Minecraft server thread by the platform adapter. Leading `/` characters are removed before dispatch. Request bodies are limited to 16 KiB and commands are limited to 2048 characters.

## Network defaults

myDash listens on:

```text
127.0.0.1:8765
```

by default.

Remote binding requires explicitly setting:

```properties
server.bind=0.0.0.0
server.allowRemote=true
```

For internet-facing deployments, put myDash behind HTTPS/reverse-proxy access controls rather than exposing the embedded HTTP server directly.


### GET /api/v1/players

Returns the players currently connected to the Minecraft server.

Example:

```json
[
  {
    "uuid": "00000000-0000-0000-0000-000000000000",
    "name": "ExamplePlayer",
    "operator": false
  }
]
```

The platform adapter gathers the list on the Minecraft server thread.

### POST /api/v1/players/{uuid}/kick

Disconnects one currently connected player.

Request:

```json
{
  "reason": "Kicked by myDash"
}
```

The reason is optional and limited to 256 characters. The kick is executed on the Minecraft server thread.

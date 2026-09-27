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


### GET /api/v1/settings

Returns the current embedded web server configuration without authentication secrets.

Example:

```json
{
  "bindAddress": "127.0.0.1",
  "port": 8765,
  "allowRemote": false
}
```

### POST /api/v1/settings

Stores new embedded web server settings.

Request:

```json
{
  "bindAddress": "127.0.0.1",
  "port": 8765,
  "allowRemote": false
}
```

Changing these settings requires a Minecraft server restart before the listener changes.

A non-loopback bind address requires `allowRemote=true`.

### POST /api/v1/auth/rotate

Rotates the administrator token.

The request must be authenticated with the current administrator token. The old token becomes invalid immediately.

Example response:

```json
{
  "rotated": true,
  "token": "mydash_xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx"
}
```

The returned plaintext token is not written to disk by myDash. Store it securely when rotating.


### GET /api/v1/server-properties

Returns the curated `server.properties` settings currently stored on disk.

Fields:

- `motd`
- `serverPort`
- `maxPlayers`
- `onlineMode`
- `whiteList`
- `difficulty`
- `gamemode`
- `hardcore`

### POST /api/v1/server-properties

Updates only the curated settings above and preserves the other property values.

Example:

```json
{
  "motd": "My Minecraft Server",
  "serverPort": 25565,
  "maxPlayers": 20,
  "onlineMode": true,
  "whiteList": false,
  "difficulty": "normal",
  "gamemode": "survival",
  "hardcore": false
}
```

The write uses a temporary file followed by an atomic replacement when the filesystem supports it. A symlinked `server.properties` file is rejected.

A server restart is required for these settings to be applied consistently.


### GET /api/v1/console/stream

Authenticated Server-Sent Events stream for the live Minecraft console.

The browser sends the administrator Bearer token and may resume from the last processed event id:

```text
GET /api/v1/console/stream?after=123
Authorization: Bearer mydash_...
Accept: text/event-stream
```

Each console record is sent as an SSE `line` event:

```text
id: 124
event: line
data: {"id":124,"timestamp":...,"level":"INFO","logger":"...","message":"..."}
```

myDash keeps the most recent 750 log records in memory and sends up to 200 recent records when a new stream connects without an `after` value. Up to four concurrent console streams are allowed.

### Player management actions

The following authenticated actions are available for currently connected players:

```text
POST /api/v1/players/{uuid}/kick
POST /api/v1/players/{uuid}/ban
POST /api/v1/players/{uuid}/op
POST /api/v1/players/{uuid}/deop
POST /api/v1/players/{uuid}/whitelist-add
POST /api/v1/players/{uuid}/whitelist-remove
```

Kick and ban accept an optional JSON reason:

```json
{
  "reason": "Managed by myDash"
}
```

Player names are validated against the normal Minecraft username character set before command-based management actions are composed.

### GET /api/v1/files

Lists entries inside the myDash file-manager sandbox.

Allowed roots are:

```text
config/
mods/
world/serverconfig/
server.properties
ops.json
whitelist.json
banned-players.json
banned-ips.json
```

Example:

```text
GET /api/v1/files?path=config
```

The response reports whether an entry is a directory, symlink, editable text file, or read-only file.

### GET /api/v1/files/content

Reads one editable UTF-8 text file from the allowed sandbox.

```text
GET /api/v1/files/content?path=config/example.toml
```

### PUT /api/v1/files/content

Writes one editable UTF-8 text file.

```text
PUT /api/v1/files/content?path=config/example.toml
Content-Type: text/plain; charset=utf-8
```

File-manager protections include:

- path traversal rejection
- exact allowed-root enforcement
- symlink traversal rejection
- binary/NUL-byte rejection
- UTF-8 validation
- 1 MiB edit limit
- no JAR/binary editing
- automatic backup of existing files under `.mydash-backups/` before replacement
- atomic file replacement where supported by the filesystem

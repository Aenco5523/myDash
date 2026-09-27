# Security

## Network defaults

myDash listens on `127.0.0.1:8765` by default and rejects non-loopback binding unless remote access is explicitly enabled.

For public-network access, use HTTPS through a trusted reverse proxy, VPN, or tunnel.

## Administrator token

- Generated on first run using a cryptographically secure random source.
- Printed in plaintext once to the Minecraft server console.
- Plaintext is not persisted by myDash.
- Only a salt and SHA-256-derived hash are stored.
- The token can be rotated from the authenticated API/dashboard.
- The browser keeps the active token in `sessionStorage`, not persistent local storage.

## HTTP protections

myDash applies same-origin behavior and security headers, caps request bodies, validates commands, and limits concurrent live-console streams.

## File manager sandbox

Editable paths are limited to approved roots such as `config/`, `mods/`, `world/serverconfig/`, and selected root configuration JSON/properties files.

Protections include traversal rejection, symlink rejection, binary/NUL rejection, UTF-8 validation, an edit-size limit, backup-before-write, and atomic replacement where supported.

JAR and other binary files may be listed but are not editable through the text editor.

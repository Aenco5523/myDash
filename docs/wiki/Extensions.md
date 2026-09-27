# Extensions

myDash exposes a loader-neutral Java registration API so another mod/plugin can add its own dashboard entry.

```java
MyDash.register(new DashboardExtension(
    "economy",
    "Economy",
    "/extensions/economy/index.html",
    Collections.singleton("economy.view"),
    assetProvider
));
```

## Rules

- Extension IDs must match `[a-z0-9][a-z0-9._-]{0,63}`.
- Routes must be absolute myDash paths.
- Traversal segments and backslashes are rejected.
- A single extension asset is limited to 8 MiB.
- Assets are served from the same origin as the built-in dashboard.
- Sensitive server data should be requested through authenticated `/api/v1/*` endpoints.

See [EXTENSIONS.md](../EXTENSIONS.md) for the detailed Java example and authentication notes.

# myDash Extension API

myDash exposes a loader-neutral Java API so other mods/plugins can add dashboard entries without depending on NeoForge, Fabric or Paper classes.

## Registering an extension

Extensions can register before or after the Minecraft server starts.

```java
import dev.aenco.mydash.api.DashboardAsset;
import dev.aenco.mydash.api.DashboardAssetProvider;
import dev.aenco.mydash.api.DashboardExtension;
import dev.aenco.mydash.api.MyDash;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Collections;

MyDash.register(new DashboardExtension(
    "economy",
    "경제",
    "/extensions/economy/index.html",
    Collections.singleton("economy.view"),
    relativePath -> {
        String resource = "/mydash-panel/" + relativePath;

        try (InputStream input = YourMod.class.getResourceAsStream(resource)) {
            if (input == null) return null;

            ByteArrayOutputStream output = new ByteArrayOutputStream();
            byte[] buffer = new byte[8192];
            int read;
            while ((read = input.read(buffer)) != -1) {
                output.write(buffer, 0, read);
            }

            return new DashboardAsset(contentType(relativePath), output.toByteArray());
        }
    }
));
```

A small content-type helper can map file extensions such as:

```text
.html -> text/html; charset=utf-8
.css  -> text/css; charset=utf-8
.js   -> text/javascript; charset=utf-8
.svg  -> image/svg+xml
.png  -> image/png
```

## How it appears

After registration, myDash exposes the extension metadata through:

```text
GET /api/v1/extensions
```

The built-in web application automatically adds the extension to the sidebar.

When the user opens it, the route is rendered inside the myDash workspace.

For the example above:

```text
/extensions/economy/index.html
```

is loaded from the asset provider registered by the economy mod.

## Asset rules

Extension IDs are restricted to:

```text
[a-z0-9][a-z0-9._-]{0,63}
```

Routes must be absolute myDash paths and may not contain `..` or backslashes.

Asset requests may not contain traversal segments, and a single returned asset is limited to 8 MiB by myDash.

Static extension files are served from the same origin as myDash. Sensitive server data should still be fetched through authenticated `/api/v1/*` endpoints rather than embedded directly into static assets.

## Authentication from an extension panel

The built-in myDash UI keeps the administrator token in `sessionStorage` under:

```text
mydash.adminToken
```

Because extension panels are served from the same origin, their JavaScript can authenticate API requests with the current session token:

```js
const token = sessionStorage.getItem("mydash.adminToken");

const response = await fetch("/api/v1/server", {
  headers: {
    Authorization: "Bearer " + token
  }
});
```

Do not persist the token to localStorage, cookies, logs or extension configuration.

## Current API scope

The initial extension API supports:

- registration before or after myDash startup
- sidebar discovery metadata
- permissions metadata
- same-origin HTML/CSS/JS/image asset hosting
- loader-neutral registration shared by NeoForge/Fabric and future platforms

Future work can add scoped extension REST handlers and richer native myDash UI components without changing the basic registration mechanism.

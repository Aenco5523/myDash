# API

The stable base path is:

```text
/api/v1
```

All endpoints except `GET /api/v1/health` require:

```http
Authorization: Bearer mydash_...
```

## Main endpoint groups

- `GET /api/v1/health`
- `GET /api/v1/server`
- `GET /api/v1/console/stream`
- `POST /api/v1/console`
- `GET /api/v1/players`
- player kick/ban/OP/DEOP/whitelist actions
- `GET/POST /api/v1/settings`
- `GET/POST /api/v1/server-properties`
- `POST /api/v1/auth/rotate`
- `GET /api/v1/extensions`
- safe file listing/read/write endpoints under `/api/v1/files`

See the repository [API reference](../API.md) for request and response examples.

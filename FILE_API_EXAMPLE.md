# Separate file upload/download REST API example

This example lives alongside the existing product image upload code. It stores file bytes in `uploads/library/` (configurable) and file metadata in the `stored_files` database table. It does not expose the storage key as a public path.

## Endpoints

| Method | Endpoint | Purpose |
| --- | --- | --- |
| `POST` | `/api/files` | Upload one file using field `file`; optionally include `entityType` and `entityId` |
| `POST` | `/api/files/batch` | Upload multiple `files` together for required `entityType` and `entityId` |
| `GET` | `/api/files` | List all file metadata |
| `GET` | `/api/files/by-entity?entityType=Product&entityId=42` | List files attached to any entity record |
| `GET` | `/api/files/{id}` | Read one file's metadata |
| `GET` | `/api/files/{id}/download` | Download bytes with the original filename |
| `GET` | `/api/files/{id}/view` | Return content inline for browser preview where supported |
| `PUT` | `/api/files/{id}` | Edit display filename and entity association |
| `DELETE` | `/api/files/{id}` | Delete metadata and stored bytes |

Example upload:

```bash
curl -F "file=@./report.pdf" http://localhost:8080/api/files
```

Example multi-file upload attached to product 42:

```bash
curl -F "entityType=Product" -F "entityId=42" \
  -F "files=@./front.jpg" -F "files=@./manual.pdf" \
  http://localhost:8080/api/files/batch
```

`entityType` is a generic label (`Product`, `Category`, or a future entity name) and `entityId` is that record's ID. This keeps the file module reusable without changing each entity's schema. The API does not enforce that the tagged entity exists; callers should supply a valid type/ID pair. The Angular page at `/files` provides multi-select upload, filtering, view, download, edit, and delete controls.

The default storage directory is `uploads/library`. Override it with the Spring property `app.file-storage.directory` (for example, `APP_FILE_STORAGE_DIRECTORY=/srv/ims/files`). Upload size limits use the existing `spring.servlet.multipart.max-file-size` and `max-request-size` settings.

The Angular example is available at `/files`. The API is open, as requested; no authentication or authorization is added.

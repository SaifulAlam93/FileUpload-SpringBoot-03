# Submit one entity and multiple images together

The independent UploadExample demonstrates an entity payload and image files sent in one multipart request. Its JSON has title, description, referenceCode, quantity, and unitPrice. The backend saves the entity row, stores every image under a generated file name, and records image metadata in stored_files tagged with the new UploadExample ID.

## API

POST /api/upload-examples consumes multipart/form-data:

- data: a JSON part containing the complete entity fields, deserialized into UploadExampleRequestDto.
- images: one or more image file parts, repeated with this field name.

Example:

    curl -X POST http://localhost:8080/api/upload-examples \
      -F 'data={"title":"Desk lamp","description":"Adjustable metal lamp","referenceCode":"LAMP-001","quantity":12,"unitPrice":39.95};type=application/json' \
      -F 'images=@./lamp-front.jpg' \
      -F 'images=@./lamp-side.jpg'

The response includes the saved entity and image metadata, inline-view URLs, and download URLs. GET /api/upload-examples lists saved records; GET /api/upload-examples/{id} returns one record with its images.

The Angular form is at /upload-example. It builds a FormData request with a JSON data DTO and repeated images parts. Use the same pattern for another entity by replacing the example fields and UploadExample owner label with that entity's type and saved ID. The entity label is a generic association and is not a database foreign key. Requests remain subject to the existing multipart size limits.

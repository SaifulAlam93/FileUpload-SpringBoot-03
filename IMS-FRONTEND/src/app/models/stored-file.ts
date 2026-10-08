export interface StoredFile {
  id: number;
  originalFilename: string;
  contentType: string;
  size: number;
  uploadedAt: string;
  entityType: string | null;
  entityId: number | null;
  downloadUrl: string;
  viewUrl: string;
}

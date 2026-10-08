import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { StoredFile } from '../models/stored-file';

@Injectable({ providedIn: 'root' })
export class FileLibraryService {
  private readonly apiUrl = `http://${window.location.hostname}:8080/api/files`;

  constructor(private http: HttpClient) {}

  list(): Observable<StoredFile[]> {
    return this.http.get<StoredFile[]>(this.apiUrl);
  }

  upload(file: File): Observable<StoredFile> {
    const body = new FormData();
    body.append('file', file, file.name);
    return this.http.post<StoredFile>(this.apiUrl, body);
  }

  uploadMany(files: File[], entityType: string, entityId: number): Observable<StoredFile[]> {
    const body = new FormData();
    files.forEach(file => body.append('files', file, file.name));
    body.append('entityType', entityType);
    body.append('entityId', String(entityId));
    return this.http.post<StoredFile[]>(`${this.apiUrl}/batch`, body);
  }

  listForEntity(entityType: string, entityId: number): Observable<StoredFile[]> {
    return this.http.get<StoredFile[]>(`${this.apiUrl}/by-entity`, { params: { entityType, entityId } });
  }

  download(id: number): Observable<Blob> {
    return this.http.get(`${this.apiUrl}/${id}/download`, { responseType: 'blob' });
  }

  update(id: number, originalFilename: string, entityType: string | null, entityId: number | null): Observable<StoredFile> {
    return this.http.put<StoredFile>(`${this.apiUrl}/${id}`, { originalFilename, entityType, entityId });
  }

  viewUrl(id: number): string {
    return `${this.apiUrl}/${id}/view`;
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}

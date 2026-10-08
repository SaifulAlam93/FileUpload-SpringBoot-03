import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { UploadExample, UploadExamplePayload } from '../models/upload-example';

@Injectable({ providedIn: 'root' })
export class UploadExampleService {
  private readonly apiUrl = 'http://' + window.location.hostname + ':8080/api/upload-examples';

  constructor(private http: HttpClient) {}

  create(entityData: UploadExamplePayload, images: File[]): Observable<UploadExample> {
    const body = new FormData();
    body.append('data', new Blob([JSON.stringify(entityData)], { type: 'application/json' }));
    images.forEach(image => body.append('images', image, image.name));
    return this.http.post<UploadExample>(this.apiUrl, body);
  }

  list(): Observable<UploadExample[]> {
    return this.http.get<UploadExample[]>(this.apiUrl);
  }

  fileUrl(path: string): string {
    return 'http://' + window.location.hostname + ':8080' + path;
  }
}

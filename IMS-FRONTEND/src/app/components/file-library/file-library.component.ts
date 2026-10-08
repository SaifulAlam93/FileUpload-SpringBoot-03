import { Component, ElementRef, OnInit, ViewChild } from '@angular/core';
import { finalize } from 'rxjs';
import { FileLibraryService } from '../../services/file-library.service';
import { StoredFile } from '../../models/stored-file';

@Component({
  selector: 'app-file-library',
  templateUrl: './file-library.component.html',
  styleUrls: ['./file-library.component.css']
})
export class FileLibraryComponent implements OnInit {
  @ViewChild('fileInput') fileInput?: ElementRef<HTMLInputElement>;
  files: StoredFile[] = [];
  selectedFiles: File[] = [];
  entityType = '';
  entityId: number | null = null;
  busy = false;
  error = '';
  notice = '';

  constructor(private fileLibrary: FileLibraryService) {}

  ngOnInit(): void {
    this.refresh();
  }

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    this.selectedFiles = input.files ? Array.from(input.files) : [];
    this.error = '';
    this.notice = '';
  }

  upload(): void {
    const entityId = Number(this.entityId);
    if (this.selectedFiles.length === 0 || !this.entityType.trim() || !Number.isInteger(entityId) || entityId < 1 || this.busy) return;
    this.busy = true;
    this.error = '';
    this.notice = '';
    this.fileLibrary.uploadMany(this.selectedFiles, this.entityType.trim(), entityId).pipe(finalize(() => this.busy = false)).subscribe({
      next: saved => {
        this.notice = `${saved.length} file${saved.length === 1 ? '' : 's'} uploaded.`;
        this.selectedFiles = [];
        if (this.fileInput) this.fileInput.nativeElement.value = '';
        this.refresh();
      },
      error: err => this.error = this.errorMessage(err)
    });
  }

  download(file: StoredFile): void {
    this.fileLibrary.download(file.id).subscribe({
      next: blob => {
        const url = URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = url;
        link.download = file.originalFilename;
        link.click();
        window.setTimeout(() => URL.revokeObjectURL(url), 1000);
      },
      error: err => this.error = this.errorMessage(err)
    });
  }

  viewUrl(file: StoredFile): string {
    return this.fileLibrary.viewUrl(file.id);
  }

  edit(file: StoredFile): void {
    const originalFilename = window.prompt('File display name', file.originalFilename);
    if (originalFilename === null || !originalFilename.trim()) return;
    const entityType = window.prompt('Entity type (for example Product or Category)', file.entityType ?? '');
    if (entityType === null) return;
    const entityIdText = window.prompt('Entity ID (leave blank to remove the entity tag)', file.entityId?.toString() ?? '');
    if (entityIdText === null) return;
    const entityId = entityIdText.trim() ? Number(entityIdText) : null;
    if (entityId !== null && (!Number.isInteger(entityId) || entityId < 1)) {
      this.error = 'Entity ID must be a positive whole number.';
      return;
    }
    this.fileLibrary.update(file.id, originalFilename.trim(), entityType.trim() || null, entityId).subscribe({
      next: () => { this.notice = 'File details updated.'; this.refresh(); },
      error: err => this.error = this.errorMessage(err)
    });
  }

  remove(file: StoredFile): void {
    this.fileLibrary.delete(file.id).subscribe({
      next: () => {
        this.notice = `${file.originalFilename} deleted.`;
        this.refresh();
      },
      error: err => this.error = this.errorMessage(err)
    });
  }

  formatSize(bytes: number): string {
    if (bytes < 1024) return `${bytes} B`;
    if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`;
    return `${(bytes / (1024 * 1024)).toFixed(1)} MB`;
  }

  refresh(): void {
    const entityType = this.entityType.trim();
    const entityId = Number(this.entityId);
    const request = entityType && Number.isInteger(entityId) && entityId > 0
      ? this.fileLibrary.listForEntity(entityType, entityId)
      : this.fileLibrary.list();
    request.subscribe({
      next: files => this.files = files,
      error: err => this.error = this.errorMessage(err)
    });
  }

  private errorMessage(error: any): string {
    return typeof error?.error === 'string' ? error.error : 'The file request failed. Check that the backend is running.';
  }
}

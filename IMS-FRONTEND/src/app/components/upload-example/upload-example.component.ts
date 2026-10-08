import { Component, ElementRef, OnInit, ViewChild } from '@angular/core';
import { finalize } from 'rxjs';
import { UploadExample, UploadExamplePayload } from '../../models/upload-example';
import { UploadExampleService } from '../../services/upload-example.service';

@Component({
  selector: 'app-upload-example',
  templateUrl: './upload-example.component.html',
  styleUrls: ['./upload-example.component.css']
})
export class UploadExampleComponent implements OnInit {
  @ViewChild('imagesInput') imagesInput?: ElementRef<HTMLInputElement>;
  records: UploadExample[] = [];
  title = '';
  description = '';
  referenceCode = '';
  quantity: number | null = null;
  unitPrice: number | null = null;
  images: File[] = [];
  busy = false;
  error = '';
  notice = '';

  constructor(private uploadExample: UploadExampleService) {}

  ngOnInit(): void {
    this.refresh();
  }

  onImagesSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    this.images = input.files ? Array.from(input.files) : [];
    this.error = '';
  }

  submit(): void {
    if (this.busy || !this.images.length || this.quantity === null || this.unitPrice === null) return;
    const entityData: UploadExamplePayload = {
      title: this.title.trim(),
      description: this.description.trim(),
      referenceCode: this.referenceCode.trim(),
      quantity: Number(this.quantity),
      unitPrice: Number(this.unitPrice)
    };
    this.busy = true;
    this.error = '';
    this.notice = '';
    this.uploadExample.create(entityData, this.images).pipe(finalize(() => this.busy = false)).subscribe({
      next: saved => {
        this.notice = 'Record #' + saved.id + ' and ' + saved.images.length + ' image(s) saved together.';
        this.resetForm();
        if (this.imagesInput) this.imagesInput.nativeElement.value = '';
        this.refresh();
      },
      error: error => this.error = this.errorMessage(error)
    });
  }

  refresh(): void {
    this.uploadExample.list().subscribe({
      next: records => this.records = records,
      error: error => this.error = this.errorMessage(error)
    });
  }

  fileUrl(path: string): string {
    return this.uploadExample.fileUrl(path);
  }

  private resetForm(): void {
    this.title = '';
    this.description = '';
    this.referenceCode = '';
    this.quantity = null;
    this.unitPrice = null;
    this.images = [];
  }

  private errorMessage(error: any): string {
    if (typeof error?.error === 'string') return error.error;
    if (error?.status === 0) return 'Cannot reach the backend. Check that Spring Boot is running.';
    return 'The record and images could not be saved. Check the form and backend response.';
  }
}

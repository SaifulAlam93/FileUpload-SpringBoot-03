export interface UploadExampleImage {
  id: number;
  originalFilename: string;
  contentType: string;
  size: number;
  viewUrl: string;
  downloadUrl: string;
}

export interface UploadExample {
  id: number;
  title: string;
  description: string;
  referenceCode: string;
  quantity: number;
  unitPrice: number;
  createdAt: string;
  images: UploadExampleImage[];
}

export interface UploadExamplePayload {
  title: string;
  description: string;
  referenceCode: string;
  quantity: number;
  unitPrice: number;
}

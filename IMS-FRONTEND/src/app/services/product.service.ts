import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, catchError, throwError } from 'rxjs';
import { Product } from '../models/product';

@Injectable({
  providedIn: 'root'
})
export class ProductService {
  private apiUrl = 'http://localhost:8080/api/products';

  constructor(private http: HttpClient) {}

  // ✅ Get all products
  getProducts(): Observable<Product[]> {
    return this.http.get<Product[]>(this.apiUrl).pipe(
      catchError(this.handleError)
    );
  }

  // ✅ Get single product by ID
  getProduct(id: number): Observable<Product> {
    return this.http.get<Product>(`${this.apiUrl}/${id}`).pipe(
      catchError(this.handleError)
    );
  }

  // ✅ Create product with single or multiple images
  createProduct(product: Product, imageFiles?: File[]): Observable<Product> {
    const formData = new FormData();

    // Include product data as JSON
    formData.append('product', new Blob([JSON.stringify(product)], { type: 'application/json' }));

    // Append all selected files (if any)
    if (imageFiles && imageFiles.length > 0) {
      imageFiles.forEach((file, index) => {
        formData.append('images', file); // matches backend @RequestParam("images")
      });
    }

    return this.http.post<Product>(this.apiUrl, formData).pipe(
      catchError(this.handleError)
    );
  }

  // ✅ Alternate method using individual fields (optional)
  createProduct2(product: Product, imageFiles?: File[]): Observable<Product> {
    const formData = new FormData();

    // Append fields individually
    formData.append('name', product.name);
    formData.append('description', product.description || '');
    formData.append('price', product.price.toString());
    formData.append('quantity', product.quantity.toString());
    formData.append('categoryId', product.category?.id?.toString() || '');

    if (imageFiles && imageFiles.length > 0) {
      imageFiles.forEach(file => formData.append('images', file));
    }

    return this.http.post<Product>(this.apiUrl, formData).pipe(
      catchError(this.handleError)
    );
  }

    // ✅ Update product (with optional new images)
  // updateProduct(id: number, product: Product, imageFiles?: File[]): Observable<Product> {
  //   const formData = new FormData();

  //   // Include product data as JSON
  //   formData.append('product', new Blob([JSON.stringify(product)], { type: 'application/json' }));

  //   // Append all selected files (if any)
  //   if (imageFiles && imageFiles.length > 0) {
  //     imageFiles.forEach((file, index) => {
  //       formData.append('images', file); // matches backend @RequestParam("images")
  //     });
  //   }

  //   return this.http.put<Product>(`${this.apiUrl}/${id}`, formData).pipe(
  //     catchError(this.handleError)
  //   );
  // }

  updateProduct(id: number, product: Product, imageFiles?: File[]): Observable<Product> {
  const formData = new FormData();

  formData.append(
    "product",
    new Blob([JSON.stringify(product)], { type: "application/json" })
  );

  if (imageFiles) {
    imageFiles.forEach(file => formData.append("images", file));
  }

  return this.http.put<Product>(`${this.apiUrl}/${id}`, formData);
}

  // ✅ Update product (with optional new images)
  updateProduct2(id: number, product: Product, imageFiles?: File[]): Observable<Product> {
    const formData = new FormData();

    formData.append('name', product.name);
    formData.append('description', product.description || '');
    formData.append('price', product.price.toString());
    formData.append('quantity', product.quantity.toString());
    formData.append('categoryId', product.category?.id?.toString() || '');

    if (imageFiles && imageFiles.length > 0) {
      imageFiles.forEach(file => formData.append('images', file));
    }

    return this.http.put<Product>(`${this.apiUrl}/${id}`, formData).pipe(
      catchError(this.handleError)
    );
  }

  // ✅ Delete product
  deleteProduct(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`).pipe(
      catchError(this.handleError)
    );
  }

  // ✅ Global error handler
  private handleError(error: any) {
    console.error('API Error:', error);
    let errorMessage = 'An unknown error occurred';

    if (error.error instanceof ErrorEvent) {
      errorMessage = `Client Error: ${error.error.message}`;
    } else if (error.error && typeof error.error === 'string') {
      errorMessage = error.error;
    } else if (error.message) {
      errorMessage = error.message;
    } else if (error.status === 500) {
      errorMessage = 'Server error: Please check backend logs';
    }

    return throwError(() => new Error(errorMessage));
  }
}










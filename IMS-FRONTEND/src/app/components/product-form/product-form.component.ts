import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';

import { ProductService } from '../../services/product.service';
import { CategoryService } from '../../services/category.service';

import { Product } from '../../models/product';
import { Category } from '../../models/category';

@Component({
  selector: 'app-product-form',
  templateUrl: './product-form.component.html',
  styleUrls: ['./product-form.component.css'],
})
export class ProductFormComponent implements OnInit {

  productForm!: FormGroup;


  isEditMode = false;
  productId?: number;
  loading = false;

  //IMAGE
  categories: Category[] = [];

  selectedFiles: File[] = [];
  imagePreviews: string[] = [];
  existingImageUrls: string[] = [];

  constructor(
    private fb: FormBuilder,
    private productService: ProductService,
    private categoryService: CategoryService,
    private route: ActivatedRoute,
    private router: Router,
  ) { }

  ngOnInit(): void {
    // Create form
    this.productForm = this.fb.group({
      name: ['', Validators.required],
      description: [''],
      price: [0, [Validators.required, Validators.min(0)]],
      quantity: [0, [Validators.required, Validators.min(0)]],
      category: [null, Validators.required],
    });

    // Load categories
    this.loadCategories();

    // Check edit mode
    const id = this.route.snapshot.paramMap.get('id');

    if (id) {
      this.isEditMode = true;

      this.productId = Number(id);

      this.loadProduct(this.productId);
    }
  }

  // ============================
  // Load Categories
  // ============================

  loadCategories(): void {
    this.categoryService.getCategories().subscribe({
      next: (categories) => {
        this.categories = categories;
      },

      error: (error) => {
        console.error(error);
      },
    });
  }

  // ============================
  // Load Product
  // ============================

  loadProduct(id: number): void {
    this.loading = true;

    this.productService.getByID(id).subscribe({
      next: (product: Product) => {

        this.productForm.patchValue({
          name: product.name,

          description: product.description,

          price: product.price,

          quantity: product.quantity,

          category: product.category,
        });

        // Existing images
        this.existingImageUrls = product.imageUrls || [];
        this.imagePreviews = [...this.existingImageUrls];

        this.loadProductImages(product);
        this.loading = false;
      },

      error: (error) => {
        console.error(error);
        this.loading = false;
        this.router.navigate(['/products']);
      },
    });
  }

  loadProductImages(product: Product) {

    const urls = product.imageUrls?.filter(url => !!url?.trim()) ?? [];

    const updatedURLs = urls.map(url =>
       url.startsWith('http') ? url : "http://localhost:8080" + url
    );

    // Existing images
    this.existingImageUrls = updatedURLs;

    // Show existing images
    this.imagePreviews = updatedURLs;

  }


  // ============================
  // Select Images
  // ============================

  onFilesSelected(event: Event): void {
    const input = event.target as HTMLInputElement;

    if (!input.files) {
      return;
    }

    const files = Array.from(input.files);

    files.forEach((file) => {
      // Check image
      if (!file.type.startsWith('image/')) {
        alert('Only image files are allowed.');
        return;
      }

      // Check size 10MB
      if (file.size > 20 * 1024 * 1024) {
        alert('Image size cannot exceed 10MB.');

        return;
      }

      // Add file
      this.selectedFiles.push(file);

      // Create preview
      const reader = new FileReader();

      reader.onload = () => {
        this.imagePreviews.push(reader.result as string);
      };

      reader.readAsDataURL(file);
    });

    // Allow selecting same file again
    input.value = '';
  }

  // ============================
  // Remove Image
  // ============================

  removeFile(index: number): void {
    // Existing image
    if (index < this.existingImageUrls.length) {
      this.existingImageUrls.splice(index, 1);
    }

    // New selected image
    else {
      const fileIndex = index - this.existingImageUrls.length;

      this.selectedFiles.splice(fileIndex, 1);
    }

    // Remove preview
    this.imagePreviews.splice(index, 1);
  }

  // ============================
  // Save Product
  // ============================

  saveProduct(): void {
    // Check validation
    if (this.productForm.invalid) {
      this.productForm.markAllAsTouched();
      return;
    }

    // Get form data
    const product: Product = {
      ...this.productForm.value,
      id: this.productId,
      imageUrls: [...this.existingImageUrls],
    };

    // Update
    if (this.isEditMode) {
      this.productService
        .updateProduct(this.productId!, product, this.selectedFiles)
        .subscribe({
          next: () => {
            this.router.navigate(['/products']);
          },

          error: (error) => {
            alert(error.message);
          },
        });
    } else {
      this.productService.create(product, this.selectedFiles).subscribe({
        next: () => {
          this.router.navigate(['/products']);
        },

        error: (error) => {
          alert(error.message);
        },
      });
    }
  }

  // ============================
  // Cancel
  // ============================

  cancel(): void {
    this.router.navigate(['/products']);
  }

  // ============================
  // Easy Validation Helper
  // ============================

  isInvalid(fieldName: string): boolean {
    const field = this.productForm.get(fieldName);

    return !!(field && field.touched && field.invalid);
  }
}

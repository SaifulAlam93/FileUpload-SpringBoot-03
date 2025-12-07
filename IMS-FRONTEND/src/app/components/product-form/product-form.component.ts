import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { Product } from '../../models/product';
import { Category } from '../../models/category';
import { ProductService } from '../../services/product.service';
import { CategoryService } from '../../services/category.service';

@Component({
  selector: 'app-product-form',
  templateUrl: './product-form.component.html',
  styleUrls: ['./product-form.component.css']
})
export class ProductFormComponent implements OnInit {

  product: Product = {
    name: '',
    description: '',
    price: 0,
    quantity: 0,
    category: {} as Category,
    imageUrls: []
  };

  categories: Category[] = [];
  isEditMode = false;
  loading = false;

  selectedFiles: File[] = [];            // New uploads
  imagePreviews: (string | ArrayBuffer | null)[] = [];

  existingImageUrls: string[] = [];      // Old URLs kept after user removes some

  constructor(
    private productService: ProductService,
    private categoryService: CategoryService,
    private route: ActivatedRoute,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadCategories();

    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.isEditMode = true;
      this.loadProduct(Number(id));
    }
  }

  loadCategories(): void {
    this.categoryService.getCategories().subscribe(categories => {
      this.categories = categories;

      if (!this.product.category.id && categories.length > 0) {
        this.product.category = categories[0];
      }
    });
  }

  loadProduct(id: number): void {
    this.loading = true;

    this.productService.getProduct(id).subscribe(
      product => {
        this.product = product;
        this.loading = false;

        // FIX: Prevent undefined error
        this.existingImageUrls = [...(product.imageUrls ?? [])];

        // Show previews
        this.imagePreviews = [...this.existingImageUrls];
      },
      error => {
        console.error(error);
        this.router.navigate(['/products']);
      }
    );
  }

  onFilesSelected(event: any): void {
    const files = event.target.files;
    if (!files.length) return;

    for (let i = 0; i < files.length; i++) {
      const file = files[i];

      if (!file.type.startsWith('image/')) { 
        alert("Invalid file"); 
        continue; }
      if (file.size > 10 * 1024 * 1024) { 
        alert("File too large"); 
        continue; }

      this.selectedFiles.push(file);

      const reader = new FileReader();
      reader.onload = () => this.imagePreviews.push(reader.result!);
      reader.readAsDataURL(file);
    }
  }

  removeFile(index: number): void {
    const preview = this.imagePreviews[index];

    if (typeof preview === 'string' && preview.startsWith('http')) {
      // Remove old image
      this.existingImageUrls = this.existingImageUrls.filter(url => url !== preview);
    } else {
      // Remove new uploaded image
      const newIndex = index - this.existingImageUrls.length;
      this.selectedFiles.splice(newIndex, 1);
    }

    this.imagePreviews.splice(index, 1);
  }

  saveProduct(): void {
    // Attach remaining old images before update
    this.product.imageUrls = [...this.existingImageUrls];

    if (this.isEditMode) {
      this.productService.updateProduct(this.product.id!, this.product, this.selectedFiles)
        .subscribe(
          () => this.router.navigate(['/products']),
          error => alert(error.message)
        );
    } else {
      this.productService.createProduct(this.product, this.selectedFiles)
        .subscribe(
          () => this.router.navigate(['/products']),
          error => alert(error.message)
        );
    }
  }

  cancel(): void {
    this.router.navigate(['/products']);
  }
}

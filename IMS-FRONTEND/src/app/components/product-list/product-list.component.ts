import { Component, HostListener, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { Product } from '../../models/product';
import { Category } from '../../models/category';
import { ProductService } from '../../services/product.service';
import { CategoryService } from '../../services/category.service';

@Component({
  selector: 'app-product-list',
  templateUrl: './product-list.component.html',
  styleUrls: ['./product-list.component.css']
})
export class ProductListComponent implements OnInit {
  products: Product[] = [];
  categories: Category[] = [];

  selectedImageUrl: string | null = null;
  selectedImageAlt = '';

  constructor(
    private productService: ProductService,
    private categoryService: CategoryService,
    private router: Router
  ) { }

  ngOnInit(): void {
    this.loadProducts();
    this.loadCategories();
  }

  loadProducts(): void {
    this.productService.getAll().subscribe(
      products => this.products = products
    );
  }

  loadCategories(): void {
    this.categoryService.getCategories().subscribe(
      categories => this.categories = categories
    );
  }

  addProduct(): void {
    this.router.navigate(['/products/new']);
  }

  editProduct(id: number): void {
    this.router.navigate(['/products/edit', id]);
  }

  deleteProduct(id: number): void {
    if (confirm('Are you sure you want to delete this product?')) {
      this.productService.deleteProduct(id).subscribe(
        () => {
          this.products = this.products.filter(p => p.id !== id);
        }
      );
    }
  }

  getCategoryName(category: Category): string {
    return category ? category.name : 'No Category';
  }

  getProductImages(product: Product): string[] {
    const urls = product.imageUrls?.filter(url => !!url?.trim())?? [];

    return urls.map(url =>
      url.startsWith('http') ? url : "http://localhost:8080" + url
    );
  }

  openImageViewer(url: string, name: string, index: number): void {
    this.selectedImageUrl = url.startsWith('http')
      ? url
      : `http://localhost:8080${url}`;

    this.selectedImageAlt = `${name} image ${index + 1}`;
  }

  closeImageViewer(): void {
    this.selectedImageUrl = null;
    this.selectedImageAlt = '';
  }

  @HostListener('document:keydown.escape')
  onEscapeKey(): void {
    this.closeImageViewer();
  }
}


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

  selectedFiles: File[] = [];
  imagePreviews: (string | ArrayBuffer | null)[] = [];
// selectedFiles: File[] = [];              // For new uploads
// imagePreviews: (string | ArrayBuffer)[] = [];  // For all previews
   existingImageUrls: string[] = [];        // For old images (from DB)

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
         // Keep old image URLs separately
      this.existingImageUrls = product.imageUrls || [];

      // Show those URLs as previews too
      this.imagePreviews = [...this.existingImageUrls];

      this.selectedFiles = (product.imageUrls || []).map(url => {
        const filename = url.split('/').pop() || 'unknown.jpg';
        // Create a dummy File object (no real content, just metadata)
        console.log('------filename----', filename);
        
        return new File([], filename, { type: 'image/*' });
      });

      console.log(this.selectedFiles.length);
      
      },
      error => {
        console.error('Error loading product:', error);
        this.loading = false;
        this.router.navigate(['/products']);
      }
    );
  }

//   async loadProduct(id: number): Promise<void> {
//   this.loading = true;
//   this.productService.getProduct(id).subscribe(
//     async (product) => {
//       this.product = product;
//       this.loading = false;

//       if (this.product.imageUrls && this.product.imageUrls.length > 0) {
//         this.selectedFiles = await Promise.all(
//           this.product.imageUrls.map(async (url: string) => {
//             const response = await fetch(url);
//             const blob = await response.blob();
//             const fileName = url.split('/').pop() || 'image.jpg';
//             return new File([blob], fileName, { type: blob.type });
//           })
//         );

//         // Generate previews from URLs
//         this.imagePreviews = [...this.product.imageUrls];
//       }
//     },
//     (error) => {
//       console.error('Error loading product:', error);
//       this.loading = false;
//       this.router.navigate(['/products']);
//     }
//   );
// }


  // ✅ Multiple file selection
  // onFilesSelected(event: any): void {
  //   const files: FileList = event.target.files;
  //   if (files.length === 0) return;

  //   for (let i = 0; i < files.length; i++) {
  //     const file = files[i];

  //     // Validate file type
  //     if (!file.type.startsWith('image/')) {
  //       alert(`File ${file.name} is not an image.`);
  //       continue;
  //     }

  //     // Validate file size
  //     if (file.size > 5 * 1024 * 1024) {
  //       alert(`File ${file.name} exceeds 5MB limit.`);
  //       continue;
  //     }

  //     this.selectedFiles.push(file);

  //     // Preview
  //     const reader = new FileReader();
  //     reader.onload = (e) => this.imagePreviews.push(reader.result);
  //     reader.readAsDataURL(file);
  //   }
  // }
onFilesSelected(event: any): void {
  const files: FileList = event.target.files;
  if (!files.length) return;

  for (let i = 0; i < files.length; i++) {
    const file = files[i];

    if (!file.type.startsWith('image/')) {
      alert(`File ${file.name} is not an image.`);
      continue;
    }

    if (file.size > 5 * 1024 * 1024) {
      alert(`File ${file.name} exceeds 5MB limit.`);
      continue;
    }

    this.selectedFiles.push(file);

    const reader = new FileReader();
    reader.onload = () => this.imagePreviews.push(reader.result!);
    reader.readAsDataURL(file);
  }
}

  // removeFile(index: number): void {
  //   this.selectedFiles.splice(index, 1);
  //   this.imagePreviews.splice(index, 1);
  // }
removeFile(index: number): void {
  // If it’s an existing image URL (string), remove from existing list
  const preview = this.imagePreviews[index];
  console.log('---------------', preview);
  
  if (typeof preview === 'string' && preview.startsWith('http')) {
    this.existingImageUrls = this.existingImageUrls.filter(url => url !== preview);
    this.selectedFiles.splice(index - this.existingImageUrls.length, 1);

  } else {
    // If it’s a newly added file, remove from selectedFiles
    this.selectedFiles.splice(index - this.existingImageUrls.length, 1);
  }

  // Remove preview
  this.imagePreviews.splice(index, 1);
}

  saveProduct(): void {
          console.log('Total ------------------'+this.selectedFiles.length);

    if (!this.product.name || !this.product.price || !this.product.quantity) {
      alert('Please fill in all required fields');
      return;
    }

    if (!this.product.category || !this.product.category.id) {
      alert('Please select a category');
      return;
    }

    this.loading = true;

    if (this.isEditMode) {
      this.productService.updateProduct(this.product.id!, this.product, this.selectedFiles).subscribe(
        () => {
          this.loading = false;
          this.router.navigate(['/products']);
        },
        error => {
          console.error('Error updating product:', error);
          alert('Error updating product: ' + error.message);
          this.loading = false;
        }
      );
    } else {
      this.productService.createProduct(this.product, this.selectedFiles).subscribe(
        () => {
          this.loading = false;
          this.router.navigate(['/products']);
        },
        error => {
          console.error('Error creating product:', error);
          alert('Error creating product: ' + error.message);
          this.loading = false;
        }
      );
    }
  }

  cancel(): void {
    this.router.navigate(['/products']);
  }
}



// import { Component, OnInit } from '@angular/core';
// import { ActivatedRoute, Router } from '@angular/router';
// import { Product } from '../../models/product';
// import { Category } from '../../models/category';
// import { ProductService } from '../../services/product.service';
// import { CategoryService } from '../../services/category.service';

// @Component({
//   selector: 'app-product-form',
//   templateUrl: './product-form.component.html',
//   styleUrls: ['./product-form.component.css']
// })
// export class ProductFormComponent implements OnInit {
//   product: Product = { 
//     name: '',
//     description: '',
//     price: 0,
//     quantity: 0,
//     category: {} as Category
//   };
  
//   categories: Category[] = [];
//   isEditMode = false;
//   loading = false;
//   selectedFile: File | null = null;
//   imagePreview: string | ArrayBuffer | null = null;

//   constructor(
//     private productService: ProductService,
//     private categoryService: CategoryService,
//     private route: ActivatedRoute,
//     private router: Router
//   ) { }

//   ngOnInit(): void {
//     this.loadCategories();
    
//     const id = this.route.snapshot.paramMap.get('id');
//     if (id) {
//       this.isEditMode = true;
//       this.loadProduct(Number(id));
//     }
//   }

//   loadCategories(): void {
//     this.categoryService.getCategories().subscribe(
//       categories => {
//         this.categories = categories;
//         // If no category is selected and we have categories, select the first one
//         if (!this.product.category.id && categories.length > 0) {
//           this.product.category = categories[0];
//         }
//       }
//     );
//   }

//   loadProduct(id: number): void {
//     this.loading = true;
//     this.productService.getProduct(id).subscribe(
//       product => {
//         this.product = product;
//         this.loading = false;
//       },
//       error => {
//         console.error('Error loading product:', error);
//         this.loading = false;
//         this.router.navigate(['/products']);
//       }
//     );
//   }

//   onFileSelected(event: any): void {
//     const file = event.target.files[0];
//     if (file) {
//       // Validate file type
//       if (!file.type.startsWith('image/')) {
//         alert('Please select an image file (JPEG, PNG, GIF, etc.)');
//         return;
//       }

//       // Validate file size (5MB limit)
//       if (file.size > 5 * 1024 * 1024) {
//         alert('File size must be less than 5MB');
//         return;
//       }

//       this.selectedFile = file;
      
//       // Create preview
//       const reader = new FileReader();
//       reader.onload = () => {
//         this.imagePreview = reader.result;
//       };
//       reader.readAsDataURL(file);
//     }
//   }

//   removeImage(): void {
//     this.selectedFile = null;
//     this.imagePreview = null;
//     this.product.imageUrl = '';
//   }

//   saveProduct(): void {
//     if (!this.product.name || !this.product.price || !this.product.quantity) {
//       alert('Please fill in all required fields');
//       return;
//     }

//     if (!this.product.category || !this.product.category.id) {
//       alert('Please select a category');
//       return;
//     }

//     this.loading = true;

//     if (this.isEditMode) {
//       this.productService.updateProduct(this.product.id!, this.product, this.selectedFile!).subscribe(
//         () => {
//           this.loading = false;
//           this.router.navigate(['/products']);
//         },
//         error => {
//           console.error('Error updating product:', error);
//           alert('Error updating product: ' + error.message);
//           this.loading = false;
//         }
//       );
//     } else {
//       this.productService.createProduct(this.product, this.selectedFile!).subscribe(
//         (savedProduct) => {
//           this.loading = false;
//           this.router.navigate(['/products']);
//         },
//         error => {
//           console.error('Error creating product:', error);
//           alert('Error creating product: ' + error.message);
//           this.loading = false;
//         }
//       );
//     }
//   }

//   cancel(): void {
//     this.router.navigate(['/products']);
//   }
// }
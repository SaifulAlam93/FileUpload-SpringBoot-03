import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { Category } from '../../models/category';
import { CategoryService } from '../../services/category.service';

@Component({
  selector: 'app-category-form',
  templateUrl: './category-form.component.html',
  styleUrls: ['./category-form.component.css']
})
export class CategoryFormComponent implements OnInit {
  category: Category = {
    name: '',
    description: ''
  };
  
  isEditMode = false;
  loading = false;

  constructor(
    private categoryService: CategoryService,
    private route: ActivatedRoute,
    private router: Router
  ) { }

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.isEditMode = true;
      this.loadCategory(Number(id));
    }
  }

  loadCategory(id: number): void {
    this.loading = true;
    this.categoryService.getCategory(id).subscribe(
      category => {
        this.category = category;
        this.loading = false;
      },
      error => {
        console.error('Error loading category:', error);
        this.loading = false;
        this.router.navigate(['/categories']);
      }
    );
  }

  saveCategory(): void {
    if (!this.category.name) {
      alert('Please enter a category name');
      return;
    }

    this.loading = true;
    
    if (this.isEditMode) {
      this.categoryService.updateCategory(this.category.id!, this.category).subscribe(
        () => {
          this.loading = false;
          this.router.navigate(['/categories']);
        },
        error => {
          console.error('Error updating category:', error);
          alert('Error updating category: ' + error.message);
          this.loading = false;
        }
      );
    } else {
      this.categoryService.createCategory(this.category).subscribe(
        () => {
          this.loading = false;
          this.router.navigate(['/categories']);
        },
        error => {
          console.error('Error creating category:', error);
          alert('Error creating category: ' + error.message);
          this.loading = false;
        }
      );
    }
  }

  cancel(): void {
    this.router.navigate(['/categories']);
  }
}
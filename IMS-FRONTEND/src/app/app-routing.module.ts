import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { CategoryListComponent } from './components/category-list/category-list.component';
import { ProductListComponent } from './components/product-list/product-list.component';
import { CategoryFormComponent } from './components/category-form/category-form.component';
import { ProductFormComponent } from './components/product-form/product-form.component';

const routes: Routes = [
  { path: '', redirectTo: '/products', pathMatch: 'full' },
  { 
    path: 'products', 
    component: ProductListComponent
  },
  { 
    path: 'products/new', 
    component: ProductFormComponent,
    data: { mode: 'create' }
  },
  { 
    path: 'products/edit/:id', 
    component: ProductFormComponent,
    data: { mode: 'edit' }
  },
  { 
    path: 'categories', 
    component: CategoryListComponent
  },
  { 
    path: 'categories/new', 
    component: CategoryFormComponent,
    data: { mode: 'create' }
  },
  { 
    path: 'categories/edit/:id', 
    component: CategoryFormComponent,
    data: { mode: 'edit' }
  },
  { path: '**', redirectTo: '/products' }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
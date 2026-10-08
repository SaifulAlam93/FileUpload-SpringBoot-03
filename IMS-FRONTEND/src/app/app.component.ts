import { Component } from '@angular/core';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent {
  title = 'Inventory Management System';
  activeTab: string = 'products';

  setActiveTab(tab: string): void {
    this.activeTab = tab;
  }
}
import { Component } from '@angular/core';
import { MainAppComponent } from './components/main-app/main-app.component';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [MainAppComponent],
  template: `
    <app-main-app></app-main-app>
  `,
  styleUrl: './app.scss'
})
export class App {
  protected readonly title = 'Your Car Your Way - Chat POC';
}

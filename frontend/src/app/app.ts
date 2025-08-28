import { Component } from '@angular/core';
import { MockLoginComponent } from './components/mock-login/mock-login.component';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [MockLoginComponent],
  template: `
    <app-mock-login></app-mock-login>
  `,
  styleUrl: './app.scss'
})
export class App {
  protected readonly title = 'Your Car Your Way - Chat POC';
}

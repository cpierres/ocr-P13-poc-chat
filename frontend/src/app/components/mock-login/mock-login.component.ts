import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MockAuthService } from '../../services/mock-auth.service';
import { MockUser } from '../../models/user.model';

@Component({
  selector: 'app-mock-login',
  standalone: true,
  imports: [
    CommonModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatProgressSpinnerModule
  ],
  template: `
    <div class="login-container">
      <mat-card class="login-card">
        <mat-card-header>
          <mat-card-title>Your Car Your Way - Chat POC</mat-card-title>
          <mat-card-subtitle>Sélectionnez votre rôle pour la démonstration</mat-card-subtitle>
        </mat-card-header>

        <mat-card-content>
          @if (currentUser()) {
            <div class="current-user">
              <h4>✅ Connecté en tant que :</h4>
              <p><strong>{{ currentUser()?.firstName }} {{ currentUser()?.lastName }}</strong></p>
              <p><em>{{ currentUser()?.role }} - {{ currentUser()?.email }}</em></p>
            </div>
          } @else {
            <div class="select-user">
              <p>Choisissez votre rôle pour tester le chat :</p>
            </div>
          }
        </mat-card-content>

        <mat-card-actions>
          @if (isLoading()) {
            <mat-spinner diameter="30"></mat-spinner>
          } @else {
            <button mat-stroked-button
                    color="primary"
                    (click)="loginAsClient()"
                    [disabled]="currentUser()?.role === 'CLIENT'">
              <!-- mat-icon>person</mat-icon -->
              Se connecter en tant que Client
            </button>

            <button mat-stroked-button
                    color="accent"
                    (click)="loginAsAgent()"
                    [disabled]="currentUser()?.role === 'AGENT'">
              <!-- mat-icon>support_agent</mat-icon -->
              Se connecter en tant qu'Agent
            </button>

            @if (currentUser()) {
              <button mat-stroked-button (click)="logout()">
                <mat-icon>logout</mat-icon>
                Se déconnecter
              </button>
            }
          }
        </mat-card-actions>
      </mat-card>
    </div>
  `,
  styles: [`
    .login-container {
      display: flex;
      justify-content: center;
      align-items: center;
      min-height: 100vh;
      padding: 20px;
      background: white;
    }

    .login-card {
      max-width: 500px;
      width: 100%;
      box-shadow: 0 10px 30px rgba(0,0,0,0.2);
    }

    .current-user {
      background: #e8f5e8;
      padding: 16px;
      border-radius: 8px;
      margin: 16px 0;
      border-left: 4px solid #4caf50;
    }

    .select-user {
      text-align: center;
      padding: 16px 0;
    }

    mat-card-actions {
      display: flex;
      gap: 12px;
      flex-wrap: wrap;
      justify-content: center;
    }

    mat-card-title {
      color: #333;
      font-weight: 500;
    }

    mat-card-subtitle {
      color: #666;
    }
  `]
})
export class MockLoginComponent implements OnInit {
  currentUser = signal<MockUser | null>(null);
  isLoading = signal(false);

  constructor(private mockAuthService: MockAuthService) {}

  ngOnInit() {
    this.mockAuthService.getCurrentUser().subscribe(user => {
      this.currentUser.set(user);
    });
  }

  loginAsClient() {
    this.isLoading.set(true);
    this.mockAuthService.loginAsClient().subscribe({
      next: (response) => {
        console.log('✅ Connexion client réussie:', response.message);
        this.isLoading.set(false);
      },
      error: (error) => {
        console.error('❌ Erreur connexion client:', error);
        this.isLoading.set(false);
        alert('Erreur de connexion client');
      }
    });
  }

  loginAsAgent() {
    this.isLoading.set(true);
    this.mockAuthService.loginAsAgent().subscribe({
      next: (response) => {
        console.log('✅ Connexion agent réussie:', response.message);
        this.isLoading.set(false);
      },
      error: (error) => {
        console.error('❌ Erreur connexion agent:', error);
        this.isLoading.set(false);
        alert('Erreur de connexion agent');
      }
    });
  }

  logout() {
    this.mockAuthService.logout();
    console.log('👋 Déconnexion réussie');
  }
}

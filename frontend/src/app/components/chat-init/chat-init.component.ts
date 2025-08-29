import { Component, OnInit, signal, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatSelectModule } from '@angular/material/select';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatListModule } from '@angular/material/list';

import { MockAuthService } from '../../services/mock-auth.service';
import { ChatService } from '../../services/chat.service';
import { TicketService } from '../../services/ticket.service';
import { MockUser } from '../../models/user.model';
import { SupportTicket } from '../../models/ticket.model';

@Component({
  selector: 'app-chat-init',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatCardModule,
    MatInputModule,
    MatButtonModule,
    MatSelectModule,
    MatIconModule,
    MatProgressSpinnerModule,
    MatListModule
  ],
  template: `
    <div class="chat-init-container">
      <mat-card class="init-card">
        <mat-card-header>
          <mat-card-title>Your Car Your Way - Support Chat</mat-card-title>
          <mat-card-subtitle>Gérer vos tickets de support</mat-card-subtitle>
        </mat-card-header>

        <mat-card-content>
          <!-- Utilisateur connecté -->
          <div class="user-info">
            <h4>👤 Utilisateur connecté :</h4>
            <p><strong>{{ currentUser()?.firstName }} {{ currentUser()?.lastName }}</strong></p>
            <p><em>{{ currentUser()?.role }} - {{ currentUser()?.email }}</em></p>
            <button mat-stroked-button (click)="logout()">
              Se déconnecter
            </button>
          </div>

          <!-- Tickets existants -->
          <div class="tickets-section">
            <h4>🎫 Tickets existants :</h4>
            @if (isLoadingTickets()) {
              <div class="loading-section">
                <mat-spinner diameter="30"></mat-spinner>
                <p>Chargement des tickets...</p>
              </div>
            } @else if (existingTickets().length > 0) {
              <div class="tickets-list">
                @for (ticket of existingTickets(); track ticket.id) {
                  <div class="ticket-item" (click)="selectExistingTicket(ticket)">
                    <div class="ticket-header">
                      <strong>{{ ticket.subject }}</strong>
                      <span class="ticket-status" [class]="getStatusClass(ticket.status)">
                        {{ ticket.status }}
                      </span>
                    </div>
                    <div class="ticket-details">
                      <small>Créé le {{ ticket.createdAt | date:'dd/MM/yyyy à HH:mm' }}</small>
                      @if (ticket.assignedAgent) {
                        <small>• Agent : {{ ticket.assignedAgent }}</small>
                      }
                    </div>
                    <div class="ticket-description">
                      {{ ticket.description | slice:0:100 }}
                      @if (ticket.description && ticket.description.length > 100) {
                        <span>...</span>
                      }
                    </div>
                  </div>
                }
              </div>
            } @else {
              <div class="no-tickets">
                <mat-icon>assignment</mat-icon>
                <p>Aucun ticket existant</p>
                <p class="hint">Créez votre premier ticket de support ci-dessous</p>
              </div>
            }
          </div>

          @if (currentUser()?.role === 'CLIENT') {
          <!-- Création de ticket -->
          <div class="create-section">
            <h4>Créer un ticket :</h4>

            <mat-form-field appearance="outline" class="full-width">
              <mat-label>Sujet du ticket</mat-label>
              <input matInput
                     [(ngModel)]="newTicketSubject"
                     placeholder="Ex: Problème avec ma réservation"
                     maxlength="255">
              <mat-hint>Décrivez brièvement votre problème</mat-hint>
            </mat-form-field>

            <mat-form-field appearance="outline" class="full-width">
              <mat-label>Description détaillée</mat-label>
              <textarea matInput
                       [(ngModel)]="newTicketDescription"
                       placeholder="Expliquez votre problème en détail..."
                       rows="4"
                       maxlength="1000"></textarea>
              <mat-hint>{{ newTicketDescription.length || 0 }}/1000 caractères</mat-hint>
            </mat-form-field>

            <div class="create-actions">
              <button mat-stroked-button
                      color="primary"
                      (click)="createAndStartChat()"
                      [disabled]="!canCreateTicket() || isCreating()">
                <span>➕ Créer ticket et démarrer chat</span>
              </button>
              <button mat-stroked-button (click)="clearForm()">
                <mat-icon>clear</mat-icon>
                Effacer
              </button>
            </div>
          </div>
          }
        </mat-card-content>
      </mat-card>
    </div>

  `,
  styles: [`
    .chat-init-container {
      padding: 20px;
      max-width: 800px;
      margin: 0 auto;
      min-height: 100vh;
      background: #f5f5f5;
    }

    .init-card {
      width: 100%;
      box-shadow: 0 8px 25px rgba(0,0,0,0.12);
    }

    .user-info {
      background: #e3f2fd;
      padding: 16px;
      border-radius: 8px;
      margin: 16px 0;
      border-left: 4px solid #2196f3;
    }

    .tickets-section, .create-section {
      margin: 24px 0;
      padding-top: 16px;
      border-top: 1px solid #e0e0e0;
    }

    .loading-section {
      display: flex;
      flex-direction: column;
      align-items: center;
      padding: 20px;
      color: #666;
    }

    .tickets-list {
      max-height: 300px;
      overflow-y: auto;
      border: 1px solid #e0e0e0;
      border-radius: 8px;
    }

    .ticket-item {
      padding: 16px;
      border-bottom: 1px solid #f0f0f0;
      cursor: pointer;
      transition: all 0.2s ease;
    }

    .ticket-item:hover {
      background-color: #f8f8f8;
      transform: translateX(4px);
    }

    .ticket-item:last-child {
      border-bottom: none;
    }

    .ticket-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 8px;
    }

    .ticket-status {
      padding: 4px 12px;
      border-radius: 12px;
      font-size: 0.75em;
      font-weight: 600;
      text-transform: uppercase;
    }

    .ticket-status.open {
      background: #e8f5e8;
      color: #2e7d32;
    }

    .ticket-status.in_progress {
      background: #fff3e0;
      color: #ef6c00;
    }

    .ticket-status.resolved {
      background: #e3f2fd;
      color: #1976d2;
    }

    .ticket-status.closed {
      background: #fce4ec;
      color: #c2185b;
    }

    .ticket-details {
      display: flex;
      gap: 16px;
      margin-bottom: 8px;
      color: #666;
      font-size: 0.875em;
    }

    .ticket-description {
      color: #777;
      font-size: 0.875em;
      line-height: 1.4;
    }

    .no-tickets {
      text-align: center;
      padding: 40px 20px;
      color: #999;
    }

    .no-tickets mat-icon {
      font-size: 48px;
      width: 48px;
      height: 48px;
      color: #ccc;
      margin-bottom: 16px;
    }

    .no-tickets .hint {
      font-style: italic;
      color: #666;
    }

    .create-section {
      background: #f9f9f9;
      padding: 20px;
      border-radius: 8px;
    }

    .full-width {
      width: 100%;
      margin-bottom: 16px;
    }

    .create-actions {
      display: flex;
      gap: 12px;
      margin-top: 16px;
    }

    .create-actions button {
      display: flex;
      align-items: center;
      gap: 8px;
    }

    h4 {
      color: #333;
      margin: 0 0 12px 0;
      display: flex;
      align-items: center;
      gap: 8px;
    }

    @media (max-width: 600px) {
      .chat-init-container {
        padding: 10px;
      }

      .create-actions {
        flex-direction: column;
      }

      .ticket-details {
        flex-direction: column;
        gap: 4px;
      }
    }
  `]
})
export class ChatInitComponent implements OnInit {
  currentUser = signal<MockUser | null>(null);
  existingTickets = signal<SupportTicket[]>([]);
  isLoadingTickets = signal<boolean>(false);
  isCreating = signal<boolean>(false);

  newTicketSubject = '';
  newTicketDescription = '';

  @Output() chatStarted = new EventEmitter<SupportTicket>();

  constructor(
    private mockAuthService: MockAuthService,
    private chatService: ChatService,
    private ticketService: TicketService
  ) {}

  async ngOnInit() {
    // S'abonner à l'utilisateur actuel
    this.mockAuthService.getCurrentUser().subscribe(user => {
      this.currentUser.set(user);
      //user?.role === 'AGENT' ? this.loadUserTickets(user.id) : this.existingTickets.set([]);
      user?.role === 'CLIENT' ? this.loadUserTickets(user.id) : this.loadUserTickets("822d37e8-812f-4059-81ac-357cb3b45b50");
      //if (user) {
      //  this.loadUserTickets(user.id);
      //}
    });
  }

  /**
   * si je suis connecté en tant qu'agent, je dois afficher les tickets qui m'ont été assignés
   * @param userId
   * @private
   */
  private loadUserTickets(userId: string) {
    this.isLoadingTickets.set(true);

    this.ticketService.getTicketsByUserId(userId).subscribe({
      next: (tickets) => {
        // Convertir les dates string en Date objects
        const processedTickets = tickets.map(ticket => ({
          ...ticket,
          createdAt: new Date(ticket.createdAt),
          updatedAt: ticket.updatedAt ? new Date(ticket.updatedAt) : undefined
        }));

        // Trier par date de création (plus récent en premier)
        processedTickets.sort((a, b) => b.createdAt.getTime() - a.createdAt.getTime());

        this.existingTickets.set(processedTickets);
        this.isLoadingTickets.set(false);
        console.log('🎫 Tickets chargés:', tickets.length);
      },
      error: (error) => {
        console.error('❌ Erreur chargement tickets:', error);
        this.existingTickets.set([]);
        this.isLoadingTickets.set(false);
      }
    });
  }

  async selectExistingTicket(ticket: SupportTicket) {
    try {
      console.log('🔗 Sélection du ticket:', ticket.subject);
      await this.chatService.joinTicket(ticket);
      this.chatStarted.emit(ticket);
    } catch (error) {
      console.error('❌ Erreur connexion ticket:', error);
      alert('Erreur lors de la connexion au ticket');
    }
  }

  async createAndStartChat() {
    if (!this.canCreateTicket()) {
      return;
    }

    this.isCreating.set(true);

    try {
      console.log('Création d\'un ticket:', this.newTicketSubject);

      const ticket = await this.chatService.createNewTicket(
        this.newTicketSubject.trim(),
        this.newTicketDescription.trim() || 'Aucune description fournie'
      );

      // Démarrer le chat avec le nouveau ticket
      await this.chatService.joinTicket(ticket);

      // Effacer le formulaire
      this.clearForm();

      // Recharger la liste des tickets
      const currentUser = this.currentUser();
      if (currentUser) {
        this.loadUserTickets(currentUser.id);
      }

      // Émettre l'événement de chat démarré
      this.chatStarted.emit(ticket);

      console.log('✅ Ticket créé et chat démarré:', ticket.subject);

    } catch (error) {
      console.error('❌ Erreur création/connexion ticket:', error);
      alert('Erreur lors de la création du ticket: ' + (error as Error).message);
    } finally {
      this.isCreating.set(false);
    }
  }

  canCreateTicket(): boolean {
    return !!(
      this.currentUser() &&
      this.newTicketSubject.trim().length > 0 &&
      this.newTicketSubject.trim().length <= 255
    );
  }

  clearForm() {
    this.newTicketSubject = '';
    this.newTicketDescription = '';
  }

  getStatusClass(status: string): string {
    return status.toLowerCase().replace('_', '');
  }

  logout() {
    this.mockAuthService.logout();
    console.log('👋 Déconnexion réussie');
  }
}




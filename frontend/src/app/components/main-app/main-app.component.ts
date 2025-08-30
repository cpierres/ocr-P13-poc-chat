import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';

import { MockLoginComponent } from '../mock-login/mock-login.component';
import { ChatInitComponent } from '../chat-init/chat-init.component';
import { ChatWindowComponent } from '../chat-window/chat-window.component';

import { MockAuthService } from '../../services/mock-auth.service';
import { MockUser } from '../../models/user.model';
import { SupportTicket } from '../../models/ticket.model';

// États de l'application
enum AppState {
  LOGIN = 'login',
  TICKET_MANAGEMENT = 'ticket_management',
  CHAT = 'chat'
}

@Component({
  selector: 'app-main-app',
  standalone: true,
  imports: [
    CommonModule,
    MockLoginComponent,
    ChatInitComponent,
    ChatWindowComponent
  ],
  template: `
    <div class="app-container">
      @switch (currentState()) {
        @case ('login') {
          <app-mock-login></app-mock-login>
        }
        @case ('ticket_management') {
          <app-chat-init (chatStarted)="onChatStarted($event)"></app-chat-init>
        }
        @case ('chat') {
          <app-chat-window (backToTicketsClicked)="onBackToTickets()"></app-chat-window>
        }
      }

      <!-- Debug info -->
      <!--
      @if (showDebug()) {
        <div class="debug-info">
          <p><strong>État:</strong> {{ currentState() }}</p>
          <p><strong>Utilisateur:</strong> {{ currentUser()?.firstName }} {{ currentUser()?.lastName }} ({{ currentUser()?.role }})</p>
          @if (currentTicket()) {
            <p><strong>Ticket:</strong> {{ currentTicket()?.subject }}</p>
          }
          <button (click)="toggleDebug()">Masquer debug</button>
        </div>
      } @else {
        <button class="debug-toggle" (click)="toggleDebug()">Debug</button>
      }
          -->
    </div>
  `,
  styles: [`
    .app-container {
      width: 100%;
      height: 100vh;
      position: relative;
      overflow: hidden;
    }

    .debug-info {
      position: fixed;
      top: 10px;
      right: 10px;
      background: rgba(0,0,0,0.8);
      color: white;
      padding: 12px;
      border-radius: 8px;
      font-size: 0.8em;
      z-index: 9999;
      min-width: 200px;
    }

    .debug-info p {
      margin: 4px 0;
    }

    .debug-info button {
      margin-top: 8px;
      background: #fff;
      color: #000;
      border: none;
      padding: 4px 8px;
      border-radius: 4px;
      cursor: pointer;
      font-size: 0.8em;
    }

    .debug-toggle {
      position: fixed;
      top: 10px;
      right: 10px;
      background: rgba(0,0,0,0.6);
      color: white;
      border: none;
      padding: 8px 12px;
      border-radius: 20px;
      cursor: pointer;
      font-size: 0.8em;
      z-index: 9999;
    }

    .debug-toggle:hover {
      background: rgba(0,0,0,0.8);
    }
  `]
})
export class MainAppComponent implements OnInit {
  currentState = signal<AppState>(AppState.LOGIN);
  currentUser = signal<MockUser | null>(null);
  currentTicket = signal<SupportTicket | null>(null);
  showDebug = signal<boolean>(false);

  constructor(private mockAuthService: MockAuthService) {}

  ngOnInit() {
    // Surveiller les changements d'utilisateur pour gérer les transitions d'état
    this.mockAuthService.getCurrentUser().subscribe(user => {
      this.currentUser.set(user);
      this.handleUserChange(user);
    });
  }

  private handleUserChange(user: MockUser | null) {
    if (!user) {
      // Pas d'utilisateur connecté → état LOGIN
      this.currentState.set(AppState.LOGIN);
      this.currentTicket.set(null);
      console.log('🔄 État changé vers: LOGIN');
    } else {
      // Utilisateur connecté → état TICKET_MANAGEMENT (sauf si déjà en chat)
      if (this.currentState() === AppState.LOGIN) {
        this.currentState.set(AppState.TICKET_MANAGEMENT);
        console.log('🔄 État changé vers: TICKET_MANAGEMENT');
      }
    }
  }

  onChatStarted(ticket: SupportTicket) {
    console.log('🚀 Chat démarré avec le ticket:', ticket.subject);
    this.currentTicket.set(ticket);
    this.currentState.set(AppState.CHAT);
    console.log('🔄 État changé vers: CHAT');
  }

  onBackToTickets() {
    console.log('🔙 Retour à la gestion des tickets');
    this.currentTicket.set(null);
    this.currentState.set(AppState.TICKET_MANAGEMENT);
    console.log('🔄 État changé vers: TICKET_MANAGEMENT');
  }

  toggleDebug() {
    this.showDebug.set(!this.showDebug());
  }
}

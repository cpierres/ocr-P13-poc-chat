import { Component, OnInit, OnDestroy, signal, ViewChild, ElementRef, AfterViewChecked, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatBadgeModule } from '@angular/material/badge';
import { MatTooltipModule } from '@angular/material/tooltip';

import { ChatService } from '../../services/chat.service';
import { MockAuthService } from '../../services/mock-auth.service';
import { WebSocketService } from '../../services/websocket.service';
import { ChatMessage } from '../../models/message.model';
import { SupportTicket } from '../../models/ticket.model';
import { MockUser } from '../../models/user.model';

@Component({
  selector: 'app-chat-window',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatCardModule,
    MatInputModule,
    MatButtonModule,
    MatIconModule,
    MatToolbarModule,
    MatBadgeModule,
    MatTooltipModule
  ],
  template: `
    <div class="chat-container">
      <!-- Header du chat -->
      <mat-toolbar class="chat-header">
        <button mat-icon-button (click)="backToTickets()" matTooltip="Retour aux tickets">
          <mat-icon>arrow_back</mat-icon>
        </button>

        <div class="ticket-info">
          <span class="ticket-title">{{ currentTicket()?.subject }}</span>
          <span class="ticket-subtitle">
            <!-- Ticket #{{ currentTicket()?.id?.substring(0, 8) }}... -->
           Ticket #{{ currentTicket()?.id?.slice(0, 8) }}...
            @if (currentTicket()?.assignedAgent) {
              • Agent: {{ currentTicket()?.assignedAgent }}
            }
          </span>
        </div>

      </mat-toolbar>

      <!-- Zone des messages -->
      <div class="messages-container" #messagesContainer>
        <div class="messages-content">
          @if (messages().length === 0) {
            <div class="no-messages">
              <p>Aucun message pour le moment</p>
              <p class="hint">Commencez la conversation en tapant un message ci-dessous</p>
            </div>
          } @else {
            @for (message of messages(); track message.id || $index) {
              <div class="message"
                   [class.own-message]="message.senderId === currentUser()?.id"
                   [class.system-message]="!message.senderId">

                @if (!message.senderId) {
                  <!-- Message système -->
                  <div class="system-content">
                    <mat-icon>info</mat-icon>
                    <span>{{ message.content }}</span>
                  </div>
                } @else {
                  <!-- Message utilisateur -->
                  <div class="message-bubble">
                    <div class="message-content">
                      {{ message.content }}
                    </div>
                    <div class="message-meta">
                      <span class="message-sender">
                        {{ message.senderId === currentUser()?.id ? 'Vous' : 'Agent' }}
                      </span>
                      <span class="message-time">
                        {{ message.timestamp | date:'HH:mm' }}
                      </span>
                    </div>
                  </div>
                }
              </div>
            }
          }

          <!-- Indicateur de frappe -->
          @if (typingIndicator()) {
            <div class="typing-indicator">
              <div class="typing-dots">
                <span></span>
                <span></span>
                <span></span>
              </div>
              <span class="typing-text">{{ typingIndicator() }}</span>
            </div>
          }
        </div>
      </div>

      <!-- Zone de saisie -->
      <div class="input-container">
        <mat-card class="input-card">
          <div class="input-area">
            <mat-form-field appearance="outline" class="message-input">
              <mat-label>Tapez votre message...</mat-label>
              <textarea matInput
                       [(ngModel)]="newMessage"
                       (keydown)="onKeyDown($event)"
                       [disabled]="!isConnected()"
                       placeholder="Écrivez votre message ici..."
                       rows="1"
                       maxlength="1000"
                       #messageTextarea></textarea>
              <mat-hint>{{ newMessage.length || 0 }}/1000 • Entrée pour envoyer, Shift+Entrée pour nouvelle ligne</mat-hint>
            </mat-form-field>

            <button mat-icon-button
                    color="primary"
                    (click)="sendMessage()"
                    [disabled]="!canSendMessage()"
                    class="send-button"
                    matTooltip="Envoyer le message">
              <mat-icon>send</mat-icon>
            </button>
          </div>
        </mat-card>
      </div>
    </div>
  `,
  styles: [`
    .chat-container {
      display: flex;
      flex-direction: column;
      height: 100vh;
      background: #f5f5f5;
    }

    .chat-header {
      background: dodgerblue ;
      color: white;
      min-height: 64px;
      padding: 0 16px;
      box-shadow: 0 2px 8px rgba(0,0,0,0.15);
      z-index: 10;
    }

    .ticket-info {
      flex: 1;
      margin-left: 16px;
      display: flex;
      flex-direction: column;
    }

    .ticket-title {
      font-weight: 500;
      font-size: 1.1em;
      line-height: 1.2;
    }

    .ticket-subtitle {
      font-size: 0.85em;
      opacity: 0.9;
      margin-top: 2px;
    }

    .status-connected {
      color: #4caf50;
    }

    .status-disconnected {
      color: #f44336;
    }

    .messages-container {
      flex: 1;
      overflow-y: auto;
      background: #fafafa;
      position: relative;
    }

    .messages-content {
      padding: 20px;
      min-height: 100%;
      display: flex;
      flex-direction: column;
    }

    .no-messages {
      text-align: center;
      padding: 60px 20px;
      color: #999;
      flex: 1;
      display: flex;
      flex-direction: column;
      justify-content: center;
      align-items: center;
    }

    .no-messages mat-icon {
      font-size: 64px;
      width: 64px;
      height: 64px;
      color: #ddd;
      margin-bottom: 16px;
    }

    .no-messages .hint {
      font-style: italic;
      color: #666;
      margin-top: 8px;
    }

    .message {
      display: flex;
      margin-bottom: 16px;
      animation: messageIn 0.3s ease-out;
    }

    .message.own-message {
      flex-direction: row-reverse;
    }

    .message.system-message {
      justify-content: center;
      margin: 12px 0;
    }

    .system-content {
      background: #e0e0e0;
      color: #666;
      padding: 8px 16px;
      border-radius: 16px;
      display: flex;
      align-items: center;
      gap: 8px;
      font-size: 0.9em;
    }

    .message-avatar {
      width: 40px;
      height: 40px;
      display: flex;
      align-items: flex-end;
      justify-content: center;
      margin: 0 8px;
    }

    .message-bubble {
      max-width: 70%;
      min-width: 120px;
    }

    .message-content {
      background: white;
      padding: 12px 16px;
      border-radius: 18px;
      box-shadow: 0 1px 2px rgba(0,0,0,0.1);
      word-wrap: break-word;
      white-space: pre-wrap;
      line-height: 1.4;
    }

    .own-message .message-content {
      background: #1976d2;
      color: white;
    }

    .message-meta {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-top: 4px;
      font-size: 0.75em;
      color: #999;
      padding: 0 4px;
    }

    .own-message .message-meta {
      color: #bbdefb;
    }

    .typing-indicator {
      display: flex;
      align-items: center;
      gap: 12px;
      padding: 8px 16px;
      margin-bottom: 16px;
    }

    .typing-dots {
      display: flex;
      gap: 4px;
    }

    .typing-dots span {
      width: 8px;
      height: 8px;
      border-radius: 50%;
      background: #999;
      animation: typing 1.4s infinite ease-in-out;
    }

    .typing-dots span:nth-child(1) { animation-delay: -0.32s; }
    .typing-dots span:nth-child(2) { animation-delay: -0.16s; }

    .typing-text {
      color: #666;
      font-style: italic;
      font-size: 0.9em;
    }

    .input-container {
      padding: 16px;
      background: white;
      border-top: 1px solid #e0e0e0;
    }

    .input-card {
      box-shadow: 0 2px 8px rgba(0,0,0,0.1);
    }

    .input-area {
      display: flex;
      align-items: flex-end;
      gap: 12px;
      padding: 16px;
    }

    .message-input {
      flex: 1;
    }

    .send-button {
      flex-shrink: 0;
      margin-bottom: 26px;
    }

    .send-button[disabled] {
      background: #e0e0e0 !important;
      color: #999 !important;
    }

    /* Animations */
    @keyframes messageIn {
      from {
        opacity: 0;
        transform: translateY(20px);
      }
      to {
        opacity: 1;
        transform: translateY(0);
      }
    }

    @keyframes typing {
      0%, 60%, 100% {
        transform: translateY(0);
      }
      30% {
        transform: translateY(-10px);
      }
    }

    /* Responsive */
    @media (max-width: 768px) {
      .ticket-info {
        margin-left: 8px;
      }

      .ticket-title {
        font-size: 1em;
      }

      .ticket-subtitle {
        font-size: 0.8em;
      }

      .message-bubble {
        max-width: 85%;
      }

      .messages-content {
        padding: 16px 12px;
      }

      .input-container {
        padding: 12px;
      }

      .input-area {
        padding: 12px;
      }
    }

    /* Scroll personnalisé */
    .messages-container::-webkit-scrollbar {
      width: 6px;
    }

    .messages-container::-webkit-scrollbar-track {
      background: #f1f1f1;
    }

    .messages-container::-webkit-scrollbar-thumb {
      background: #c1c1c1;
      border-radius: 3px;
    }

    .messages-container::-webkit-scrollbar-thumb:hover {
      background: #a1a1a1;
    }
  `]
})
export class ChatWindowComponent implements OnInit, OnDestroy, AfterViewChecked {
  messages = signal<ChatMessage[]>([]);
  currentTicket = signal<SupportTicket | null>(null);
  currentUser = signal<MockUser | null>(null);
  isConnected = signal<boolean>(false);
  typingIndicator = signal<string>('');

  newMessage = '';
  private shouldScrollToBottom = false;
  private typingTimeout: any;

  @ViewChild('messagesContainer') messagesContainer!: ElementRef;
  @ViewChild('messageTextarea') messageTextarea!: ElementRef;
  @Output() backToTicketsClicked = new EventEmitter<void>();

  constructor(
    private chatService: ChatService,
    private mockAuthService: MockAuthService,
    private websocketService: WebSocketService
  ) {}

  ngOnInit() {
    // S'abonner aux messages
    this.chatService.messages.subscribe(messages => {
      const previousCount = this.messages().length;
      this.messages.set(messages);

      // Scroll automatique si nouveau message
      if (messages.length > previousCount) {
        this.shouldScrollToBottom = true;
      }
    });

    // S'abonner au ticket actuel
    this.chatService.currentTicket.subscribe(ticket => {
      this.currentTicket.set(ticket);
    });

    // S'abonner à l'utilisateur actuel
    this.mockAuthService.getCurrentUser().subscribe(user => {
      this.currentUser.set(user);
    });

    // S'abonner à l'état de connexion WebSocket
    this.websocketService.isConnected$.subscribe(connected => {
      this.isConnected.set(connected);
    });

    // Focus automatique sur le champ de saisie
    setTimeout(() => {
      if (this.messageTextarea) {
        this.messageTextarea.nativeElement.focus();
      }
    }, 100);
  }

  ngAfterViewChecked() {
    if (this.shouldScrollToBottom) {
      this.scrollToBottom();
      this.shouldScrollToBottom = false;
    }
  }

  ngOnDestroy() {
    if (this.typingTimeout) {
      clearTimeout(this.typingTimeout);
    }
    this.chatService.disconnect();
  }

  onKeyDown(event: KeyboardEvent) {
    if (event.key === 'Enter' && !event.shiftKey) {
      event.preventDefault();
      this.sendMessage();
    }
  }

  sendMessage() {
    if (!this.canSendMessage()) {
      return;
    }

    try {
      const messageContent = this.newMessage.trim();
      this.chatService.sendMessage(messageContent);
      this.newMessage = '';

      // Focus sur le champ de saisie
      if (this.messageTextarea) {
        this.messageTextarea.nativeElement.focus();
      }

      console.log('📤 Message envoyé:', messageContent);
    } catch (error) {
      console.error('❌ Erreur envoi message:', error);
      alert('Erreur lors de l\'envoi du message: ' + (error as Error).message);
    }
  }

  canSendMessage(): boolean {
    return !!(
      this.isConnected() &&
      this.newMessage.trim().length > 0 &&
      this.currentTicket() &&
      this.currentUser()
    );
  }

  backToTickets() {
    this.backToTicketsClicked.emit();
  }

  private scrollToBottom(): void {
    try {
      const container = this.messagesContainer?.nativeElement;
      if (container) {
        container.scrollTop = container.scrollHeight;
      }
    } catch (error) {
      console.warn('Erreur scroll automatique:', error);
    }
  }
}

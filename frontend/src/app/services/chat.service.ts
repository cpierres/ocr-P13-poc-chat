import { Injectable } from '@angular/core';
import { Observable, BehaviorSubject, firstValueFrom } from 'rxjs';
import { ChatMessage } from '../models/message.model';
import { SupportTicket } from '../models/ticket.model';
import { MockUser } from '../models/user.model';
import { WebSocketService } from './websocket.service';
import { TicketService } from './ticket.service';
import { MockAuthService } from './mock-auth.service';

@Injectable({
  providedIn: 'root'
})
export class ChatService {
  private currentTicket$ = new BehaviorSubject<SupportTicket | null>(null);
  private messages$ = new BehaviorSubject<ChatMessage[]>([]);

  constructor(
    private websocketService: WebSocketService,
    private ticketService: TicketService,
    private mockAuthService: MockAuthService
  ) {}

  async createNewTicket(subject: string, description: string): Promise<SupportTicket> {
    const currentUser = await firstValueFrom(this.mockAuthService.getCurrentUser());
    if (!currentUser) {
      throw new Error('Aucun utilisateur connecté');
    }

    try {
      const ticket = await firstValueFrom(
        this.ticketService.createTicket(subject, description, currentUser.id)
      );

      console.log('Nouveau ticket créé:', ticket);
      return ticket;
    } catch (error) {
      console.error('Erreur création ticket:', error);
      throw error;
    }
  }

  async joinTicket(ticket: SupportTicket): Promise<void> {
    try {
      console.log('🔗 Connexion au ticket:', ticket.subject);

      // Connecter WebSocket
      await this.websocketService.connect();

      this.currentTicket$.next(ticket);

      // S'abonner aux messages du ticket
      this.websocketService.subscribeToTicket(ticket.id).subscribe({
        next: (message) => {
          const currentMessages = this.messages$.value;
          // Éviter les doublons
          if (!currentMessages.find(m => m.id === message.id)) {
            this.messages$.next([...currentMessages, message]);
          }
        },
        error: (error) => console.error('❌ Erreur abonnement messages:', error)
      });

      // Rejoindre le ticket via WebSocket
      this.websocketService.joinTicket(ticket.id);

      // Charger l'historique des messages
      await this.loadMessageHistory(ticket.id);

      console.log('Connecté au ticket:', ticket.subject);
    } catch (error) {
      console.error('Erreur connexion ticket:', error);
      throw error;
    }
  }

  private async loadMessageHistory(ticketId: string): Promise<void> {
    try {
      const messages = await firstValueFrom(this.ticketService.getMessagesByTicket(ticketId));
      // Convertir les timestamps en Date objects
      const processedMessages = messages.map(msg => ({
        ...msg,
        timestamp: new Date(msg.timestamp)
      }));
      this.messages$.next(processedMessages);
      console.log('Historique chargé:', messages.length, 'messages');
    } catch (error) {
      console.error('Erreur chargement historique:', error);
      this.messages$.next([]);
    }
  }

  sendMessage(content: string): void {
    const currentUser$ = this.mockAuthService.getCurrentUser();
    const currentTicket = this.currentTicket$.value;

    firstValueFrom(currentUser$).then(currentUser => {
      if (!currentUser || !currentTicket) {
        throw new Error('Utilisateur ou ticket non disponible');
      }

      const message: ChatMessage = {
        ticketId: currentTicket.id,
        senderId: currentUser.id,
        content,
        timestamp: new Date()
      };

      this.websocketService.sendMessage(message);
      console.log('Message envoyé:', content);
    }).catch(error => {
      console.error('Erreur envoi message:', error);
    });
  }

  get currentTicket(): Observable<SupportTicket | null> {
    return this.currentTicket$.asObservable();
  }

  get messages(): Observable<ChatMessage[]> {
    return this.messages$.asObservable();
  }

  disconnect() {
    this.websocketService.disconnect();
    this.currentTicket$.next(null);
    this.messages$.next([]);
  }
}

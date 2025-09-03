import { Injectable } from '@angular/core';
import { Client, StompConfig } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import { BehaviorSubject, Observable } from 'rxjs';
import { ChatMessage } from '../models/message.model';

@Injectable({
  providedIn: 'root'
})
export class WebSocketService {
  private client!: Client;
  private connected$ = new BehaviorSubject<boolean>(false);

  constructor() {
    this.initializeWebSocketConnection();
  }

  private initializeWebSocketConnection() {
    const config: StompConfig = {
      webSocketFactory: () => new SockJS('/ws'),
      connectHeaders: {},
      debug: (str) => console.log('🔌 WebSocket:', str),
      reconnectDelay: 5000,
      heartbeatIncoming: 4000,
      heartbeatOutgoing: 4000,
      onConnect: () => {
        this.connected$.next(true);
        console.log('✅ WebSocket connecté');
      },
      onDisconnect: () => {
        this.connected$.next(false);
        console.log('❌ WebSocket déconnecté');
      },
      onStompError: (error) => {
        console.error('❌ Erreur STOMP:', error);
        this.connected$.next(false);
      }
    };

    this.client = new Client(config);
  }

  connect(): Promise<void> {
    return new Promise((resolve, reject) => {
      if (this.connected$.value) {
        resolve();
        return;
      }

      this.client.onConnect = () => {
        this.connected$.next(true);
        resolve();
      };

      this.client.onStompError = (error) => {
        reject(error);
      };

      this.client.activate();
    });
  }

  disconnect() {
    if (this.client) {
      this.client.deactivate();
    }
  }

  subscribeToTicket(ticketId: string): Observable<ChatMessage> {
    return new Observable(observer => {
      if (!this.connected$.value) {
        observer.error('WebSocket non connecté');
        return;
      }

      const subscription = this.client.subscribe(
        `/topic/messages/${ticketId}`,
        (message) => {
          try {
            const chatMessage = JSON.parse(message.body) as ChatMessage;
            chatMessage.timestamp = new Date(chatMessage.timestamp);
            observer.next(chatMessage);
          } catch (error) {
            console.error('Erreur parsing message WebSocket:', error);
          }
        }
      );

      return () => subscription.unsubscribe();
    });
  }

  sendMessage(message: ChatMessage) {
    if (!this.connected$.value) {
      throw new Error('WebSocket non connecté');
    }

    this.client.publish({
      destination: '/app/chat.send',
      body: JSON.stringify(message)
    });
  }

  joinTicket(ticketId: string) {
    if (!this.connected$.value) {
      throw new Error('WebSocket non connecté');
    }

    this.client.publish({
      destination: '/app/chat.join',
      body: JSON.stringify({ ticketId })
    });
  }

  get isConnected$(): Observable<boolean> {
    return this.connected$.asObservable();
  }
}

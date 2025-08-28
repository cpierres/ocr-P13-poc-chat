import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { SupportTicket } from '../models/ticket.model';

@Injectable({
  providedIn: 'root'
})
export class TicketService {
  private apiUrl = 'http://localhost:8083/api';

  constructor(private http: HttpClient) {}

  createTicket(subject: string, description: string, userId: string): Observable<SupportTicket> {
    const ticket = {
      userId,
      subject,
      description
    };

    return this.http.post<SupportTicket>(`${this.apiUrl}/tickets`, ticket);
  }

  getTicketById(ticketId: string): Observable<SupportTicket> {
    return this.http.get<SupportTicket>(`${this.apiUrl}/tickets/${ticketId}`);
  }

  getTicketsByUserId(userId: string): Observable<SupportTicket[]> {
    return this.http.get<SupportTicket[]>(`${this.apiUrl}/tickets/user/${userId}`);
  }

  getAllTickets(): Observable<SupportTicket[]> {
    return this.http.get<SupportTicket[]>(`${this.apiUrl}/tickets`);
  }

  getMessagesByTicket(ticketId: string): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/messages/ticket/${ticketId}`);
  }
}

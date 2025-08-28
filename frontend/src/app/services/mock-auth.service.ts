import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, BehaviorSubject } from 'rxjs';
import { tap } from 'rxjs/operators';
import { MockUser, LoginResponse } from '../models/user.model';

@Injectable({
  providedIn: 'root'
})
export class MockAuthService {
  private apiUrl = 'http://localhost:8083/api/mock';
  private currentUser$ = new BehaviorSubject<MockUser | null>(null);
  private currentToken$ = new BehaviorSubject<string | null>(null);

  constructor(private http: HttpClient) {
    this.initializeFromStorage();
  }

  loginAsClient(): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(`${this.apiUrl}/login/CLIENT`, {}).pipe(
      tap(response => this.handleLoginSuccess(response, 'Christophe', 'Pierrès'))
    );
  }

  loginAsAgent(): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(`${this.apiUrl}/login/AGENT`, {}).pipe(
      tap(response => this.handleLoginSuccess(response, 'Marie', 'Agent'))
    );
  }

  private handleLoginSuccess(response: LoginResponse, firstName: string, lastName: string) {
    const user: MockUser = {
      id: response.userId,
      email: response.email,
      firstName,
      lastName,
      role: response.role as 'CLIENT' | 'AGENT'
    };

    this.currentUser$.next(user);
    this.currentToken$.next(response.token);

    localStorage.setItem('mock-auth-token', response.token);
    localStorage.setItem('mock-user', JSON.stringify(user));
  }

  getCurrentUser(): Observable<MockUser | null> {
    return this.currentUser$.asObservable();
  }

  getCurrentToken(): string | null {
    return this.currentToken$.value;
  }

  logout() {
    this.currentUser$.next(null);
    this.currentToken$.next(null);
    localStorage.removeItem('mock-auth-token');
    localStorage.removeItem('mock-user');
  }

  private initializeFromStorage() {
    const token = localStorage.getItem('mock-auth-token');
    const userJson = localStorage.getItem('mock-user');

    if (token && userJson) {
      try {
        const user = JSON.parse(userJson) as MockUser;
        this.currentUser$.next(user);
        this.currentToken$.next(token);
      } catch (error) {
        console.error('Erreur parsing utilisateur stocké:', error);
        this.logout();
      }
    }
  }
}

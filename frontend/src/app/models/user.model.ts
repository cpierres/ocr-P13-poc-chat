export interface MockUser {
  id: string;
  email: string;
  firstName: string;
  lastName: string;
  role: 'CLIENT' | 'AGENT';
}

export interface LoginResponse {
  token: string;
  userId: string;
  email: string;
  role: string;
  message: string;
}

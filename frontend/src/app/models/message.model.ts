export interface ChatMessage {
  id?: string;
  ticketId: string;
  senderId: string;
  content: string;
  timestamp: Date;
}

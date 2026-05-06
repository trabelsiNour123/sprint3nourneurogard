export type PaymentStatus = 'PENDING' | 'SUCCESS' | 'FAILED' | 'REFUNDED';

export interface Payment {
  id: number;
  orderId: number;
  amount: number;
  status: PaymentStatus;
  paymentMethod: string;
  createdAt: string;
}

export interface CreatePaymentRequest {
  orderId: number;
  amount: number;
  paymentMethod: string;
}

export interface UpdatePaymentStatusRequest {
  status: PaymentStatus;
}

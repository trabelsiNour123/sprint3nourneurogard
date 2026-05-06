import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import type {
  CreatePaymentRequest,
  Payment,
  UpdatePaymentStatusRequest
} from '../models/payment.model';

@Injectable({
  providedIn: 'root'
})
export class PaymentService {
  private baseUrl = `${environment.paymentApiUrl ?? environment.apiUrl}/payments`;

  constructor(private http: HttpClient) {}

  create(request: CreatePaymentRequest): Observable<Payment> {
    return this.http.post<Payment>(this.baseUrl, request);
  }

  getById(id: number): Observable<Payment> {
    return this.http.get<Payment>(`${this.baseUrl}/${id}`);
  }

  getByOrderId(orderId: number): Observable<Payment[]> {
    const params = new HttpParams().set('orderId', String(orderId));
    return this.http.get<Payment[]>(this.baseUrl, { params });
  }

  updateStatus(id: number, request: UpdatePaymentStatusRequest): Observable<Payment> {
    return this.http.put<Payment>(`${this.baseUrl}/${id}/status`, request);
  }
}

import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { finalize } from 'rxjs/operators';
import { parseHttpError } from '../../../core/utils/http-error.util';
import { PaymentService } from '../../../core/services/payment.service';
import type {
  CreatePaymentRequest,
  Payment,
  PaymentStatus
} from '../../../core/models/payment.model';

type PaymentMethod = 'CARD' | 'PAYPAL';

@Component({
  selector: 'app-payment-management',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './payment-management.component.html',
  styleUrls: ['./payment-management.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class PaymentManagementComponent {
  selectedMethod: PaymentMethod = 'CARD';
  lookupOrderId: number | null = null;
  orderId: number | null = null;
  amount: number | null = null;
  cardNumber = '';
  expiryDate = '';
  securityCode = '';
  fullName = '';
  country = 'Tunisie';
  addressLine1 = '';

  isSubmitting = false;
  isLoadingPayments = false;
  successMessage = '';
  errorMessage = '';

  payments: Payment[] = [];
  readonly statusOptions: PaymentStatus[] = ['PENDING', 'SUCCESS', 'FAILED', 'REFUNDED'];

  constructor(
    private paymentService: PaymentService,
    private cdr: ChangeDetectorRef
  ) {}

  selectMethod(method: PaymentMethod): void {
    this.selectedMethod = method;
    this.cdr.markForCheck();
  }

  submitPayment(): void {
    this.clearMessages();

    if (!this.orderId || this.orderId <= 0) {
      this.errorMessage = 'Order ID is required.';
      this.cdr.markForCheck();
      return;
    }
    if (this.amount == null || !Number.isFinite(this.amount) || this.amount <= 0) {
      this.errorMessage = 'Amount must be greater than 0.';
      this.cdr.markForCheck();
      return;
    }
    if (!this.fullName.trim()) {
      this.errorMessage = 'Full name is required.';
      this.cdr.markForCheck();
      return;
    }
    if (!this.addressLine1.trim()) {
      this.errorMessage = 'Billing address is required.';
      this.cdr.markForCheck();
      return;
    }
    if (this.selectedMethod === 'CARD') {
      if (!this.cardNumber.trim() || !this.expiryDate.trim() || !this.securityCode.trim()) {
        this.errorMessage = 'Card information is required.';
        this.cdr.markForCheck();
        return;
      }
    }

    const payload: CreatePaymentRequest = {
      orderId: this.orderId,
      amount: Number(this.amount),
      paymentMethod: this.selectedMethod === 'CARD' ? 'Carte bancaire' : 'PayPal'
    };

    this.isSubmitting = true;
    this.cdr.markForCheck();

    this.paymentService
      .create(payload)
      .pipe(
        finalize(() => {
          this.isSubmitting = false;
          this.cdr.markForCheck();
        })
      )
      .subscribe({
        next: (payment) => {
          this.successMessage = `Payment #${payment.id} created successfully.`;
          this.lookupOrderId = payment.orderId;
          this.resetSensitiveFields();
          this.loadPayments();
        },
        error: (err: unknown) => {
          this.errorMessage = parseHttpError(err, 'Failed to create payment.');
        }
      });
  }

  loadPayments(): void {
    if (!this.lookupOrderId || this.lookupOrderId <= 0) {
      this.payments = [];
      this.cdr.markForCheck();
      return;
    }

    this.isLoadingPayments = true;
    this.clearMessages();
    this.cdr.markForCheck();

    this.paymentService
      .getByOrderId(this.lookupOrderId)
      .pipe(
        finalize(() => {
          this.isLoadingPayments = false;
          this.cdr.markForCheck();
        })
      )
      .subscribe({
        next: (payments) => {
          this.payments = payments ?? [];
        },
        error: (err: unknown) => {
          this.errorMessage = parseHttpError(err, 'Failed to load payments.');
          this.payments = [];
        }
      });
  }

  setStatus(payment: Payment, status: PaymentStatus): void {
    if (payment.status === status) {
      return;
    }

    this.clearMessages();
    this.paymentService.updateStatus(payment.id, { status }).subscribe({
      next: (updated) => {
        this.successMessage = `Payment #${updated.id} updated to ${updated.status}.`;
        this.payments = this.payments.map((item) => (item.id === updated.id ? updated : item));
        this.cdr.markForCheck();
      },
      error: (err: unknown) => {
        this.errorMessage = parseHttpError(err, 'Failed to update payment status.');
        this.cdr.markForCheck();
      }
    });
  }

  trackByPaymentId(_: number, payment: Payment): number {
    return payment.id;
  }

  private clearMessages(): void {
    this.successMessage = '';
    this.errorMessage = '';
  }

  private resetSensitiveFields(): void {
    this.cardNumber = '';
    this.expiryDate = '';
    this.securityCode = '';
  }
}

import { Injectable } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Observable, throwError, Subject } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { Client, Message } from '@stomp/stompjs';
import { environment } from '../../../environments/environment';
import { AlertResponse, AlertRequest } from '../../core/models/alert.model';
import { AuthService } from './auth.service';
import { NotificationService, NotificationAction } from './notification.service';

@Injectable({
  providedIn: 'root'
})
export class AlertService {
  private apiUrl = environment.apiUrl; // points to gateway
  private stompClient: Client | null = null;
  private patientAlertSubject = new Subject<AlertResponse>();
  private providerAlertSubject = new Subject<AlertResponse>();

  constructor(
    private http: HttpClient,
    private authService: AuthService,
    private notificationService: NotificationService
  ) {
    // Listen for authentication state changes to manage WebSocket lifecycle
    this.authService.isLoggedIn$.subscribe((isLoggedIn) => {
      if (isLoggedIn) {
        // Only initialize WebSocket when user is actually logged in
        if (!this.stompClient || !this.stompClient.active) {
          this.initializeWebSocketConnection();
        }
      } else {
        // Disconnect WebSocket when user logs out
        this.disconnectWebSocket();
      }
    });
  }

  private initializeWebSocketConnection(): void {
    // Get token at initialization time, not at constructor time
    const token = this.authService.getToken();

    // Do not proceed if token is invalid
    if (!token) {
      console.warn('[AlertService] Cannot initialize WebSocket: no valid token available');
      return;
    }

    const wsUrl = this.apiUrl.replace('http', 'ws').replace('https', 'wss') + '/ws/alerts?token=' + token;
    this.stompClient = new Client({
      brokerURL: wsUrl,
      reconnectDelay: 5000,
      heartbeatIncoming: 4000,
      heartbeatOutgoing: 4000,
      debug: (str) => {
        // console.log('[STOMP]', str);
      }
    });

    this.stompClient.onConnect = (frame) => {
      console.log('[AlertService] Connected to STOMP over WebSocket');
      console.log('[AlertService] Current user state at connect:', this.authService.currentUser);
      
      const role = this.authService.currentUser?.role?.toUpperCase() || '';
      const userId = this.authService.currentUser?.userId;

      if (role.includes('PATIENT') && userId) {
        console.log(`[AlertService] Subscribing patient ${userId} to /topic/alerts/patient/${userId}`);
        this.stompClient?.subscribe('/topic/alerts/patient/' + userId, (message: Message) => {
          if (message.body) {
            const event = JSON.parse(message.body);
            const action: NotificationAction = event.action;
            const parsed: AlertResponse = event.data;
            console.log('[AlertService] STOMP event for PATIENT:', action, parsed);
            this.patientAlertSubject.next(parsed);
            this.notificationService.addNotification(
              this.notificationService.buildAlertNotification(action, parsed)
            );
            this.showGlobalNotification(action, parsed);
          }
        });
      } else if (role.includes('PROVIDER')) {
        console.log('[AlertService] Subscribing provider to /topic/alerts/provider');
        this.stompClient?.subscribe('/topic/alerts/provider', (message: Message) => {
          if (message.body) {
            const event = JSON.parse(message.body);
            const action: NotificationAction = event.action;
            const parsed: AlertResponse = event.data;
            console.log('[AlertService] STOMP event for PROVIDER:', action, parsed);
            this.providerAlertSubject.next(parsed);
            // Provider gets dropdown + toast ONLY for RESOLVE events
            if (action === 'RESOLVE') {
              this.notificationService.addNotification(
                this.notificationService.buildAlertNotification(action, parsed)
              );
              this.showGlobalNotification(action, parsed);
            }
          }
        });
      } else if (role.includes('CAREGIVER') && userId) {
        console.log('[AlertService] CAREGIVER connected. Flushing pending patient subscriptions:', this.pendingPatientSubscriptions);
      } else {
        console.warn('[AlertService] Unrecognized role or missing userId. Role:', role, 'userId:', userId);
      }

      // Flush any queued per-patient subscriptions (used by CAREGIVER)
      while (this.pendingPatientSubscriptions.length > 0) {
        const pid = this.pendingPatientSubscriptions.shift()!;
        console.log(`[AlertService] Flushing pending subscription for patient ${pid}`);
        this.stompClient?.subscribe('/topic/alerts/patient/' + pid, (message: Message) => {
          if (message.body) {
            const event = JSON.parse(message.body);
            const action: NotificationAction = event.action;
            const parsed: AlertResponse = event.data;
            console.log(`[AlertService] STOMP event for CAREGIVER (patient ${pid}):`, action, parsed);
            this.patientAlertSubject.next(parsed);
            this.notificationService.addNotification(
              this.notificationService.buildAlertNotification(action, parsed)
            );
            this.showGlobalNotification(action, parsed);
          }
        });
      }
    };


    this.stompClient.onStompError = (frame) => {
      console.error('[AlertService] STOMP Error', frame.headers['message']);
      console.error('Additional details: ' + frame.body);
    };

    this.stompClient.activate();
  }

  getPatientAlertStream(): Observable<AlertResponse> {
    return this.patientAlertSubject.asObservable();
  }

  getProviderAlertStream(): Observable<AlertResponse> {
    return this.providerAlertSubject.asObservable();
  }

  private disconnectWebSocket(): void {
    if (this.stompClient && this.stompClient.active) {
      console.log('[AlertService] Disconnecting WebSocket due to logout');
      this.stompClient.deactivate().then(() => {
        console.log('[AlertService] WebSocket disconnected successfully');
      }).catch((error) => {
        console.error('[AlertService] Error disconnecting WebSocket:', error);
      });
      this.stompClient = null;
    }
    // Clear pending subscriptions when disconnecting
    this.pendingPatientSubscriptions = [];
    this.subscribedPatientIds.clear();
  }

  // Queue for patient IDs to subscribe when STOMP connects
  private pendingPatientSubscriptions: number[] = [];
  private subscribedPatientIds = new Set<number>();

  subscribeToPatientAlerts(patientId: number): void {
    // Avoid duplicate subscriptions
    if (this.subscribedPatientIds.has(patientId)) return;
    this.subscribedPatientIds.add(patientId);

    const doSubscribe = () => {
      this.stompClient?.subscribe('/topic/alerts/patient/' + patientId, (message: Message) => {
        if (message.body) {
          const event = JSON.parse(message.body);
          const action: NotificationAction = event.action;
          const parsed: AlertResponse = event.data;
          console.log(`[AlertService] STOMP event for CAREGIVER (patient ${patientId}):`, action, parsed);
          this.patientAlertSubject.next(parsed);
          this.notificationService.addNotification(
            this.notificationService.buildAlertNotification(action, parsed)
          );
          this.showGlobalNotification(action, parsed);
        }
      });
    };

    if (this.stompClient && this.stompClient.connected) {
      doSubscribe();
    } else {
      // Queue up — the main onConnect handler will flush this
      this.pendingPatientSubscriptions.push(patientId);
    }
  }


  private handleError(error: HttpErrorResponse) {
    let errorMessage = 'An error occurred';
    if (error.error instanceof ErrorEvent) {
      errorMessage = `Client Error: ${error.error.message}`;
    } else {
      errorMessage = `Error Code: ${error.status}\nMessage: ${error.message}`;
    }
    console.error('[AlertService]', errorMessage);
    return throwError(() => new Error(errorMessage));
  }

  private showGlobalNotification(action: NotificationAction, alert: AlertResponse) {
    // 1. Create or find the global container so multiple toasts stack properly
    let container = document.getElementById('neuroguard-toast-container');
    if (!container) {
      container = document.createElement('div');
      container.id = 'neuroguard-toast-container';
      container.style.position = 'fixed';
      container.style.bottom = '30px';
      container.style.right = '30px';
      container.style.display = 'flex';
      container.style.flexDirection = 'column';
      container.style.gap = '12px';
      container.style.zIndex = '999999';
      container.style.alignItems = 'flex-end';
      container.style.pointerEvents = 'none';
      document.body.appendChild(container);
    }

    const toast = document.createElement('div');
    toast.style.pointerEvents = 'auto';
    
    // Pick color based on action + severity
    const actionColors: Record<string, string> = {
      CREATE: alert.severity === 'CRITICAL' ? 'rgba(220, 53, 69, 0.95)' : alert.severity === 'WARNING' ? 'rgba(255, 193, 7, 0.95)' : 'rgba(23, 162, 184, 0.9)',
      UPDATE: 'rgba(13, 110, 253, 0.92)',
      DELETE: 'rgba(108, 117, 125, 0.92)',
      RESOLVE: 'rgba(25, 135, 84, 0.95)'
    };
    const actionIcons: Record<string, string> = {
      CREATE: `<svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"></path><line x1="12" y1="9" x2="12" y2="13"></line><line x1="12" y1="17" x2="12.01" y2="17"></line></svg>`,
      UPDATE: `<svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"></path><path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"></path></svg>`,
      DELETE: `<svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><polyline points="3 6 5 6 21 6"></polyline><path d="M19 6l-1 14a2 2 0 0 1-2 2H8a2 2 0 0 1-2-2L5 6"></path><path d="M10 11v6"></path><path d="M14 11v6"></path></svg>`,
      RESOLVE: `<svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"></path><polyline points="22 4 12 14.01 9 11.01"></polyline></svg>`
    };
    const actionTitles: Record<string, string> = {
      CREATE: 'New Alert',
      UPDATE: 'Alert Updated',
      DELETE: 'Alert Dismissed',
      RESOLVE: 'Alert Resolved'
    };

    const bgColor = actionColors[action] || 'rgba(23, 162, 184, 0.9)';
    const iconSvg = actionIcons[action] || actionIcons['CREATE'];
    const title = actionTitles[action] || 'Alert Event';
    const textColor = (action === 'CREATE' && alert.severity === 'WARNING') ? '#212529' : '#ffffff';
    const now = new Date();
    const timeString = now.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });

    toast.style.position = 'relative';
    toast.style.backgroundColor = bgColor;
    toast.style.color = textColor;
    toast.style.padding = '16px 20px';
    toast.style.borderRadius = '14px';
    toast.style.boxShadow = '0 10px 30px rgba(0,0,0,0.15), 0 4px 10px rgba(0,0,0,0.1)';
    toast.style.backdropFilter = 'blur(12px)';
    (toast.style as any).WebkitBackdropFilter = 'blur(12px)';
    toast.style.width = '340px';
    toast.style.fontFamily = '"Inter", "Segoe UI", Roboto, Helvetica, Arial, sans-serif';
    toast.style.display = 'flex';
    toast.style.alignItems = 'flex-start';
    toast.style.gap = '14px';
    toast.style.border = '1px solid rgba(255,255,255,0.15)';
    toast.style.opacity = '0';
    toast.style.transform = 'translateX(40px) scale(0.95)';
    toast.style.transition = 'all 0.4s cubic-bezier(0.175, 0.885, 0.32, 1.25)';

    toast.innerHTML = `
      <div style="flex-shrink: 0; display: flex; align-items: center; justify-content: center; width: 36px; height: 36px; background: rgba(255,255,255,0.25); border-radius: 50%;">
        ${iconSvg}
      </div>
      <div style="display: flex; flex-direction: column; gap: 4px; flex-grow: 1;">
        <div style="display: flex; justify-content: space-between; align-items: center; width: 100%;">
          <strong style="font-size: 15px; font-weight: 700; letter-spacing: 0.3px;">${title}</strong>
          <span style="font-size: 11.5px; opacity: 0.8; font-weight: 600;">${timeString}</span>
        </div>
        <span style="font-size: 13.5px; font-weight: 500; line-height: 1.4; opacity: 0.95;">
          ${alert.message.length > 90 ? alert.message.substring(0, 90) + '...' : alert.message}
        </span>
      </div>
      <button class="toast-close-btn" style="background: transparent; border: none; color: inherit; opacity: 0.6; cursor: pointer; padding: 0; margin-left: 4px; display: flex; align-items: center; justify-content: center; align-self: flex-start; height: 20px; transition: opacity 0.2s;">
        <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><line x1="18" y1="6" x2="6" y2="18"></line><line x1="6" y1="6" x2="18" y2="18"></line></svg>
      </button>
    `;

    const closeBtn = toast.querySelector('.toast-close-btn') as HTMLElement;
    if (closeBtn) {
      closeBtn.onmouseover = () => closeBtn.style.opacity = '1';
      closeBtn.onmouseout = () => closeBtn.style.opacity = '0.6';
      closeBtn.onclick = (e) => { e.stopPropagation(); dismissToast(); };
    }

    const dismissToast = () => {
      toast.style.opacity = '0';
      toast.style.transform = 'translateX(40px) scale(0.95)';
      setTimeout(() => { if (container && container.contains(toast)) container.removeChild(toast); }, 400);
    };

    container.appendChild(toast);
    setTimeout(() => { toast.style.opacity = '1'; toast.style.transform = 'translateX(0) scale(1)'; }, 10);
    setTimeout(() => { if (container && container.contains(toast)) dismissToast(); }, 6000);
  }

  // ================== PATIENT ENDPOINTS ==================
  getMyAlerts(): Observable<AlertResponse[]> {
    return this.http.get<AlertResponse[]>(`${this.apiUrl}/api/patient/alerts`)
      .pipe(catchError(err => this.handleError(err)));
  }

  // ================== CAREGIVER ENDPOINTS ==================
  getCaregiverAlerts(): Observable<AlertResponse[]> {
    return this.http.get<AlertResponse[]>(`${this.apiUrl}/api/caregiver/alerts`)
      .pipe(catchError(err => this.handleError(err)));
  }

  // ================== PROVIDER ENDPOINTS ==================
  // Trigger rule‑based alert generation for all patients
  triggerAlertGeneration(): Observable<string> {
    return this.http.post(`${this.apiUrl}/api/provider/alerts/generate`, {}, { responseType: 'text' })
      .pipe(catchError(err => this.handleError(err)));
  }

  // Trigger ML‑based predictive alert generation for all patients
  triggerPredictiveGeneration(): Observable<string> {
    return this.http.post(`${this.apiUrl}/api/provider/alerts/generate-predictive`, {}, { responseType: 'text' })
      .pipe(catchError(err => this.handleError(err)));
  }

  // Create a custom alert
  createAlert(request: AlertRequest): Observable<AlertResponse> {
    console.log(`[AlertService] Creating alert at: ${this.apiUrl}/api/provider/alerts`, request);
    return this.http.post<AlertResponse>(`${this.apiUrl}/api/provider/alerts`, request)
      .pipe(catchError(err => {
        console.error('[AlertService] Create alert error:', err);
        return this.handleError(err);
      }));
  }

  // Update an alert
  updateAlert(alertId: number, request: AlertRequest): Observable<AlertResponse> {
    console.log(`[AlertService] Updating alert ${alertId} at: ${this.apiUrl}/api/provider/alerts/${alertId}`, request);
    return this.http.put<AlertResponse>(`${this.apiUrl}/api/provider/alerts/${alertId}`, request)
      .pipe(catchError(err => {
        console.error(`[AlertService] Update alert ${alertId} error:`, err);
        return this.handleError(err);
      }));
  }

  // Resolve an alert
  resolveAlert(alertId: number): Observable<AlertResponse> {
    const role = this.authService.currentUser?.role?.toUpperCase() || '';
    let endpoint = `${this.apiUrl}/api/provider/alerts/${alertId}/resolve`;

    if (role.includes('PATIENT')) {
      endpoint = `${this.apiUrl}/api/patient/alerts/${alertId}/resolve`;
    } else if (role.includes('CAREGIVER')) {
      endpoint = `${this.apiUrl}/api/caregiver/alerts/${alertId}/resolve`;
    }

    console.log(`[AlertService] Resolving alert ${alertId} at: ${endpoint}`);
    return this.http.patch<AlertResponse>(endpoint, {})
      .pipe(catchError(err => {
        console.error(`[AlertService] Resolve alert ${alertId} error:`, err);
        return this.handleError(err);
      }));
  }

  // Delete an alert
  deleteAlert(alertId: number): Observable<void> {
    console.log(`[AlertService] Deleting alert ${alertId} from: ${this.apiUrl}/api/provider/alerts/${alertId}`);
    return this.http.delete<void>(`${this.apiUrl}/api/provider/alerts/${alertId}`)
      .pipe(
        catchError(err => {
          console.error(`[AlertService] Delete failed for alert ${alertId}:`, err);
          return this.handleError(err);
        })
      );
  }

  // Get alerts for a specific patient (provider view)
  getAlertsByPatient(patientId: number): Observable<AlertResponse[]> {
    const role = this.authService.currentUser?.role?.toUpperCase() || '';
    let endpoint = `${this.apiUrl}/api/provider/alerts/patient/${patientId}`;

    if (role.includes('PATIENT')) {
      endpoint = `${this.apiUrl}/api/patient/alerts`;
    } else if (role.includes('CAREGIVER')) {
      endpoint = `${this.apiUrl}/api/caregiver/alerts`;
    }

    console.log(`[AlertService] Fetching alerts from: ${endpoint} for role: ${role}`);
    return this.http.get<AlertResponse[]>(endpoint)
      .pipe(catchError(err => {
        console.error(`[AlertService] Get alerts error:`, err);
        return this.handleError(err);
      }));
  }
}
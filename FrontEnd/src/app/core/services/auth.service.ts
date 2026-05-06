import { Injectable } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Observable, tap, catchError, throwError, BehaviorSubject } from 'rxjs';
import { Router } from '@angular/router';
import { environment } from '../../../environments/environment';

export interface CurrentUser {
  name: string;
  username: string;
  firstName?: string;
  lastName?: string;
  role: string;
  userId: number;
}

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private apiUrl = environment.apiUrl;  // URL of the backend API (gateway)
  currentUser: CurrentUser | null = null;
  private isLoggedInSubject = new BehaviorSubject<boolean>(false);
  public isLoggedIn$ = this.isLoggedInSubject.asObservable();

  private readonly jwtPattern = /^[A-Za-z0-9-_]+\.[A-Za-z0-9-_]+\.[A-Za-z0-9-_]+$/;
  private expectedRole: string | null = null;

  constructor(private http: HttpClient, private router: Router) {
    this.setupStorageShield();
    this.initializeCurrentUser();
  }

  // Debug helper to find out WHO is interacting with the token
  private setupStorageShield() {
    const originalSetItem = localStorage.setItem.bind(localStorage);
    const originalGetItem = localStorage.getItem.bind(localStorage);

    // Proxy setItem
    localStorage.setItem = (key: string, value: string) => {
      if (key === 'authToken') {
        try {
          const payload = this.decodeJwtPayload(value);
          console.group('%c[STORAGE SHIELD] setItem Detected', 'color: #00ff00; font-weight: bold;');
          console.log(`Setting Role: ${payload.role} | UserId: ${payload.userId}`);
          console.trace();
          console.groupEnd();
        } catch (e) {
          console.warn('[STORAGE SHIELD] Setting invalid token string');
        }
      }
      originalSetItem(key, value);
    };

    // Proxy getItem
    localStorage.getItem = (key: string): string | null => {
      const val = originalGetItem(key);
      if (key === 'authToken' && val) {
        try {
          const payload = this.decodeJwtPayload(val);
          // Only warn if the token role doesn't match the expected role for this session
          if (this.expectedRole && payload.role !== this.expectedRole) {
             console.warn(`%c[STORAGE SHIELD] Session Alert! Expected ${this.expectedRole} token but got ${payload.role}`, 'color: #ffa500; font-weight: bold;');
             console.trace();
          }
        } catch (e) {}
      }
      return val;
    };
  }

  // Check if user is logged in based on stored token
  get isLoggedIn(): boolean {
    return this.isLoggedInSubject.value;
  }

  // Initialize the current user by decoding the token stored in localStorage
  private initializeCurrentUser() {
    const storedAuthValue = localStorage.getItem('authToken');
    const token = this.extractJwtToken(storedAuthValue);

    if (token) {
      try {
        const payload = this.decodeJwtPayload(token);
        const firstName = payload.firstName || '';
        const lastName = payload.lastName || '';
        const fullName = `${firstName} ${lastName}`.trim() || payload.name || payload.username || payload.sub || 'User';
        this.currentUser = {
          name: fullName,
          username: payload.username || payload.sub || 'user',
          firstName: firstName,
          lastName: lastName,
          role: payload.role,
          userId: payload.userId   // <-- userId from token
        };
        this.isLoggedInSubject.next(true);
      } catch (error) {
        console.error('Error parsing token:', error);
        localStorage.removeItem('authToken');
        this.currentUser = null;
        this.isLoggedInSubject.next(false);
      }
    } else {
      this.isLoggedInSubject.next(false);
    }
  }

  // Register a new user
  register(user: any): Observable<any> {
    return this.http.post(`${this.apiUrl}/auth/register`, user, {
      responseType: 'text'
    }).pipe(
      tap((responseText: string) => {
        const message = (responseText || '').toString().trim();
        if (!message) {
          return;
        }
        const lowered = message.toLowerCase();
        if (lowered.includes('already exists') || lowered.includes('already exist')) {
          if (lowered.includes('email')) {
            throw new Error('Email already registered. Please use another or login.');
          }
          if (lowered.includes('username')) {
            throw new Error('Username already taken. Please choose another.');
          }
          throw new Error('An account with these details already exists.');
        }
        if (lowered.includes('duplicate') || lowered.includes('conflict')) {
          throw new Error('An account with these details already exists.');
        }
        if (lowered.includes('exists')) {
          throw new Error('User already exists. Please use different information.');
        }
      }),
      catchError(this.handleError)
    );
  }

  // Login and get JWT token
  login(credentials: any): Observable<any> {
    // Clear any previous role lockdown before attempting new login to prevent role mismatch errors
    this.expectedRole = null;

    return this.http.post(`${this.apiUrl}/auth/login`, credentials, {
      responseType: 'text'
    }).pipe(
      tap((responseBody: string) => {
        const token = this.extractJwtToken(responseBody);

        if (!token) {
          throw new Error('Invalid username or password.');
        }

        // Store token in localStorage
        localStorage.setItem('authToken', token);

        // Decode token
        try {
          const payload = this.decodeJwtPayload(token);
          const firstName = payload.firstName || '';
          const lastName = payload.lastName || '';
          const fullName = `${firstName} ${lastName}`.trim() || payload.name || payload.username || payload.sub || credentials.username;
          this.currentUser = {
            name: fullName,
            username: payload.username || payload.sub || credentials.username,
            firstName: firstName,
            lastName: lastName,
            role: payload.role,
            userId: payload.userId
          };
          this.expectedRole = payload.role; // Lockdown the role for this session
          this.isLoggedInSubject.next(true);
        } catch (error) {
          localStorage.removeItem('authToken');
          this.isLoggedInSubject.next(false);
          throw new Error('Invalid token received. Please try again.');
        }
      }),
      catchError(this.handleError)
    );
  }

  // Login with Google
  googleLogin(idToken: string): Observable<any> {
    // Clear any previous role lockdown before attempting new login
    this.expectedRole = null;

    return this.http.post<any>(`${this.apiUrl}/auth/google`, { idToken }).pipe(
      tap((response: any) => {
        // Response can be { token: "..." } or { newUser: true, ... }
        if (response.newUser) {
          // Do nothing here, the component will handle the modal
          return;
        }

        const token = this.extractJwtToken(response);
        if (!token) throw new Error('Invalid token received from Google authentication.');
        
        localStorage.setItem('authToken', token);
        this.initializeCurrentUser();
      }),
      catchError(this.handleError)
    );
  }

  // Finalize Google Signup with Role selection
  googleComplete(idToken: string, role: string): Observable<any> {
    // Clear any previous role lockdown before attempting new login
    this.expectedRole = null;

    return this.http.post<any>(`${this.apiUrl}/auth/google/complete`, { idToken, role }).pipe(
      tap((response: any) => {
        const token = this.extractJwtToken(response);
        if (!token) throw new Error('Failed to complete registration.');
        
        localStorage.setItem('authToken', token);
        this.initializeCurrentUser();
      }),
      catchError(this.handleError)
    );
  }

  // Logout the user and clear token
  logout() {
    // Clear local session immediately to prevent token usage
    this.clearLocalSession();

    // Then notify the backend so it can invalidate the token
    this.http.post(`${this.apiUrl}/auth/logout`, {}).subscribe({
      next: () => {
        console.log('[AuthService] Logout completed on backend');
      },
      error: (err) => {
        console.warn('[AuthService] Backend logout failed, but local session cleared', err);
      }
    });
  }

  private clearLocalSession() {
    // Clear expectedRole FIRST to prevent token mismatch checks from interfering
    this.expectedRole = null;

    // Clear all auth-related storage
    localStorage.removeItem('authToken');
    this.currentUser = null;
    this.isLoggedInSubject.next(false);

    console.log('[AuthService] Local session cleared completely');

    // Force a full window reload to the home page to clear all RxJS states
    // and prevent components from using old tokens/roles.
    window.location.href = '/homePage';
  }

  // Redirect user to their respective dashboard based on role
  redirectBasedOnRole(role: string) {
    if (role === 'ADMIN') {
      this.router.navigate(['/admin/dashboard']);
    } else if (role === 'PATIENT') {
      this.router.navigate(['/patient/home']);
    } else if (role === 'PROVIDER') {
      this.router.navigate(['/provider/home']);
    } else if (role === 'CAREGIVER') {
      this.router.navigate(['/caregiver/home']);
    }
  }

  // Helper function to extract role from JWT token
  getRoleFromToken(token: string): string {
    const payload = this.decodeJwtPayload(token);
    return payload.role;
  }

  // Get current user ID (convenience method)
  getCurrentUserId(): number | null {
    return this.currentUser?.userId || null;
  }

  // Handle HTTP error responses
  private handleError(error: HttpErrorResponse) {
    let errorMessage = 'An unknown error occurred!';
    const url = error?.url || '';

    if (error instanceof Error) {
      errorMessage = error.message;
    } else if (error.error instanceof ErrorEvent) {
      errorMessage = `Error: ${error.error.message}`;
    } else if (error.status) {
      const raw = typeof error.error === 'string' ? error.error : '';
      const lowered = raw.toLowerCase();

      if (url.includes('/auth/login')) {
        if (error.status === 401 || error.status === 403) {
          // For 403, check if it's a ban/disable message before using generic message
          if (error.status === 403 && raw && (raw.includes('banned') || raw.includes('disabled'))) {
            errorMessage = raw;
          } else {
            errorMessage = 'Invalid username or password.';
          }
        } else if (raw) {
          errorMessage = raw;
        } else {
          errorMessage = 'Login failed. Please try again.';
        }
      } else if (url.includes('/auth/register')) {
        if (error.status === 409 || lowered.includes('already exists') || lowered.includes('duplicate')) {
          if (lowered.includes('email')) {
            errorMessage = 'Email already registered. Please use another or login.';
          } else if (lowered.includes('username')) {
            errorMessage = 'Username already taken. Please choose another.';
          } else {
            errorMessage = 'An account with these details already exists.';
          }
        } else if (raw) {
          errorMessage = raw;
        } else {
          errorMessage = 'Registration failed. Please try again.';
        }
      } else if (raw) {
        errorMessage = raw;
      } else {
        errorMessage = `Error Code: ${error.status}\nMessage: ${error.message}`;
      }
    } else {
      errorMessage = error.message || 'Unknown error occurred';
    }

    console.error('Auth Service Error:', errorMessage, error);
    return throwError(() => new Error(errorMessage));
  }
  
  getToken(): string | null {
    const raw = localStorage.getItem('authToken');
    const token = this.extractJwtToken(raw);

    if (!token) {
      return null;
    }

    // Safety check: if we are supposed to be a specific role but the token is different, clear it
    if (this.expectedRole) {
      try {
        const payload = this.decodeJwtPayload(token);
        if (payload.role !== this.expectedRole) {
          console.error(
            `[AUTH CRITICAL] Token inconsistency detected! Expected ${this.expectedRole} but found ${payload.role}.`
          );
          // Clear the corrupt token immediately to prevent it from being sent
          localStorage.removeItem('authToken');
          this.currentUser = null;
          this.isLoggedInSubject.next(false);
          return null;
        }
      } catch (e) {
        console.warn('[AUTH] Failed to validate token payload:', e);
        // Clear malformed tokens
        localStorage.removeItem('authToken');
        return null;
      }
    }

    return token;
  }

  private extractJwtToken(rawValue: unknown): string | null {
    if (!rawValue) {
      return null;
    }

    // Handle case where rawValue is already a parsed object
    if (typeof rawValue === 'object') {
      const obj = rawValue as any;
      const candidate = obj.token || obj.accessToken || obj.jwt;
      if (typeof candidate === 'string' && this.jwtPattern.test(candidate.trim())) {
        return candidate.trim();
      }
      return null;
    }

    if (typeof rawValue !== 'string' || !rawValue.trim()) {
      return null;
    }

    const trimmed = rawValue.trim();
    if (this.jwtPattern.test(trimmed)) {
      return trimmed;
    }

    try {
      const parsed = JSON.parse(trimmed);
      const candidate = parsed?.token || parsed?.accessToken || parsed?.jwt;

      if (typeof candidate === 'string' && this.jwtPattern.test(candidate.trim())) {
        return candidate.trim();
      }
    } catch {
      return null;
    }

    return null;
  }

  private decodeJwtPayload(token: string): any {
    const payloadPart = token.split('.')[1];
    const base64 = payloadPart.replace(/-/g, '+').replace(/_/g, '/');
    const padded = base64.padEnd(Math.ceil(base64.length / 4) * 4, '=');
    return JSON.parse(atob(padded));
  }

  // Forgot password - send reset link to email
  forgotPassword(email: string): Observable<any> {
    return this.http.post(`${this.apiUrl}/auth/forgot-password`, { email }).pipe(
      catchError(this.handleError)
    );
  }

  // Reset password - validate token and update password
  resetPassword(token: string, newPassword: string, confirmPassword: string): Observable<any> {
    return this.http.post(`${this.apiUrl}/auth/reset-password`, {
      token,
      newPassword,
      confirmPassword
    }).pipe(
      catchError(this.handleError)
    );
  }
}
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Injectable, computed, inject, signal } from '@angular/core';
import { catchError, map, Observable, throwError, timeout } from 'rxjs';

const TOKEN_KEY = 'auth:jwt';
const LOGIN_ENDPOINT = '/api/auth/login';
const LOGIN_TIMEOUT = 30000; // 30 seconds timeout for slower networks

export interface LoginResponse {
  token: string;
}

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);
  private rolesCache: Map<string, string[]> = new Map();

  private readonly _token = signal<string | null>(this.loadToken());
  readonly token = this._token.asReadonly();
  readonly isAuthenticated = computed(() => this._token() !== null);
  readonly roles = computed(() => this.extractRolesFromToken(this._token()));

  login(username: string, password: string): Observable<string> {
    return this.http
      .post<LoginResponse>(LOGIN_ENDPOINT, { username, password })
      .pipe(
        timeout(LOGIN_TIMEOUT),
        map((resp) => resp.token),
        map((token) => {
          this.setToken(token);
          // Pre-cache roles to avoid re-computation
          this.extractRolesFromToken(token);
          return token;
        }),
        catchError((err: unknown) => throwError(() => this.normalizeError(err)))
      );
  }

  logout(): void {
    this._token.set(null);
    try {
      localStorage.removeItem(TOKEN_KEY);
    } catch {
      // Ignore storage quota/security errors.
    }
  }

  hasRole(role: string): boolean {
    return this.roles().includes(role);
  }

  isAdmin(): boolean {
    return this.hasRole('ROLE_ADMIN');
  }

  private setToken(token: string): void {
    this._token.set(token);
    try {
      localStorage.setItem(TOKEN_KEY, token);
    } catch {
      // Ignore storage quota/security errors.
    }
  }

  private loadToken(): string | null {
    try {
      const t = localStorage.getItem(TOKEN_KEY);
      return t && t.trim().length ? t : null;
    } catch {
      return null;
    }
  }

  private extractRolesFromToken(token: string | null): string[] {
    if (!token) return [];

    // Check cache first for performance
    const cached = this.rolesCache.get(token);
    if (cached) return cached;

    try {
      const parts = token.split('.');
      if (parts.length !== 3) return [];

      const decoded = JSON.parse(atob(parts[1]));
      const roles = Array.isArray(decoded.roles) ? decoded.roles : [];

      // Cache the roles for performance
      this.rolesCache.set(token, roles);
      return roles;
    } catch {
      return [];
    }
  }

  private normalizeError(err: unknown): Error {
    if (err instanceof HttpErrorResponse) {
      const msg =
        typeof err.error === 'string'
          ? err.error
          : (err.message ?? 'Login failed');
      return new Error(msg);
    }
    return err instanceof Error ? err : new Error('Login failed');
  }
}


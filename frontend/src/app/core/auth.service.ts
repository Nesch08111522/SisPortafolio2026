import { HttpBackend, HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Injectable, inject, signal } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';

export interface UserView {
  id: string;
  email: string;
  firstName: string;
  lastName: string;
  emailVerified: boolean;
  onboardingCompleted: boolean;
  countryCode: string;
  roles: string[];
}

export interface AuthResponse {
  accessToken: string;
  refreshToken: string;
  expiresIn: number;
  user: UserView;
}

const API = '/api/v1';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly raw = new HttpClient(inject(HttpBackend));
  private readonly router = inject(Router);
  readonly user = signal<UserView | null>(this.readUser());
  readonly accessToken = signal<string | null>(this.stored('ph_access'));

  async ensureSession(): Promise<boolean> {
    const token = this.accessToken();
    if (token && !this.expired(token)) {
      return true;
    }
    return this.refresh();
  }

  hasRole(role: string): boolean {
    return this.user()?.roles.includes(role) ?? false;
  }

  register(body: Record<string, unknown>) {
    return this.http.post<AuthResponse>(`${API}/auth/register`, body);
  }

  login(email: string, password: string) {
    return this.http.post<AuthResponse>(`${API}/auth/login`, { email, password });
  }

  persist(response: AuthResponse, remember = localStorage.getItem('ph_remember') === '1'): void {
    const store = remember ? localStorage : sessionStorage;
    const other = remember ? sessionStorage : localStorage;
    for (const key of ['ph_access', 'ph_refresh', 'ph_user']) other.removeItem(key);
    if (remember) localStorage.setItem('ph_remember', '1');
    else localStorage.removeItem('ph_remember');
    store.setItem('ph_access', response.accessToken);
    store.setItem('ph_refresh', response.refreshToken);
    store.setItem('ph_user', JSON.stringify(response.user));
    this.accessToken.set(response.accessToken);
    this.user.set(response.user);
  }

  async refresh(): Promise<boolean> {
    const refreshToken = this.stored('ph_refresh');
    if (!refreshToken) {
      return false;
    }
    try {
      const response = await new Promise<AuthResponse>((resolve, reject) => {
        this.raw.post<AuthResponse>(`${API}/auth/refresh`, { refreshToken }).subscribe({ next: resolve, error: reject });
      });
      this.persist(response);
      return true;
    } catch {
      this.clear();
      return false;
    }
  }

  logout(): void {
    const refreshToken = this.stored('ph_refresh');
    this.http.post(`${API}/auth/logout`, { refreshToken }).subscribe({ complete: () => this.exit(), error: () => this.exit() });
  }

  private exit(): void {
    this.clear();
    void this.router.navigateByUrl('/');
  }

  private clear(): void {
    for (const store of [sessionStorage, localStorage]) {
      store.removeItem('ph_access');
      store.removeItem('ph_refresh');
      store.removeItem('ph_user');
    }
    localStorage.removeItem('ph_remember');
    this.accessToken.set(null);
    this.user.set(null);
  }

  private stored(key: string): string | null {
    return sessionStorage.getItem(key) ?? localStorage.getItem(key);
  }

  private readUser(): UserView | null {
    const raw = this.stored('ph_user');
    return raw ? JSON.parse(raw) as UserView : null;
  }

  private expired(token: string): boolean {
    try {
      const payload = JSON.parse(atob(token.split('.')[1])) as { exp: number };
      return payload.exp * 1000 < Date.now() + 5000;
    } catch {
      return true;
    }
  }
}

export const authGuard: CanActivateFn = async () => {
  const auth = inject(AuthService);
  return (await auth.ensureSession()) ? true : inject(Router).createUrlTree(['/iniciar-sesion']);
};

export const guestGuard: CanActivateFn = async () => {
  const auth = inject(AuthService);
  return (await auth.ensureSession()) ? inject(Router).createUrlTree(['/app']) : true;
};

export const adminGuard: CanActivateFn = async () => {
  const auth = inject(AuthService);
  const router = inject(Router);
  if (!(await auth.ensureSession())) {
    return router.createUrlTree(['/iniciar-sesion']);
  }
  return auth.hasRole('ADMIN') || auth.hasRole('MODERATOR') ? true : router.createUrlTree(['/app']);
};

export function errorText(error: unknown): string {
  if (error instanceof HttpErrorResponse) {
    const body = error.error as { message?: string; fields?: Record<string, string> } | null;
    if (body?.fields) {
      return Object.values(body.fields).join(' ');
    }
    if (body?.message) {
      return body.message;
    }
  }
  return 'No se pudo completar la acción.';
}

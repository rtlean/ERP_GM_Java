import { HttpClient } from '@angular/common/http';
import { Injectable, computed, inject, signal } from '@angular/core';
import { firstValueFrom } from 'rxjs';
import { Permission, SessionUser } from './api.types';

/**
 * Sessão do usuário. O token fica em cookie httpOnly definido pelo backend; aqui só guardamos
 * quem está logado para a interface. A autorização de verdade acontece no servidor.
 */
@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly current = signal<SessionUser | null>(null);
  private restored: Promise<void> | null = null;

  readonly user = this.current.asReadonly();
  readonly isAuthenticated = computed(() => this.current() !== null);

  /** Restaura a sessão do cookie uma única vez por carregamento da página. */
  restore(): Promise<void> {
    this.restored ??= this.loadSession();
    return this.restored;
  }

  async login(email: string, password: string): Promise<void> {
    await this.ensureCsrfToken();
    const user = await firstValueFrom(
      this.http.post<SessionUser>('/api/v1/auth/login', { email: email.trim().toLowerCase(), password }),
    );
    this.current.set(user);
  }

  async logout(): Promise<void> {
    try {
      await this.ensureCsrfToken();
      await firstValueFrom(this.http.post<void>('/api/v1/auth/logout', {}));
    } finally {
      this.clear();
    }
  }

  /** Chamado quando a API responde 401: a sessão expirou ou foi encerrada no sistema atual. */
  clear(): void {
    this.current.set(null);
  }

  can(permission: Permission): boolean {
    return this.current()?.permissions.includes(permission) ?? false;
  }

  private async loadSession(): Promise<void> {
    try {
      this.current.set(await firstValueFrom(this.http.get<SessionUser>('/api/v1/auth/me')));
    } catch {
      this.current.set(null);
    }
  }

  /** O backend só emite o cookie XSRF-TOKEN quando solicitado; o HttpClient o devolve em X-XSRF-TOKEN. */
  private ensureCsrfToken(): Promise<void> {
    return firstValueFrom(this.http.get<void>('/api/v1/auth/csrf'));
  }
}

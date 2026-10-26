import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';
import { Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { Permission } from '../core/api.types';
import { AuthService } from '../core/auth.service';

interface NavItem {
  label: string;
  path?: string;
  permission?: Permission;
}

/** Módulos já migrados abrem aqui; os demais continuam no sistema atual. */
const NAV: NavItem[] = [
  { label: 'Locadores', path: '/locadores', permission: 'PEOPLE_READ' },
  { label: 'Imóveis', path: '/imoveis', permission: 'PROPERTIES_READ' },
  { label: 'Locatários', path: '/locatarios', permission: 'PEOPLE_READ' },
];
const PENDING: NavItem[] = [
  { label: 'Contratos' },
  { label: 'Financeiro' },
  { label: 'Relatório' },
  { label: 'Configurações' },
];

export const LEGACY_URL = 'https://www.admgmimobiliaria.com.br/';

@Component({
  selector: 'app-shell',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [RouterOutlet, RouterLink, RouterLinkActive],
  template: `
    <a class="skip" href="#conteudo">Pular para o conteúdo</a>
    <div class="layout" [class.collapsed]="collapsed()">
      <aside class="sidebar" aria-label="Navegação principal">
        <div class="brand">
          <img src="brand-logo.svg" alt="GM Imobiliária" height="40" />
        </div>
        <nav>
          <span class="group">Cadastros</span>
          @for (item of nav(); track item.path) {
            <a [routerLink]="item.path" routerLinkActive="active" [attr.title]="item.label">{{ item.label }}</a>
          }
          <span class="group">Em migração</span>
          @for (item of pending; track item.label) {
            <span class="pending" title="Disponível no sistema atual">{{ item.label }}</span>
          }
          <a class="legacy" [href]="legacyUrl" target="_blank" rel="noopener">Abrir sistema atual ↗</a>
        </nav>
      </aside>

      <div class="main">
        <header class="topbar">
          <button type="button" class="ghost icon" (click)="collapsed.set(!collapsed())"
                  [attr.aria-expanded]="!collapsed()" aria-label="Recolher ou expandir menu">☰</button>
          <span class="env">Homologação</span>
          <div class="user">
            <span class="name">{{ displayName() }}</span>
            <span class="role">{{ roleLabel() }}</span>
          </div>
          <button type="button" class="ghost" (click)="logout()" [disabled]="leaving()">Sair</button>
        </header>
        <main id="conteudo" class="content" tabindex="-1">
          <router-outlet />
        </main>
      </div>
    </div>
  `,
  styles: `
    .skip { position: absolute; left: -9999px; }
    .skip:focus { left: 16px; top: 16px; z-index: 10; background: var(--surface); padding: 8px 12px; border-radius: 8px; }
    .layout { display: grid; grid-template-columns: 240px 1fr; min-height: 100vh; }
    .layout.collapsed { grid-template-columns: 0 1fr; }
    .layout.collapsed .sidebar { visibility: hidden; }
    .sidebar { background: var(--navy); color: #fff; display: flex; flex-direction: column; overflow: hidden; }
    .brand { background: var(--surface); padding: 16px; display: flex; justify-content: center; }
    .brand img { height: 40px; width: auto; }
    nav { display: flex; flex-direction: column; padding: 12px; gap: 2px; }
    .group { font-size: 11px; text-transform: uppercase; letter-spacing: 0.08em; color: rgb(255 255 255 / 55%); padding: 16px 12px 6px; }
    nav a, .pending { padding: 10px 12px; border-radius: 8px; color: #fff; text-decoration: none; font-weight: 500; border-left: 3px solid transparent; }
    nav a:hover { background: rgb(255 255 255 / 8%); }
    nav a.active { background: rgb(255 255 255 / 12%); border-left-color: var(--tab); }
    .pending { color: rgb(255 255 255 / 45%); cursor: default; }
    .legacy { margin-top: 12px; font-size: 14px; color: rgb(255 255 255 / 80%) !important; }
    .main { display: flex; flex-direction: column; min-width: 0; }
    .topbar { display: flex; align-items: center; gap: 16px; height: 64px; padding: 0 24px; background: var(--surface); border-bottom: 1px solid var(--line); }
    .icon { width: 40px; padding: 0; font-size: 18px; }
    .env { font-size: 12px; font-weight: 600; color: #9a3412; background: #fff7ed; border: 1px solid #fed7aa; border-radius: 999px; padding: 3px 10px; }
    .user { margin-left: auto; display: flex; flex-direction: column; align-items: flex-end; line-height: 1.2; }
    .name { font-weight: 600; }
    .role { font-size: 12px; color: var(--muted); }
    .content { padding: 28px; outline: none; }
    @media (max-width: 900px) {
      .layout { grid-template-columns: 0 1fr; }
      .layout .sidebar { visibility: hidden; }
      .layout.collapsed { grid-template-columns: 220px 1fr; }
      .layout.collapsed .sidebar { visibility: visible; }
      .content { padding: 20px 16px; }
    }
  `,
})
export class ShellComponent {
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);

  protected readonly legacyUrl = LEGACY_URL;
  protected readonly pending = PENDING;
  protected readonly collapsed = signal(false);
  protected readonly leaving = signal(false);
  protected readonly nav = computed(() => {
    this.auth.user();
    return NAV.filter((item) => !item.permission || this.auth.can(item.permission));
  });
  protected readonly displayName = computed(() => this.auth.user()?.name || this.auth.user()?.email || '');
  protected readonly roleLabel = computed(() =>
    this.auth.user()?.role === 'ADMINISTRADOR' ? 'Administrador' : 'Operacional',
  );

  protected async logout(): Promise<void> {
    this.leaving.set(true);
    try {
      await this.auth.logout();
    } finally {
      this.leaving.set(false);
      await this.router.navigate(['/login']);
    }
  }
}

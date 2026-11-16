import { Component, inject, signal } from '@angular/core';
import { NavigationEnd, Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { filter } from 'rxjs';
import { ApiService, Dashboard, Notice, ProfileResponse } from '../core/api.service';
import { AuthService } from '../core/auth.service';

@Component({
  selector: 'app-shell',
  imports: [RouterOutlet, RouterLink, RouterLinkActive],
  template: `
    <div class="hub" [class.rail]="rail()">
      @if (open()) { <button class="hub-scrim" type="button" aria-label="Cerrar menú" (click)="open.set(false)"></button> }
      <aside id="navegacion" class="hub-side" [class.open]="open()">
        <div class="hub-brandbar">
          <a class="hub-brand" routerLink="/app" (click)="open.set(false)" aria-label="Portfolio Hub">
            <span class="hub-mark">P</span>
            <span class="hub-brandcopy">
              <strong>Portfolio Hub</strong>
              <small>Archivo académico</small>
            </span>
          </a>
          <button class="hub-rail-btn" type="button" (click)="toggleRail()" [attr.aria-pressed]="rail()" [attr.aria-label]="rail() ? 'Expandir el menú' : 'Retraer el menú'">
            <svg viewBox="0 0 24 24" aria-hidden="true"><path d="M15 6l-6 6 6 6"/></svg>
          </button>
          <button class="hub-icon hub-close" type="button" aria-label="Cerrar menú" (click)="open.set(false)">
            <svg viewBox="0 0 24 24" aria-hidden="true"><path d="M6 6l12 12M18 6L6 18"/></svg>
          </button>
        </div>
        <p class="hub-kicker">Módulos del sistema</p>
        <nav aria-label="Navegación principal">
          @for (item of items; track item.path; let index = $index) {
            <a [routerLink]="item.path" routerLinkActive="active" [routerLinkActiveOptions]="{ exact: item.exact }" [attr.aria-label]="item.label" [attr.title]="item.label" (click)="open.set(false)">
              <span class="hub-glyph" aria-hidden="true">
                @switch (item.icon) {
                  @case ('panel') { <svg viewBox="0 0 24 24"><rect x="3" y="3" width="7" height="7"/><rect x="14" y="3" width="7" height="7"/><rect x="14" y="14" width="7" height="7"/><rect x="3" y="14" width="7" height="7"/></svg> }
                  @case ('perfil') { <svg viewBox="0 0 24 24"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg> }
                  @case ('formacion') { <svg viewBox="0 0 24 24"><path d="M22 10v6M2 10l10-5 10 5-10 5z"/><path d="M6 12v5c3 3 9 3 12 0v-5"/></svg> }
                  @case ('contenido') { <svg viewBox="0 0 24 24"><path d="M4 19.5A2.5 2.5 0 0 1 6.5 17H20"/><path d="M6.5 2H20v20H6.5A2.5 2.5 0 0 1 4 19.5v-15A2.5 2.5 0 0 1 6.5 2z"/></svg> }
                  @case ('documentos') { <svg viewBox="0 0 24 24"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><path d="M14 2v6h6M8 13h8M8 17h8"/></svg> }
                  @case ('disenos') { <svg viewBox="0 0 24 24"><rect x="3" y="3" width="18" height="18"/><path d="M3 9h18M9 21V9"/></svg> }
                  @case ('editor') { <svg viewBox="0 0 24 24"><path d="M4 21V14M4 10V3M12 21V12M12 8V3M20 21V16M20 12V3M1 14h6M9 8h6M17 16h6"/></svg> }
                  @case ('publicar') { <svg viewBox="0 0 24 24"><circle cx="12" cy="12" r="10"/><path d="M2 12h20M12 2a15 15 0 0 1 4 10 15 15 0 0 1-4 10 15 15 0 0 1-4-10 15 15 0 0 1 4-10z"/></svg> }
                  @case ('mensajes') { <svg viewBox="0 0 24 24"><path d="M4 4h16a2 2 0 0 1 2 2v12a2 2 0 0 1-2 2H4a2 2 0 0 1-2-2V6a2 2 0 0 1 2-2z"/><path d="M22 6l-10 7L2 6"/></svg> }
                }
              </span>
              <span class="hub-label">{{ item.label }}</span>
              @if (item.path === '/app/mensajes' && unread()) { <b class="hub-index">{{ unread() }}</b> }
              @else { <em class="hub-index">{{ pad(index + 1) }}</em> }
            </a>
          }
          @if (auth.hasRole('ADMIN') || auth.hasRole('MODERATOR')) {
            <a routerLink="/app/admin" routerLinkActive="active" aria-label="Administración" title="Administración" (click)="open.set(false)">
              <span class="hub-glyph" aria-hidden="true"><svg viewBox="0 0 24 24"><path d="M12 3l8 4v6c0 5-3.5 7.5-8 8-4.5-.5-8-3-8-8V7z"/></svg></span>
              <span class="hub-label">Administración</span><em class="hub-index">10</em>
            </a>
          }
        </nav>
        <div class="hub-sidefoot">
          <div class="hub-status" [attr.title]="status()">
            <div><span>Estado del sitio</span><i [class.off]="status() !== 'PUBLICADO'"></i></div>
            <strong>{{ status() }}</strong>
          </div>
          <button class="hub-logout" type="button" aria-label="Cerrar sesión" title="Cerrar sesión" (click)="auth.logout()">
            <svg viewBox="0 0 24 24" aria-hidden="true"><path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4M16 17l5-5-5-5M21 12H9"/></svg>
            <span>Cerrar sesión</span>
          </button>
        </div>
      </aside>
      <div class="hub-body">
        <header class="hub-top">
          <button class="hub-icon hub-menu" type="button" [attr.aria-expanded]="open()" aria-controls="navegacion" [attr.aria-label]="open() ? 'Cerrar menú' : 'Abrir menú'" (click)="open.set(!open())">
            @if (open()) {
              <svg viewBox="0 0 24 24" aria-hidden="true"><path d="M6 6l12 12M18 6L6 18"/></svg>
            } @else {
              <svg viewBox="0 0 24 24" aria-hidden="true"><path d="M4 7h16M4 12h16M4 17h16"/></svg>
            }
          </button>
          <div class="hub-context">
            <p>Portfolio Hub / {{ crumb() }}</p>
            <strong>{{ name() }} <span>· {{ career() }}@if (place()) { · {{ place() }} }</span></strong>
          </div>
          <div class="hub-tools">
            @if (board()?.publicUrl) {
              <div class="hub-url">
                <a [href]="board()?.publicUrl" target="_blank" rel="noopener noreferrer">{{ board()?.publicUrl }}</a>
                <button type="button" (click)="copy()">{{ copied() ? 'Copiado' : 'Copiar' }}</button>
              </div>
            }
            <div class="hub-notepos">
              <button class="hub-icon" type="button" aria-label="Notificaciones" [attr.aria-expanded]="notesOpen()" (click)="notesOpen.set(!notesOpen())">
                <svg viewBox="0 0 24 24" aria-hidden="true"><path d="M6 9a6 6 0 0 1 12 0c0 5 2 7 2 7H4s2-2 2-7"/><path d="M10 19a2 2 0 0 0 4 0"/></svg>
                @if (unread()) { <i></i> }
              </button>
              @if (notesOpen()) {
                <div class="hub-notes">
                  <p>Notificaciones <span>{{ unread() }} nuevas</span></p>
                  @for (note of notices(); track note.id) {
                    <a routerLink="/app/mensajes" (click)="notesOpen.set(false)">
                      <strong>{{ note.title }}</strong>
                      <span>{{ note.body }}</span>
                    </a>
                  } @empty { <span class="quiet">No hay avisos nuevos.</span> }
                </div>
              }
            </div>
            <a class="hub-avatar" routerLink="/app/perfil" [attr.aria-label]="'Ver perfil de ' + name()">{{ initials() }}</a>
          </div>
        </header>
        <main class="hub-main" id="contenido">
          <router-outlet />
        </main>
        <footer class="hub-foot">
          <span>© 2026 Portfolio Hub · Sistema de gestión de portafolios</span>
          <nav>
            <a routerLink="/privacidad">Privacidad</a>
            <a routerLink="/cookies">Cookies</a>
            <a routerLink="/terminos">Términos</a>
          </nav>
          <span><i></i> {{ name() }}</span>
        </footer>
      </div>
      @if (copied()) { <p class="hub-toast" role="status">Enlace copiado</p> }
    </div>
  `
})
export class Shell {
  readonly auth = inject(AuthService);
  private readonly api = inject(ApiService);
  private readonly router = inject(Router);
  readonly open = signal(false);
  readonly rail = signal(this.storedRail());
  readonly notesOpen = signal(false);
  readonly copied = signal(false);
  readonly profile = signal<ProfileResponse | null>(null);
  readonly board = signal<Dashboard | null>(null);
  readonly notices = signal<Notice[]>([]);
  readonly items = [
    { path: '/app', label: 'Panel', exact: true, icon: 'panel' },
    { path: '/app/perfil', label: 'Perfil', exact: false, icon: 'perfil' },
    { path: '/app/academico', label: 'Formación', exact: false, icon: 'formacion' },
    { path: '/app/contenido', label: 'Contenido', exact: false, icon: 'contenido' },
    { path: '/app/documentos', label: 'Documentos', exact: false, icon: 'documentos' },
    { path: '/app/disenos', label: 'Diseños', exact: false, icon: 'disenos' },
    { path: '/app/editor', label: 'Editor', exact: false, icon: 'editor' },
    { path: '/app/publicar', label: 'Publicación', exact: false, icon: 'publicar' },
    { path: '/app/mensajes', label: 'Mensajes', exact: false, icon: 'mensajes' }
  ];

  constructor() {
    this.api.profile().subscribe({ next: (item) => this.profile.set(item), error: () => undefined });
    this.api.dashboard().subscribe({ next: (item) => this.board.set(item), error: () => undefined });
    this.api.notifications().subscribe({ next: (items) => this.notices.set(items), error: () => undefined });
    this.router.events.pipe(filter((event) => event instanceof NavigationEnd)).subscribe(() => {
      this.open.set(false);
      this.notesOpen.set(false);
    });
  }

  name(): string {
    const first = this.profile()?.firstName || this.auth.user()?.firstName || '';
    const last = this.profile()?.lastName || this.auth.user()?.lastName || '';
    return `${first} ${last}`.trim() || 'Tu cuenta';
  }

  initials(): string {
    const first = (this.profile()?.firstName || this.auth.user()?.firstName || 'P').charAt(0);
    const last = (this.profile()?.lastName || this.auth.user()?.lastName || '').charAt(0);
    return `${first}${last}`.toUpperCase();
  }

  career(): string {
    return this.board()?.career || this.profile()?.academic?.careerName || 'Carrera pendiente';
  }

  place(): string {
    return this.board()?.location || this.profile()?.personal?.locationLabel || '';
  }

  status(): string {
    const value = this.board()?.publicationStatus;
    if (value === 'PUBLISHED') return 'PUBLICADO';
    if (value === 'UNPUBLISHED') return 'NO PUBLICADO';
    return 'BORRADOR';
  }

  unread(): number {
    return this.notices().filter((item) => !item.readAt).length;
  }

  crumb(): string {
    const path = this.router.url.split('?')[0];
    if (path === '/app') return 'ESCRITORIO';
    return this.items.find((item) => item.exact ? path === item.path : path.startsWith(item.path))?.label.toUpperCase()
      || (path.startsWith('/app/admin') ? 'ADMINISTRACIÓN' : 'ESCRITORIO');
  }

  pad(value: number): string {
    return String(value).padStart(2, '0');
  }

  toggleRail(): void {
    this.rail.update((value) => !value);
    try { localStorage.setItem('ph_side', this.rail() ? 'icons' : 'full'); } catch { /* El navegador puede bloquear el almacenamiento. */ }
  }

  private storedRail(): boolean {
    try { return localStorage.getItem('ph_side') === 'icons'; } catch { return false; }
  }

  copy(): void {
    const url = this.board()?.publicUrl;
    if (!url) return;
    const done = () => {
      this.copied.set(true);
      setTimeout(() => this.copied.set(false), 2000);
    };
    if (navigator.clipboard?.writeText) {
      navigator.clipboard.writeText(url).then(done).catch(() => this.fallbackCopy(url, done));
    } else {
      this.fallbackCopy(url, done);
    }
  }

  private fallbackCopy(url: string, done: () => void): void {
    const field = document.createElement('textarea');
    field.value = url;
    field.style.position = 'fixed';
    field.style.opacity = '0';
    document.body.appendChild(field);
    field.select();
    document.execCommand('copy');
    field.remove();
    done();
  }
}

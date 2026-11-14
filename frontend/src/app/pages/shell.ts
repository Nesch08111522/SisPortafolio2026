import { Component, inject, signal } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { AuthService } from '../core/auth.service';

@Component({
  selector: 'app-shell',
  imports: [RouterOutlet, RouterLink, RouterLinkActive],
  template: `
    <div class="shell">
      @if (open()) { <button class="scrim" type="button" aria-label="Cerrar menú" (click)="open.set(false)"></button> }
      <aside class="sidebar" id="navegacion" [class.open]="open()">
        <div class="brand"><span class="mark">P</span> Portfolio Hub</div>
        <a routerLink="/app" routerLinkActive="active" [routerLinkActiveOptions]="{ exact: true }" (click)="open.set(false)">Panel</a>
        <a routerLink="/app/perfil" routerLinkActive="active" (click)="open.set(false)">Perfil</a>
        <a routerLink="/app/academico" routerLinkActive="active" (click)="open.set(false)">Formación</a>
        <a routerLink="/app/contenido" routerLinkActive="active" (click)="open.set(false)">Contenido</a>
        <a routerLink="/app/documentos" routerLinkActive="active" (click)="open.set(false)">Documentos</a>
        <a routerLink="/app/disenos" routerLinkActive="active" (click)="open.set(false)">Diseños</a>
        <a routerLink="/app/editor" routerLinkActive="active" (click)="open.set(false)">Editor</a>
        <a routerLink="/app/publicar" routerLinkActive="active" (click)="open.set(false)">Publicación</a>
        <a routerLink="/app/mensajes" routerLinkActive="active" (click)="open.set(false)">Mensajes</a>
        @if (auth.hasRole('ADMIN') || auth.hasRole('MODERATOR')) {
          <a routerLink="/app/admin" routerLinkActive="active" (click)="open.set(false)">Administración</a>
        }
        <button class="linkish" type="button" (click)="auth.logout()">Cerrar sesión</button>
      </aside>
      <div class="page">
        <header class="topbar">
          <button class="btn-ghost menu" type="button" [attr.aria-expanded]="open()" aria-controls="navegacion" (click)="open.set(!open())">Menú</button>
          <strong>{{ auth.user()?.firstName }} {{ auth.user()?.lastName }}</strong>
        </header>
        <router-outlet />
      </div>
    </div>
  `
})
export class Shell {
  readonly auth = inject(AuthService);
  readonly open = signal(false);
}

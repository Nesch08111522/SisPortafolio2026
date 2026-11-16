import { Component, inject, signal } from '@angular/core';
import { NavigationEnd, Router, RouterLink } from '@angular/router';
import { filter } from 'rxjs';
import { ApiService } from '../core/api.service';

@Component({
  selector: 'app-consent',
  imports: [RouterLink],
  template: `
    @if (visible()) {
      <div class="consent-bar" role="dialog" aria-labelledby="cookies-titulo">
        <p id="cookies-titulo">Este sistema guarda una preferencia en el navegador para recordar tu elección. La sesión de quien inicia sesión no se publica. La visita anónima de un sitio solo se registra si la aceptas. Consulta la <a [routerLink]="privacy()">privacidad</a>, las <a [routerLink]="cookies()">cookies</a> y los <a [routerLink]="terms()">términos</a>.</p>
        <button class="btn" type="button" (click)="choose(true)">Aceptar</button>
        <button class="btn-ghost" type="button" (click)="choose(false)">Solo lo esencial</button>
      </div>
    }
  `
})
export class ConsentBar {
  private readonly router = inject(Router);
  private readonly api = inject(ApiService);
  readonly visible = signal(!localStorage.getItem('ph_consent'));
  private path = this.router.url;

  constructor() {
    this.router.events.pipe(filter((event) => event instanceof NavigationEnd)).subscribe((event) => {
      this.path = event.urlAfterRedirects;
    });
  }

  privacy(): string { return this.legal('privacidad'); }
  cookies(): string { return this.legal('cookies'); }
  terms(): string { return this.legal('terminos'); }

  choose(analytics: boolean): void {
    localStorage.setItem('ph_consent', analytics ? 'all' : 'essential');
    this.visible.set(false);
    const slug = this.slug();
    if (!analytics || !slug) return;
    const device = window.innerWidth < 700 ? 'mobile' : window.innerWidth < 1100 ? 'tablet' : 'desktop';
    this.api.visit(slug, { consent: true, path: this.path, device }).subscribe({ error: () => undefined });
  }

  private slug(): string {
    const match = this.path.match(/^\/p\/([^/]+)/);
    return match ? decodeURIComponent(match[1]) : '';
  }

  private legal(page: string): string {
    const slug = this.slug();
    return slug ? `/p/${slug}/${page}` : `/${page}`;
  }
}

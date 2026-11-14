import { Component, inject, signal } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { ApiService, PortfolioView } from '../core/api.service';
import { errorText } from '../core/auth.service';
import { FolioView } from '../folio/folio-view';

@Component({
  selector: 'app-public-folio',
  imports: [FolioView],
  template: `
    @if (error()) { <main class="page" id="contenido"><p class="card">{{ error() }}</p></main> }
    @if (view()) {
      <div class="page">
        <p class="card">Si la persona autora activó la analítica, puedes permitir una visita anónima con la ruta y el tipo de dispositivo. No se guarda una ubicación precisa.</p>
        <button class="btn" type="button" (click)="consent(true)" [disabled]="recorded()">Aceptar registro</button>
        <button class="btn-ghost" type="button" (click)="consent(false)">No registrar</button>
      </div>
    }
    <app-folio-view [view]="view()" [slug]="slug" />
  `
})
export class PublicFolioPage {
  private readonly api = inject(ApiService);
  private readonly route = inject(ActivatedRoute);
  readonly view = signal<PortfolioView | null>(null);
  readonly error = signal('');
  readonly recorded = signal(false);
  readonly slug = this.route.snapshot.paramMap.get('slug') ?? '';

  constructor() {
    this.api.publicPortfolio(this.slug).subscribe({
      next: (view) => this.view.set(view),
      error: (err) => this.error.set(errorText(err))
    });
  }

  consent(accepted: boolean): void {
    this.recorded.set(true);
    if (!accepted) return;
    const device = window.innerWidth < 700 ? 'mobile' : window.innerWidth < 1100 ? 'tablet' : 'desktop';
    this.api.visit(this.slug, { consent: true, path: `/p/${this.slug}`, device }).subscribe({ error: () => undefined });
  }
}

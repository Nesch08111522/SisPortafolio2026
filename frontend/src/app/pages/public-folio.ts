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
    @if (view() && !recorded()) {
      <div class="consent-bar">
        <p>Si la persona autora activó la analítica, puedes permitir una visita anónima con la ruta y el tipo de dispositivo. No se guarda una ubicación precisa.</p>
        <button class="btn" type="button" (click)="consent(true)">Aceptar registro</button>
        <button class="btn-ghost" type="button" (click)="consent(false)">No registrar</button>
      </div>
    }
    <app-folio-view [view]="view()" [slug]="slug()" [page]="page()" />
  `
})
export class PublicFolioPage {
  private readonly api = inject(ApiService);
  private readonly route = inject(ActivatedRoute);
  readonly view = signal<PortfolioView | null>(null);
  readonly error = signal('');
  readonly recorded = signal(false);
  readonly slug = signal('');
  readonly page = signal('inicio');

  constructor() {
    this.route.paramMap.subscribe((params) => {
      const slug = params.get('slug') ?? '';
      this.page.set(params.get('page') ?? 'inicio');
      if (slug === this.slug()) return;
      this.slug.set(slug);
      this.view.set(null);
      this.error.set('');
      this.recorded.set(false);
      this.api.publicPortfolio(slug).subscribe({
        next: (view) => this.view.set(view),
        error: (err) => this.error.set(errorText(err))
      });
    });
  }

  consent(accepted: boolean): void {
    this.recorded.set(true);
    if (!accepted) return;
    const device = window.innerWidth < 700 ? 'mobile' : window.innerWidth < 1100 ? 'tablet' : 'desktop';
    const slug = this.slug();
    this.api.visit(slug, { consent: true, path: `/p/${slug}`, device }).subscribe({ error: () => undefined });
  }
}

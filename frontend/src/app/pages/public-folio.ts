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
    <app-folio-view [view]="view()" [slug]="slug()" [page]="page()" />
  `
})
export class PublicFolioPage {
  private readonly api = inject(ApiService);
  private readonly route = inject(ActivatedRoute);
  readonly view = signal<PortfolioView | null>(null);
  readonly error = signal('');
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
      this.api.publicPortfolio(slug).subscribe({
        next: (view) => this.view.set(view),
        error: (err) => this.error.set(errorText(err))
      });
    });
  }

}

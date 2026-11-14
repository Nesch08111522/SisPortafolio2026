import { Component, Input, OnChanges, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Title, Meta } from '@angular/platform-browser';
import { ApiService, PortfolioView, ProjectView, SectionState } from '../core/api.service';

@Component({
  selector: 'app-folio-view',
  imports: [ReactiveFormsModule],
  templateUrl: './folio-view.html'
})
export class FolioView implements OnChanges {
  private readonly titles = inject(Title);
  private readonly meta = inject(Meta);
  private readonly api = inject(ApiService);
  private readonly forms = inject(FormBuilder);
  readonly locale = signal<'es' | 'en'>('es');
  readonly recruiter = signal(false);
  readonly sent = signal('');
  readonly contactForm = this.forms.nonNullable.group({
    name: ['', Validators.required],
    email: ['', [Validators.required, Validators.email]],
    message: ['', [Validators.required, Validators.minLength(10)]],
    companyWebsite: ['']
  });

  @Input() view: PortfolioView | null = null;
  @Input() slug = '';

  ngOnChanges(): void {
    const view = this.view;
    if (!view?.seo) return;
    this.titles.setTitle(view.seo.title);
    this.meta.updateTag({ name: 'description', content: view.seo.description });
    this.meta.updateTag({ property: 'og:title', content: view.seo.title });
    this.meta.updateTag({ property: 'og:description', content: view.seo.description });
    this.meta.updateTag({ property: 'og:url', content: view.seo.canonicalUrl });
    const existing = document.getElementById('jsonld');
    existing?.remove();
    const script = document.createElement('script');
    script.id = 'jsonld';
    script.type = 'application/ld+json';
    script.textContent = JSON.stringify(view.seo.jsonLd);
    document.head.appendChild(script);
    let canonical = document.querySelector("link[rel='canonical']");
    if (!canonical) {
      canonical = document.createElement('link');
      canonical.setAttribute('rel', 'canonical');
      document.head.appendChild(canonical);
    }
    canonical.setAttribute('href', view.seo.canonicalUrl);
  }

  text(section: SectionState, field = 'body'): string {
    const content = section.content ?? {};
    const english = content[field + 'En'];
    const spanish = content[field];
    if (this.locale() === 'en' && typeof english === 'string' && english.trim()) return english;
    return typeof spanish === 'string' ? spanish : '';
  }

  projectsFor(section: SectionState): ProjectView[] {
    const all = this.view?.projects ?? [];
    if (section.variant === 'repositories') return all.filter((item) => !!item.links?.['github'] || !!item.links?.['gitlab']);
    if (section.variant === 'architecture') return all.filter((item) => !!item.architectureNote);
    return all;
  }

  coursesFor(section: SectionState) {
    const all = this.view?.courses ?? [];
    if (section.variant === 'progress') return all.filter((item) => item.status === 'EN_CURSO');
    if (section.variant === 'completed') return all.filter((item) => item.status === 'COMPLETADO');
    return all;
  }

  experiencesFor(section: SectionState) {
    const all = this.view?.experiences ?? [];
    const kind = ({ volunteer: 'VOLUNTEER', conference: 'ACADEMIC', competition: 'ACADEMIC', preprofessional: 'PREPROFESSIONAL', formative: 'FORMATIVE', leadership: 'WORK', academic: 'ACADEMIC' } as Record<string, string>)[section.variant ?? ''];
    return kind ? all.filter((item) => item.kind === kind) : all;
  }

  publicationsFor(section: SectionState) {
    const all = this.view?.publications ?? [];
    if (section.variant === 'cases') return all.filter((item) => item.kind === 'CASE_ANALYSIS');
    return all;
  }

  skillsFor(section: SectionState) {
    const all = this.view?.skills ?? [];
    if (section.variant === 'tools') return all.filter((item) => item.category === 'TOOL' || item.category === 'TECHNICAL');
    if (section.variant === 'academic') return all.filter((item) => item.category === 'ACADEMIC' || item.category === 'PROFESSIONAL');
    return all;
  }

  items(section: SectionState): { title?: string; description?: string }[] {
    const value = section.content?.['items'];
    return Array.isArray(value) ? value as { title?: string; description?: string }[] : [];
  }

  heading(): string {
    return `"${this.view?.theme.fontHeading || 'Fraunces'}", Georgia, serif`;
  }

  bodyFont(): string {
    return `"${this.view?.theme.fontBody || 'Outfit'}", sans-serif`;
  }

  send(): void {
    if (!this.slug || this.contactForm.invalid) {
      this.contactForm.markAllAsTouched();
      return;
    }
    this.api.contact(this.slug, this.contactForm.getRawValue()).subscribe({
      next: () => {
        this.sent.set('Mensaje enviado.');
        this.contactForm.reset();
      },
      error: () => this.sent.set('No se pudo enviar el mensaje.')
    });
  }
}

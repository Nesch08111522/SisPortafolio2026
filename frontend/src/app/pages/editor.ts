import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { ApiService, PortfolioView, SectionState, TemplateCard, VersionItem } from '../core/api.service';
import { errorText } from '../core/auth.service';
import { FolioView } from '../folio/folio-view';
import { Steps } from './steps';

@Component({
  selector: 'app-designs',
  imports: [FolioView, RouterLink, Steps],
  template: `
    <app-steps />
    <p class="page-kicker">Composiciones</p>
    <h1 class="page-title">Diez diseños{{ career() ? ' para ' + career() : '' }}</h1>
    <p class="note">{{ familyNote() }} Cada sitio usa una sola composición. Cambiar de diseño conserva el texto de las secciones que comparten clave.</p>
    @if (message()) { <p [class.error]="failed()">{{ message() }}</p> }
    <div class="design-grid">
      @for (item of designs(); track item.templateId) {
        <article class="card design-card">
          <p class="page-kicker">{{ rank(item.layout) }}</p>
          <div class="design-stage">
            <app-folio-view [view]="preview()" [layoutOverride]="item.layout" [themeOverride]="item.theme ?? null" />
          </div>
          <h2>{{ item.name }}</h2>
          <p>{{ item.variantName }}</p>
          <p>{{ item.description }}</p>
          @if (item.disclaimer) { <p class="note">{{ item.disclaimer }}</p> }
          <button class="btn-ghost" type="button" (click)="pin(item)">{{ compared().includes(item) ? 'Quitar de la comparación' : 'Comparar' }}</button>
          <button class="btn" type="button" (click)="apply(item.templateId)">Usar este diseño</button>
        </article>
      } @empty { <p class="empty">Elige primero una carrera para ver configuraciones especializadas.</p> }
    </div>
    @if (compared().length) {
      <h2>Comparación</h2>
      <div class="design-grid">
        @for (item of compared(); track item.templateId) {
          <article class="card">
            <strong>{{ item.name }}</strong>
            <ul>@for (section of item.sections; track section.key) { <li>{{ section.title }}</li> }</ul>
          </article>
        }
      </div>
    }
  `
})
export class DesignsPage {
  private readonly api = inject(ApiService);
  private readonly router = inject(Router);
  readonly designs = signal<TemplateCard[]>([]);
  readonly compared = signal<TemplateCard[]>([]);
  readonly preview = signal<PortfolioView | null>(null);
  readonly message = signal('');
  readonly failed = signal(false);
  readonly career = signal('');
  private readonly ranks: Record<string, string> = {
    minimal: 'CV investigador', corporate: 'CV de negocios', tech: 'Portafolio tecnológico', editorial: 'Estudio editorial',
    creative: 'CV creativo', academic: 'CV académico', executive: 'CV profesional', experimental: 'Portafolio contemporáneo',
    dark: 'Sesión fotográfica', premium: 'Portafolio de marca',
    studio: 'Estudio de marca', exhibit: 'Sala de exposición', interior: 'Proyecto de interiores',
    screen: 'Retrato cinematográfico', fashion: 'Editorial de moda', gazette: 'Diario de investigación'
  };
  constructor() {
    this.api.preview().subscribe({ next: (view) => this.preview.set(view), error: () => undefined });
    this.api.profile().subscribe({
      next: (profile) => {
        const name = profile.academic?.careerName ?? '';
        this.career.set(name);
        this.api.recommended(profile.academic?.careerId).subscribe((items) => this.designs.set(this.forCareer(items, name)));
      },
      error: () => this.designs.set([])
    });
  }
  familyNote(): string {
    const name = this.career().toLowerCase();
    if (/medic|salud|enferm|odonto|nutri|psicol|farmac|biolog|veterin/.test(name)) return 'Para ciencias de la salud se priorizan composiciones clínicas, editoriales y de investigación.';
    if (/derech|legal|jurid/.test(name)) return 'Para derecho se priorizan composiciones editoriales, ejecutivas y de archivo.';
    if (/arquitect|diseñ|diseñ|interior|moda|arte|urban/.test(name)) return 'Para diseño y arquitectura se priorizan sala, interiores, moda y estudio.';
    if (/ingenier|sistem|software|comput|datos|industrial/.test(name)) return 'Para ingeniería y tecnología se priorizan composiciones técnicas, de producto y de tablero.';
    if (/admin|negoci|econom|market|contab|gesti/.test(name)) return 'Para administración se priorizan composiciones de negocio, marca y dirección.';
    return 'Elige una carrera para ordenar las diez composiciones según tu disciplina.';
  }
  private forCareer(items: TemplateCard[], name: string): TemplateCard[] {
    const text = name.toLowerCase();
    const order = /medic|salud|enferm|odonto|nutri|psicol|farmac|biolog|veterin/.test(text)
      ? ['academic', 'gazette', 'editorial', 'interior', 'exhibit', 'premium', 'executive', 'minimal', 'corporate', 'studio']
      : /derech|legal|jurid/.test(text)
        ? ['editorial', 'gazette', 'executive', 'academic', 'corporate', 'premium', 'minimal', 'exhibit', 'studio', 'interior']
        : /arquitect|diseñ|disen|interior|moda|arte|urban/.test(text)
          ? ['exhibit', 'interior', 'fashion', 'creative', 'studio', 'screen', 'premium', 'editorial', 'experimental', 'dark']
          : /ingenier|sistem|software|comput|datos|industrial/.test(text)
            ? ['tech', 'screen', 'experimental', 'corporate', 'studio', 'premium', 'executive', 'dark', 'minimal', 'gazette']
            : /admin|negoci|econom|market|contab|gesti/.test(text)
              ? ['corporate', 'executive', 'premium', 'studio', 'tech', 'minimal', 'editorial', 'gazette', 'academic', 'exhibit']
              : ['minimal', 'corporate', 'editorial', 'academic', 'executive', 'tech', 'premium', 'studio', 'exhibit', 'gazette'];
    const ranked = [...items].sort((a, b) => {
      const left = order.indexOf(a.layout);
      const right = order.indexOf(b.layout);
      return (left < 0 ? 99 : left) - (right < 0 ? 99 : right);
    });
    const seen = new Set<string>();
    return ranked.filter((item) => {
      if (seen.has(item.layout)) return false;
      seen.add(item.layout);
      return order.includes(item.layout);
    }).slice(0, 10);
  }
  rank(layout: string): string { return this.ranks[layout] ?? layout; }
  pin(item: TemplateCard): void {
    const current = this.compared();
    this.compared.set(current.includes(item) ? current.filter((entry) => entry !== item) : [...current, item].slice(-2));
  }
  apply(templateId: string): void {
    this.api.applyTemplate(templateId).subscribe({
      next: () => { void this.router.navigateByUrl('/app/editor'); },
      error: (err) => { this.failed.set(true); this.message.set(errorText(err)); }
    });
  }
}

@Component({
  selector: 'app-editor',
  imports: [FormsModule, FolioView, RouterLink, Steps],
  template: `
    <app-steps />
    <p class="page-kicker">Composición</p>
    <h1 class="page-title">Editor</h1>
    @if (publicUrl()) { <p class="site-link">Tu sitio <a [href]="publicUrl()" target="_blank" rel="noopener noreferrer">{{ publicUrl() }}</a></p> }
    <p class="note">{{ status() || 'Los cambios de texto se guardan solos.' }}</p>
    <div class="device-switch">
      <button class="btn-ghost" type="button" (click)="device.set('desktop')">Escritorio</button>
      <button class="btn-ghost" type="button" (click)="device.set('tablet')">Tablet</button>
      <button class="btn-ghost" type="button" (click)="device.set('phone')">Celular</button>
      <button class="btn" type="button" (click)="saveNow()">Guardar</button>
    </div>
    <div class="editor">
      <div class="list">
        @for (section of sections(); track section.sectionKey; let index = $index) {
          <article class="card">
            <label>Título<input [ngModel]="section.title" (ngModelChange)="rename(section, $event)"></label>
            <label class="check"><input type="checkbox" [ngModel]="section.visible" (ngModelChange)="toggle(section, $event)"><span>Visible</span></label>
            @if (section.component === 'richtext') {
              <label>Texto<textarea [ngModel]="text(section)" (ngModelChange)="write(section, 'body', $event)"></textarea></label>
              @if (bilingual()) { <label>Texto en inglés<textarea [ngModel]="text(section, 'bodyEn')" (ngModelChange)="write(section, 'bodyEn', $event)"></textarea></label> }
            }
            @if (section.component === 'areas' || section.component === 'services' || section.component === 'achievements') {
              <label>Elementos, uno por línea: título | descripción<textarea [ngModel]="itemsText(section)" (ngModelChange)="writeItems(section, $event)"></textarea></label>
            }
            <div class="actions">
              <button class="btn-ghost" type="button" (click)="move(index, -1)">Subir</button>
              <button class="btn-ghost" type="button" (click)="move(index, 1)">Bajar</button>
              <button class="btn-danger" type="button" (click)="remove(section)">Quitar</button>
            </div>
          </article>
        } @empty { <p class="empty">Completa tu carrera para generar secciones.</p> }
        <h2>Versiones</h2>
        @for (item of versions(); track item.id) {
          <article class="card row"><span>{{ item.label }} · {{ item.createdAt }}</span><button class="btn-ghost" type="button" (click)="restore(item.id)">Restaurar</button></article>
        }
        <button class="btn-ghost" type="button" (click)="snapshot()">Guardar versión</button>
      </div>
      <div class="preview-frame" [class.phone]="device() === 'phone'" [class.tablet]="device() === 'tablet'">
        <app-folio-view [view]="preview()" />
      </div>
    </div>
  `
})
export class EditorPage {
  private readonly api = inject(ApiService);
  readonly sections = signal<SectionState[]>([]);
  readonly versions = signal<VersionItem[]>([]);
  readonly preview = signal<PortfolioView | null>(null);
  readonly device = signal('desktop');
  readonly status = signal('');
  readonly publicUrl = signal('');
  readonly bilingual = signal(false);
  private version = 0;
  private timer: ReturnType<typeof setTimeout> | undefined;

  constructor() { this.load(); }

  text(section: SectionState, field = 'body'): string {
    const value = section.content?.[field];
    return typeof value === 'string' ? value : '';
  }

  itemsText(section: SectionState): string {
    const items = section.content?.['items'];
    if (!Array.isArray(items)) return '';
    return items.map((item) => {
      const row = item as { title?: string; description?: string };
      return `${row.title ?? ''} | ${row.description ?? ''}`;
    }).join('\n');
  }

  rename(section: SectionState, title: string): void { section.title = title; this.queue(); }
  toggle(section: SectionState, visible: boolean): void { section.visible = visible; this.queue(); }
  write(section: SectionState, field: string, value: string): void {
    section.content = { ...(section.content ?? {}), [field]: value };
    this.queue();
  }
  writeItems(section: SectionState, value: string): void {
    const items = value.split('\n').map((line) => line.trim()).filter(Boolean).map((line) => {
      const [title, description] = line.split('|');
      return { title: (title ?? '').trim(), description: (description ?? '').trim() };
    });
    section.content = { ...(section.content ?? {}), items };
    this.queue();
  }
  move(index: number, delta: number): void {
    const next = index + delta;
    const copy = [...this.sections()];
    if (next < 0 || next >= copy.length) return;
    const [item] = copy.splice(index, 1);
    copy.splice(next, 0, item);
    copy.forEach((section, order) => section.sortOrder = order);
    this.sections.set(copy);
    this.queue();
  }
  remove(section: SectionState): void {
    this.api.deleteSection(section.sectionKey).subscribe({ next: () => this.load(), error: (err) => this.status.set(errorText(err)) });
  }
  saveNow(): void { this.persist(); }
  snapshot(): void { this.api.version('Versión manual').subscribe({ next: () => this.load(), error: (err) => this.status.set(errorText(err)) }); }
  restore(id: string): void { this.api.restore(id).subscribe({ next: () => this.load(), error: (err) => this.status.set(errorText(err)) }); }

  private queue(): void {
    clearTimeout(this.timer);
    this.timer = setTimeout(() => this.persist(), 1200);
  }

  private persist(): void {
    this.api.saveSections({
      version: this.version,
      sections: this.sections().map((section) => ({
        key: section.sectionKey, title: section.title, visible: section.visible, sortOrder: section.sortOrder, content: section.content ?? {}
      }))
    }).subscribe({
      next: (summary) => {
        const version = (summary as { version?: number }).version;
        if (typeof version === 'number') this.version = version;
        this.status.set('Guardado');
        this.api.preview().subscribe((view) => this.preview.set(view));
      },
      error: (err) => this.status.set(errorText(err))
    });
  }

  private load(): void {
    this.api.editor().subscribe((state) => {
      this.sections.set(state.sections.map((section) => ({ ...section, content: section.content ?? {} })));
      this.versions.set(state.versions);
      this.version = state.portfolio.version;
      this.bilingual.set(state.portfolio.bilingual);
      this.publicUrl.set(state.portfolio.publicUrl ?? '');
    });
    this.api.preview().subscribe((view) => this.preview.set(view));
    this.api.dashboard().subscribe({ next: (item) => this.publicUrl.set(item.publicUrl ?? ''), error: () => undefined });
  }
}

import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { DomSanitizer, SafeUrl } from '@angular/platform-browser';
import { ApiService, ContactMessage, Notice } from '../core/api.service';
import { AuthService, errorText } from '../core/auth.service';

@Component({
  selector: 'app-publish',
  imports: [ReactiveFormsModule],
  template: `
    <h1>Publicación</h1>
    <p class="note">El sitio solo queda público si confirmas la publicación. Un borrador no se abre cambiando la dirección.</p>
    <form [formGroup]="form" (ngSubmit)="publish()">
      <label>Identificador de la URL<input formControlName="slug" (blur)="checkSlug()"></label>
      @if (slugState()) { <p>{{ slugState() }}</p> }
      <label>Título SEO<input formControlName="seoTitle"></label>
      <label>Descripción SEO<textarea formControlName="seoDescription"></textarea></label>
      <label>Color de acento<input formControlName="accent" type="color"></label>
      <div class="grid-2">
        <label>Tipografía de títulos
          <select formControlName="fontHeading">
            @for (font of fonts; track font) { <option [value]="font">{{ font }}</option> }
          </select>
        </label>
        <label>Tipografía de texto
          <select formControlName="fontBody">
            @for (font of fonts; track font) { <option [value]="font">{{ font }}</option> }
          </select>
        </label>
      </div>
      <label class="check"><input type="checkbox" formControlName="bilingual"><span>Mostrar botón de español e inglés</span></label>
      <label class="check"><input type="checkbox" formControlName="whatsappEnabled"><span>Botón de WhatsApp con el teléfono del perfil</span></label>
      <label class="check"><input type="checkbox" formControlName="showEmail"><span>Mostrar correo</span></label>
      <label class="check"><input type="checkbox" formControlName="showPhone"><span>Mostrar teléfono</span></label>
      <label class="check"><input type="checkbox" formControlName="analyticsEnabled"><span>Permitir visitas anónimas con consentimiento del visitante</span></label>
      <label class="check"><input type="checkbox" formControlName="recruiterViewEnabled"><span>Ofrecer modo reclutador</span></label>
      <label class="check"><input type="checkbox" formControlName="showcaseOptIn"><span>Permitir que aparezca en ejemplos</span></label>
      <label class="check"><input type="checkbox" formControlName="acceptPublication"><span>Publico este portafolio de forma consciente</span></label>
      @if (message()) { <p [class.error]="failed()">{{ message() }}</p> }
      <button class="btn" type="submit">Publicar</button>
      <button class="btn-danger" type="button" (click)="unpublish()">Despublicar</button>
    </form>
    @if (publicUrl()) { <p>Dirección pública: <a [href]="publicUrl()" target="_blank" rel="noopener noreferrer">{{ publicUrl() }}</a></p> }
    @if (qr(); as image) { <img [src]="image" alt="Código QR del portafolio" width="180" height="180"> }
    <h2>Dominio propio</h2>
    <p class="note">Puedes registrar tu interés. Esta versión guarda el dominio, pero todavía no dirige el tráfico hacia él.</p>
    <form [formGroup]="domainForm" (ngSubmit)="saveDomain()">
      <label>Dominio<input formControlName="domain" placeholder="tuportafolio.com"></label>
      <button class="btn-ghost" type="submit">Registrar interés</button>
    </form>
  `
})
export class PublishPage {
  private readonly api = inject(ApiService);
  private readonly forms = inject(FormBuilder);
  private readonly sanitizer = inject(DomSanitizer);
  readonly fonts = ['Fraunces', 'Outfit', 'Libre Baskerville', 'Source Sans 3', 'Space Grotesk', 'IBM Plex Sans', 'Newsreader', 'Sora', 'Manrope', 'Source Serif 4', 'Cormorant Garamond', 'IBM Plex Mono'];
  readonly message = signal('');
  readonly failed = signal(false);
  readonly slugState = signal('');
  readonly publicUrl = signal('');
  readonly qr = signal<SafeUrl | null>(null);
  private version = 0;
  private theme: Record<string, unknown> = {};
  readonly form = this.forms.nonNullable.group({
    slug: ['', Validators.required], seoTitle: [''], seoDescription: [''], accent: ['#0f5c45'], fontHeading: ['Fraunces'], fontBody: ['Outfit'],
    bilingual: [false], whatsappEnabled: [false], showEmail: [false], showPhone: [false], analyticsEnabled: [false], recruiterViewEnabled: [true], showcaseOptIn: [false], acceptPublication: [false]
  });
  readonly domainForm = this.forms.nonNullable.group({ domain: [''] });

  constructor() {
    this.api.editor().subscribe((state) => {
      const portfolio = state.portfolio;
      this.version = portfolio.version;
      this.theme = { ...(portfolio.theme ?? {}) };
      this.publicUrl.set(portfolio.publicUrl ?? '');
      this.form.patchValue({
        slug: portfolio.slug ?? '', seoTitle: portfolio.seoTitle ?? '', seoDescription: portfolio.seoDescription ?? '',
        accent: String(portfolio.theme?.accent ?? '#0f5c45'), fontHeading: portfolio.theme?.fontHeading ?? 'Fraunces', fontBody: portfolio.theme?.fontBody ?? 'Outfit',
        bilingual: portfolio.bilingual, whatsappEnabled: portfolio.whatsappEnabled, showEmail: portfolio.showEmail, showPhone: portfolio.showPhone,
        analyticsEnabled: portfolio.analyticsEnabled, recruiterViewEnabled: portfolio.recruiterViewEnabled, showcaseOptIn: portfolio.showcaseOptIn
      });
      if (portfolio.slug && portfolio.status === 'PUBLISHED') this.loadQr();
    });
  }

  checkSlug(): void {
    const slug = this.form.controls.slug.value.trim();
    if (!slug) return;
    this.api.slug(slug).subscribe({
      next: (result) => this.slugState.set(result.available ? `${result.slug} está disponible.` : `${result.slug} no está disponible.`),
      error: (err) => this.slugState.set(errorText(err))
    });
  }

  publish(): void {
    if (!this.form.controls.acceptPublication.value) {
      this.failed.set(true);
      this.message.set('Marca la confirmación para publicar.');
      return;
    }
    const value = this.form.getRawValue();
    this.api.saveSettings({
      version: this.version, title: undefined, theme: { ...this.theme, accent: value.accent, fontHeading: value.fontHeading, fontBody: value.fontBody },
      bilingual: value.bilingual, whatsappEnabled: value.whatsappEnabled, showEmail: value.showEmail, showPhone: value.showPhone,
      analyticsEnabled: value.analyticsEnabled, recruiterViewEnabled: value.recruiterViewEnabled, showcaseOptIn: value.showcaseOptIn,
      seoTitle: value.seoTitle, seoDescription: value.seoDescription
    }).subscribe({
      next: (summary) => {
        const version = (summary as { version?: number }).version;
        if (typeof version === 'number') this.version = version;
        this.api.publish({ slug: value.slug, acceptPublication: true, seoTitle: value.seoTitle, seoDescription: value.seoDescription }).subscribe({
          next: (result) => {
            const url = (result as { publicUrl?: string }).publicUrl ?? '';
            this.publicUrl.set(url);
            this.failed.set(false);
            this.message.set('Portafolio publicado.');
            this.loadQr();
          },
          error: (err) => { this.failed.set(true); this.message.set(errorText(err)); }
        });
      },
      error: (err) => { this.failed.set(true); this.message.set(errorText(err)); }
    });
  }

  unpublish(): void {
    this.api.unpublish().subscribe({
      next: () => { this.publicUrl.set(''); this.qr.set(null); this.failed.set(false); this.message.set('El portafolio ya no es público.'); },
      error: (err) => { this.failed.set(true); this.message.set(errorText(err)); }
    });
  }

  saveDomain(): void {
    this.api.domain(this.domainForm.controls.domain.value).subscribe({
      next: () => { this.failed.set(false); this.message.set('Interés de dominio registrado. Aún no enruta visitas.'); },
      error: (err) => { this.failed.set(true); this.message.set(errorText(err)); }
    });
  }

  private loadQr(): void {
    this.api.qr().subscribe((blob) => this.qr.set(this.sanitizer.bypassSecurityTrustUrl(URL.createObjectURL(blob))));
  }
}

@Component({
  selector: 'app-messages',
  template: `
    <h1>Mensajes y avisos</h1>
    <div class="list">
      @for (item of messages(); track item.id) { <article class="card"><strong>{{ item.name }}</strong><p>{{ item.email }}</p><p>{{ item.message }}</p></article> }
      @if (!messages().length) { <p class="empty">No hay mensajes de contacto.</p> }
      @for (item of notices(); track item.id) { <article class="card"><strong>{{ item.title }}</strong><p>{{ item.body }}</p></article> }
    </div>
  `
})
export class MessagesPage {
  private readonly api = inject(ApiService);
  readonly messages = signal<ContactMessage[]>([]);
  readonly notices = signal<Notice[]>([]);
  constructor() {
    this.api.messages().subscribe((items) => this.messages.set(items));
    this.api.notifications().subscribe((items) => this.notices.set(items));
  }
}

@Component({
  selector: 'app-admin',
  imports: [ReactiveFormsModule],
  template: `
    <h1>Administración</h1>
    @if (reports(); as item) {
      <div class="stats">
        <article class="stat"><b>{{ item['users'] }}</b> personas</article>
        <article class="stat"><b>{{ item['careers'] }}</b> carreras</article>
        <article class="stat"><b>{{ item['faculties'] }}</b> facultades</article>
        <article class="stat"><b>{{ item['portfolios'] }}</b> portafolios</article>
      </div>
    }
    @if (auth.hasRole('ADMIN')) {
      <h2>Nueva facultad</h2>
      <form [formGroup]="facultyForm" (ngSubmit)="createFaculty()">
        <div class="grid-2"><label>Código<input formControlName="code"></label><label>Nombre<input formControlName="name"></label></div>
        <button class="btn" type="submit">Crear facultad</button>
      </form>
      <h2>Nueva carrera</h2>
      <form [formGroup]="careerForm" (ngSubmit)="createCareer()">
        <div class="grid-2"><label>Código<input formControlName="code"></label><label>Nombre<input formControlName="name"></label></div>
        <label>Facultad
          <select formControlName="facultyId"><option value="">Sin asociar</option>@for (item of faculties(); track item.id) { <option [value]="item.id">{{ item.name }}</option> }</select>
        </label>
        <button class="btn" type="submit">Crear carrera</button>
      </form>
      <h2>Nueva universidad</h2>
      <form [formGroup]="universityForm" (ngSubmit)="createUniversity()">
        <label>Nombre<input formControlName="name"></label>
        <label>Ciudad<input formControlName="city"></label>
        <button class="btn" type="submit">Crear universidad</button>
      </form>
      <h2>Personas</h2>
      <div class="list">
        @for (user of users(); track user.id) {
          <article class="card row">
            <span>{{ user.firstName }} {{ user.lastName }} · {{ user.email }}</span>
            <button class="btn-ghost" type="button" (click)="toggle(user.id, !user.enabled)">{{ user.enabled ? 'Desactivar' : 'Activar' }}</button>
          </article>
        }
      </div>
    }
    <h2>Moderación</h2>
    <form [formGroup]="moderationForm" (ngSubmit)="moderate()">
      <label>Identificador del portafolio<input formControlName="id"></label>
      <label>Motivo<input formControlName="reason"></label>
      <button class="btn-danger" type="submit">Despublicar</button>
    </form>
    @if (message()) { <p>{{ message() }}</p> }
  `
})
export class AdminPage {
  private readonly api = inject(ApiService);
  private readonly forms = inject(FormBuilder);
  readonly auth = inject(AuthService);
  readonly reports = signal<Record<string, number> | null>(null);
  readonly users = signal<{ id: string; email: string; firstName: string; lastName: string; enabled: boolean }[]>([]);
  readonly faculties = signal<{ id: string; name: string }[]>([]);
  readonly message = signal('');
  readonly facultyForm = this.forms.nonNullable.group({ code: [''], name: [''] });
  readonly careerForm = this.forms.nonNullable.group({ code: [''], name: [''], facultyId: [''] });
  readonly universityForm = this.forms.nonNullable.group({ name: [''], city: [''] });
  readonly moderationForm = this.forms.nonNullable.group({ id: [''], reason: [''] });

  constructor() {
    if (this.auth.hasRole('ADMIN')) {
      this.api.adminReports().subscribe((item) => this.reports.set(item));
      this.api.adminUsers().subscribe((page) => this.users.set(page.content));
      this.api.faculties().subscribe((items) => this.faculties.set(items));
    }
  }

  createFaculty(): void {
    this.api.createFaculty(this.facultyForm.getRawValue()).subscribe({ next: () => this.message.set('Facultad creada.'), error: (err) => this.message.set(errorText(err)) });
  }
  createCareer(): void {
    const value = this.careerForm.getRawValue();
    this.api.createCareer({ ...value, facultyId: value.facultyId || null }).subscribe({ next: () => this.message.set('Carrera creada. Su configuración visual se puede ajustar sin cambiar código.'), error: (err) => this.message.set(errorText(err)) });
  }
  createUniversity(): void {
    this.api.createUniversity(this.universityForm.getRawValue()).subscribe({ next: () => this.message.set('Universidad creada.'), error: (err) => this.message.set(errorText(err)) });
  }
  toggle(id: string, enabled: boolean): void {
    this.api.setUserEnabled(id, enabled).subscribe({ next: () => this.api.adminUsers().subscribe((page) => this.users.set(page.content)), error: (err) => this.message.set(errorText(err)) });
  }
  moderate(): void {
    this.api.moderate(this.moderationForm.controls.id.value, this.moderationForm.controls.reason.value).subscribe({
      next: () => this.message.set('Portafolio despublicado.'), error: (err) => this.message.set(errorText(err))
    });
  }
}

import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ApiService, CertItem, CourseItem, DocumentItem, ExperienceItem, Extraction, ProjectItem, PublicationItem, SkillItem } from '../core/api.service';
import { errorText } from '../core/auth.service';

function blank(value: string | null | undefined): string | null {
  const trimmed = (value ?? '').trim();
  return trimmed ? trimmed : null;
}

@Component({
  selector: 'app-studio',
  imports: [ReactiveFormsModule],
  template: `
    <h1>Contenido del portafolio</h1>
    <div class="tabs" role="tablist">
      @for (item of tabs; track item.id) {
        <button type="button" role="tab" [attr.aria-selected]="tab() === item.id" (click)="tab.set(item.id)">{{ item.label }}</button>
      }
    </div>
    @if (message()) { <p [class.error]="failed()">{{ message() }}</p> }

    @if (tab() === 'projects') {
      <form [formGroup]="projectForm" (ngSubmit)="saveProject()">
        <label>Nombre<input formControlName="name"></label>
        <label>Descripción<textarea formControlName="description"></textarea></label>
        <div class="grid-2">
          <label>Categoría<input formControlName="category"></label>
          <label>Rol<input formControlName="roleName"></label>
        </div>
        <label>Tecnologías, separadas por coma<input formControlName="technologies"></label>
        <label>Resultados<textarea formControlName="results"></textarea></label>
        <label>Nota de arquitectura<textarea formControlName="architectureNote"></textarea></label>
        <div class="grid-2">
          <label>Repositorio<input formControlName="github"></label>
          <label>Demostración<input formControlName="demo"></label>
        </div>
        <label>Estado
          <select formControlName="publicationStatus"><option value="PUBLISHED">Visible</option><option value="DRAFT">Borrador</option></select>
        </label>
        <button class="btn" type="submit">Añadir proyecto</button>
      </form>
      <div class="list">
        @for (item of projects(); track item.id) {
          <article class="card row"><div><strong>{{ item.name }}</strong><p>{{ item.category }} · {{ item.publicationStatus }}</p></div><button class="btn-danger" type="button" (click)="removeProject(item.id)">Eliminar</button></article>
        } @empty { <p class="empty">Todavía no hay proyectos.</p> }
      </div>
    }

    @if (tab() === 'courses') {
      <form [formGroup]="courseForm" (ngSubmit)="saveCourse()">
        <label>Nombre<input formControlName="name"></label>
        <label>Institución<input formControlName="institution"></label>
        <div class="grid-2">
          <label>Horas<input formControlName="hours" inputmode="numeric"></label>
          <label>Estado<select formControlName="status"><option value="COMPLETADO">Completado</option><option value="EN_CURSO">En curso</option><option value="PLANIFICADO">Planificado</option></select></label>
        </div>
        <label>Descripción<textarea formControlName="description"></textarea></label>
        <button class="btn" type="submit">Añadir curso</button>
      </form>
      <div class="list">
        @for (item of courses(); track item.id) {
          <article class="card row"><div><strong>{{ item.name }}</strong><p>{{ item.institution }} · {{ item.status }}</p></div><button class="btn-danger" type="button" (click)="removeCourse(item.id)">Eliminar</button></article>
        } @empty { <p class="empty">Todavía no hay cursos.</p> }
      </div>
    }

    @if (tab() === 'certs') {
      <form [formGroup]="certForm" (ngSubmit)="saveCert()">
        <label>Nombre<input formControlName="name"></label>
        <label>Institución emisora<input formControlName="issuer"></label>
        <label>Fecha<input formControlName="issuedOn" type="date"></label>
        <label>Descripción<textarea formControlName="description"></textarea></label>
        <button class="btn" type="submit">Añadir certificación</button>
      </form>
      <div class="list">
        @for (item of certs(); track item.id) {
          <article class="card"><strong>{{ item.name }}</strong><p>{{ item.issuer }}</p><p class="note">Queda registrada como información confirmada por ti. No está autenticada por la plataforma.</p></article>
        } @empty { <p class="empty">Todavía no hay certificaciones.</p> }
      </div>
    }

    @if (tab() === 'skills') {
      <form [formGroup]="skillForm" (ngSubmit)="saveSkill()">
        <label>Habilidad<input formControlName="name"></label>
        <label>Categoría
          <select formControlName="category">
            <option value="TECHNICAL">Técnica</option>
            <option value="PROFESSIONAL">Profesional</option>
            <option value="SOFT">Blanda</option>
            <option value="TOOL">Herramienta</option>
            <option value="ACADEMIC">Académica</option>
          </select>
        </label>
        <label>Nota<input formControlName="note"></label>
        <button class="btn" type="submit">Añadir habilidad</button>
      </form>
      <p class="note">No se muestran porcentajes de dominio.</p>
      <div class="chips">
        @for (item of skills(); track item.id) {
          <button class="chip" type="button" (click)="removeSkill(item.id)">{{ item.name }} · quitar</button>
        }
      </div>
    }

    @if (tab() === 'experience') {
      <form [formGroup]="experienceForm" (ngSubmit)="saveExperience()">
        <label>Título<input formControlName="title"></label>
        <label>Organización<input formControlName="organization"></label>
        <label>Tipo
          <select formControlName="kind">
            <option value="ACADEMIC">Académica</option>
            <option value="PREPROFESSIONAL">Preprofesional</option>
            <option value="VOLUNTEER">Voluntariado</option>
            <option value="RESEARCH">Investigación</option>
            <option value="FORMATIVE">Formativa</option>
            <option value="WORK">Laboral</option>
          </select>
        </label>
        <label>Descripción<textarea formControlName="description"></textarea></label>
        <button class="btn" type="submit">Añadir experiencia</button>
      </form>
      <div class="list">
        @for (item of experiences(); track item.id) { <article class="card"><strong>{{ item.title }}</strong><p>{{ item.organization }} · {{ item.kind }}</p></article> }
      </div>
    }

    @if (tab() === 'publications') {
      <form [formGroup]="publicationForm" (ngSubmit)="savePublication()">
        <label>Título<input formControlName="title"></label>
        <label>Autores<input formControlName="authors"></label>
        <label>Medio<input formControlName="venue"></label>
        <label>Tipo
          <select formControlName="kind">
            <option value="ARTICLE">Artículo</option>
            <option value="CONFERENCE">Congreso</option>
            <option value="THESIS">Tesis</option>
            <option value="CASE_ANALYSIS">Análisis de caso</option>
            <option value="OTHER">Otro</option>
          </select>
        </label>
        <label>Enlace<input formControlName="url"></label>
        <label>Resumen<textarea formControlName="summary"></textarea></label>
        <button class="btn" type="submit">Añadir publicación</button>
      </form>
      <div class="list">
        @for (item of publications(); track item.id) { <article class="card"><strong>{{ item.title }}</strong><p>{{ item.kind }}</p></article> }
      </div>
    }
  `
})
export class StudioPage {
  private readonly api = inject(ApiService);
  private readonly forms = inject(FormBuilder);
  readonly tabs = [
    { id: 'projects', label: 'Proyectos' }, { id: 'courses', label: 'Cursos' }, { id: 'certs', label: 'Certificaciones' },
    { id: 'skills', label: 'Habilidades' }, { id: 'experience', label: 'Experiencia' }, { id: 'publications', label: 'Publicaciones' }
  ];
  readonly tab = signal('projects');
  readonly projects = signal<ProjectItem[]>([]);
  readonly courses = signal<CourseItem[]>([]);
  readonly certs = signal<CertItem[]>([]);
  readonly skills = signal<SkillItem[]>([]);
  readonly experiences = signal<ExperienceItem[]>([]);
  readonly publications = signal<PublicationItem[]>([]);
  readonly message = signal('');
  readonly failed = signal(false);
  readonly projectForm = this.forms.nonNullable.group({
    name: ['', Validators.required], description: [''], category: [''], roleName: [''], technologies: [''], results: [''], architectureNote: [''], github: [''], demo: [''], publicationStatus: ['PUBLISHED']
  });
  readonly courseForm = this.forms.nonNullable.group({ name: ['', Validators.required], institution: [''], hours: [''], status: ['COMPLETADO'], description: [''] });
  readonly certForm = this.forms.nonNullable.group({ name: ['', Validators.required], issuer: [''], issuedOn: [''], description: [''] });
  readonly skillForm = this.forms.nonNullable.group({ name: ['', Validators.required], category: ['TECHNICAL'], note: [''] });
  readonly experienceForm = this.forms.nonNullable.group({ title: ['', Validators.required], organization: [''], kind: ['ACADEMIC'], description: [''] });
  readonly publicationForm = this.forms.nonNullable.group({ title: ['', Validators.required], authors: [''], venue: [''], kind: ['ARTICLE'], url: [''], summary: [''] });

  constructor() { this.reload(); }

  saveProject(): void {
    const value = this.projectForm.getRawValue();
    const links: Record<string, string> = {};
    if (blank(value.github)) links['github'] = value.github.trim();
    if (blank(value.demo)) links['demo'] = value.demo.trim();
    this.api.saveProject({
      name: value.name, description: blank(value.description), category: blank(value.category), roleName: blank(value.roleName),
      results: blank(value.results), architectureNote: blank(value.architectureNote), links, participants: [],
      technologies: value.technologies.split(',').map((item) => item.trim()).filter(Boolean), publicationStatus: value.publicationStatus
    }).subscribe({ next: () => { this.ok('Proyecto guardado.'); this.projectForm.reset({ publicationStatus: 'PUBLISHED' }); this.reload(); }, error: (err) => this.fail(err) });
  }

  removeProject(id: string): void { this.api.deleteProject(id).subscribe({ next: () => this.reload(), error: (err) => this.fail(err) }); }
  saveCourse(): void {
    const value = this.courseForm.getRawValue();
    this.api.saveCourse({ name: value.name, institution: blank(value.institution), hours: value.hours ? Number(value.hours) : null, status: value.status, description: blank(value.description) })
      .subscribe({ next: () => { this.ok('Curso guardado.'); this.courseForm.reset({ status: 'COMPLETADO' }); this.reload(); }, error: (err) => this.fail(err) });
  }
  removeCourse(id: string): void { this.api.deleteCourse(id).subscribe({ next: () => this.reload(), error: (err) => this.fail(err) }); }
  saveCert(): void {
    const value = this.certForm.getRawValue();
    this.api.saveCertification({ name: value.name, issuer: blank(value.issuer), issuedOn: blank(value.issuedOn), description: blank(value.description) })
      .subscribe({ next: () => { this.ok('Certificación guardada.'); this.certForm.reset(); this.reload(); }, error: (err) => this.fail(err) });
  }
  saveSkill(): void {
    const value = this.skillForm.getRawValue();
    this.api.addSkill({ name: value.name, category: value.category, note: blank(value.note) ?? '' }).subscribe({ next: () => { this.ok('Habilidad guardada.'); this.skillForm.reset({ category: 'TECHNICAL' }); this.reload(); }, error: (err) => this.fail(err) });
  }
  removeSkill(id: string): void { this.api.removeSkill(id).subscribe({ next: () => this.reload(), error: (err) => this.fail(err) }); }
  saveExperience(): void {
    const value = this.experienceForm.getRawValue();
    this.api.saveExperience({ title: value.title, organization: blank(value.organization), kind: value.kind, description: blank(value.description) })
      .subscribe({ next: () => { this.ok('Experiencia guardada.'); this.experienceForm.reset({ kind: 'ACADEMIC' }); this.reload(); }, error: (err) => this.fail(err) });
  }
  savePublication(): void {
    const value = this.publicationForm.getRawValue();
    this.api.savePublication({ title: value.title, authors: blank(value.authors), venue: blank(value.venue), url: blank(value.url), kind: value.kind, summary: blank(value.summary) })
      .subscribe({ next: () => { this.ok('Publicación guardada.'); this.publicationForm.reset({ kind: 'ARTICLE' }); this.reload(); }, error: (err) => this.fail(err) });
  }

  private reload(): void {
    this.api.projects().subscribe((items) => this.projects.set(items));
    this.api.courses().subscribe((items) => this.courses.set(items));
    this.api.certifications().subscribe((items) => this.certs.set(items));
    this.api.skills().subscribe((items) => this.skills.set(items));
    this.api.experiences().subscribe((items) => this.experiences.set(items));
    this.api.publications().subscribe((items) => this.publications.set(items));
  }
  private ok(text: string): void { this.failed.set(false); this.message.set(text); }
  private fail(err: unknown): void { this.failed.set(true); this.message.set(errorText(err)); }
}

@Component({
  selector: 'app-documents',
  imports: [ReactiveFormsModule],
  template: `
    <h1>Documentos</h1>
    <p class="note">Los archivos quedan privados, salvo la fotografía de perfil o los que marques como visibles. La extracción solo propone datos: debes revisarlos antes de guardarlos.</p>
    <form [formGroup]="form">
      <label>Archivo<input type="file" (change)="choose($event)"></label>
      <label>Tipo
        <select formControlName="category">
          <option value="CV">CV</option><option value="CERTIFICATE">Certificado</option><option value="DIPLOMA">Diploma</option>
          <option value="CONSTANCIA">Constancia</option><option value="TRANSCRIPT">Historial</option><option value="PROJECT">Proyecto</option>
          <option value="PRESENTATION">Presentación</option><option value="RESEARCH">Investigación</option><option value="PUBLICATION">Publicación</option>
          <option value="IMAGE">Imagen</option><option value="OTHER">Otro</option>
        </select>
      </label>
      <label class="check"><input type="checkbox" formControlName="visibleOnPortfolio"><span>Permitir que este archivo se vea en el sitio público</span></label>
      <button class="btn" type="button" (click)="upload()">Subir</button>
    </form>
    @if (message()) { <p [class.error]="failed()">{{ message() }}</p> }
    <div class="list">
      @for (item of documents(); track item.id) {
        <article class="card">
          <strong>{{ item.originalName }}</strong>
          <p>{{ item.category }} · {{ item.visibility }} · análisis {{ item.scanStatus }}</p>
          <button class="btn-ghost" type="button" (click)="process(item.id)">Extraer datos</button>
        </article>
      } @empty { <p class="empty">No hay documentos.</p> }
    </div>
    @if (extraction(); as item) {
      <form class="card" [formGroup]="review" (ngSubmit)="confirm()">
        <h2>Revisión</h2>
        <p>{{ suggestionText() }}</p>
        <label>Guardar como
          <select formControlName="target"><option value="COURSE">Curso</option><option value="CERTIFICATION">Certificación</option><option value="NONE">Solo confirmar, sin crear ficha</option></select>
        </label>
        <label>Nombre<input formControlName="name"></label>
        <label>Institución<input formControlName="institution"></label>
        <label>Fecha<input formControlName="date" type="date"></label>
        <label>Horas<input formControlName="hours" inputmode="numeric"></label>
        <label>Descripción<textarea formControlName="description"></textarea></label>
        <button class="btn" type="submit">Confirmar datos revisados</button>
      </form>
    }
  `
})
export class DocumentsPage {
  private readonly api = inject(ApiService);
  private readonly forms = inject(FormBuilder);
  readonly documents = signal<DocumentItem[]>([]);
  readonly extraction = signal<Extraction | null>(null);
  readonly message = signal('');
  readonly failed = signal(false);
  readonly suggestionText = signal('');
  private file: File | null = null;
  private documentId = '';
  readonly form = this.forms.nonNullable.group({ category: ['CERTIFICATE'], visibleOnPortfolio: [false] });
  readonly review = this.forms.nonNullable.group({ target: ['COURSE'], name: ['', Validators.required], institution: [''], date: [''], hours: [''], description: [''] });

  constructor() { this.reload(); }

  choose(event: Event): void { this.file = (event.target as HTMLInputElement).files?.[0] ?? null; }

  upload(): void {
    if (!this.file) { this.failMessage('Selecciona un archivo.'); return; }
    const body = new FormData();
    body.set('file', this.file);
    body.set('category', this.form.controls.category.value);
    body.set('visibleOnPortfolio', String(this.form.controls.visibleOnPortfolio.value));
    this.api.upload(body).subscribe({ next: () => { this.ok('Documento guardado.'); this.reload(); }, error: (err) => this.fail(err) });
  }

  process(id: string): void {
    this.documentId = id;
    this.api.process(id).subscribe({
      next: (item) => {
        this.extraction.set(item);
        const suggestion = item.suggestion ?? {};
        const notes = Array.isArray(suggestion['notes']) ? (suggestion['notes'] as string[]).join(' ') : '';
        this.suggestionText.set(`${suggestion['authenticity'] ?? ''} ${suggestion['textSample'] ?? ''} ${notes}`.trim());
        this.review.patchValue({
          institution: String(suggestion['suggestedInstitution'] ?? ''),
          hours: suggestion['suggestedHours'] ? String(suggestion['suggestedHours']) : '',
          description: ''
        });
        this.ok('Revisa la sugerencia antes de guardarla.');
      },
      error: (err) => this.fail(err)
    });
  }

  confirm(): void {
    const current = this.extraction();
    if (!current || this.review.invalid) { this.review.markAllAsTouched(); return; }
    const value = this.review.getRawValue();
    this.api.confirm(this.documentId, current.id, {
      target: value.target, name: value.name, institution: blank(value.institution), date: blank(value.date),
      hours: value.hours ? Number(value.hours) : null, description: blank(value.description)
    }).subscribe({ next: () => { this.ok('Información confirmada por ti.'); this.extraction.set(null); }, error: (err) => this.fail(err) });
  }

  private reload(): void { this.api.documents().subscribe((items) => this.documents.set(items)); }
  private ok(text: string): void { this.failed.set(false); this.message.set(text); }
  private fail(err: unknown): void { this.failed.set(true); this.message.set(errorText(err)); }
  private failMessage(text: string): void { this.failed.set(true); this.message.set(text); }
}

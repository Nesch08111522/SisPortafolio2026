import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { Academic, ApiService, CareerDetail, Country, Dashboard, Faculty, Named, PlaceOption, ProfileResponse, WorldCareer } from '../core/api.service';
import { Choice, SearchSelect } from './search-select';
import { Steps } from './steps';
import { errorText } from '../core/auth.service';

function blank(value: string | null | undefined): string | null {
  const trimmed = (value ?? '').trim();
  return trimmed ? trimmed : null;
}

@Component({
  selector: 'app-dashboard',
  imports: [RouterLink, Steps],
  template: `
    <app-steps />
    <header class="hub-hero">
      <p class="page-kicker">Escritorio</p>
      <h1 class="page-title">Bienvenido{{ data()?.firstName ? ', ' + data()?.firstName : '' }}.</h1>
      <p class="lede">Este es tu sistema de gestión. Los datos de tu perfil, tu ubicación y tu carrera se mantienen en el encabezado de todas las secciones.</p>
    </header>
    @if (data(); as item) {
      <p class="hub-meta"><strong>{{ item.career || 'Carrera aún no elegida' }}</strong>@if (item.location) { <span>{{ item.location }}</span> }</p>
      <div class="hub-complete">
        <p>Perfil completado al {{ item.completion }} %</p>
        <span>{{ statusLabel(item.publicationStatus) }}</span>
      </div>
      <div class="progress" aria-label="Avance del perfil"><span [style.width.%]="item.completion"></span></div>
      <div class="stats">
        <article class="stat"><small>Métrica 01</small><b>{{ item.visits }}</b> visitas</article>
        <a class="stat" routerLink="/app/contenido"><small>Métrica 02</small><b>{{ item.projects }}</b> proyectos</a>
        <a class="stat" routerLink="/app/academico"><small>Métrica 03</small><b>{{ item.courses }}</b> cursos</a>
        <a class="stat" routerLink="/app/academico"><small>Métrica 04</small><b>{{ item.certifications }}</b> certificaciones</a>
      </div>
      <div class="list">
        @for (tip of item.recommendations; track tip) { <p class="card">{{ tip }}</p> }
        @if (!item.recommendations.length) { <p class="card">El perfil tiene los datos mínimos. Revisa la vista previa antes de publicar.</p> }
      </div>
      <p class="actions"><a class="btn" routerLink="/app/academico">Elegir facultad y carrera</a> <a class="btn-ghost" routerLink="/app/disenos">Ver diseños</a></p>
    }
  `
})
export class DashboardPage {
  private readonly api = inject(ApiService);
  readonly data = signal<Dashboard | null>(null);
  constructor() { this.api.dashboard().subscribe((item) => this.data.set(item)); }
  statusLabel(value?: string): string {
    if (value === 'PUBLISHED') return 'Publicado';
    if (value === 'UNPUBLISHED') return 'No publicado';
    return 'Borrador';
  }
}

@Component({
  selector: 'app-profile',
  imports: [ReactiveFormsModule, SearchSelect],
  template: `
    <p class="page-kicker">Identidad</p>
    <h1 class="page-title">Perfil personal y profesional</h1>
    <form [formGroup]="form" (ngSubmit)="save()">
      <div class="grid-2">
        <label>Nombres<input formControlName="firstName"></label>
        <label>Apellidos<input formControlName="lastName"></label>
      </div>
      <label>Biografía<textarea formControlName="bio"></textarea></label>
      <label>Biografía en inglés<textarea formControlName="bioEn"></textarea></label>
      <div class="grid-2">
        <label>Fecha de nacimiento<input formControlName="birthDate" type="date"></label>
        <label class="check"><input type="checkbox" formControlName="showBirthDate"><span>Mostrar la fecha en el sitio público</span></label>
      </div>
      <label>Fotografía profesional<input type="file" accept="image/png,image/jpeg,image/webp" (change)="photo($event)"></label>
      <div class="grid-2">
        <label>País
          <app-search-select placeholder="Busca tu país" [options]="countryChoices()" [value]="form.controls.countryId.value" (valueChange)="pickCountry($event)" />
        </label>
        <label>Región o departamento
          <app-search-select placeholder="Busca la región" [options]="regionChoices()" [value]="form.controls.customRegion.value" (valueChange)="pickRegion($event)" />
        </label>
      </div>
      <div class="grid-2">
        <label>Provincia
          <app-search-select placeholder="Busca la provincia" [options]="provinceChoices()" [value]="form.controls.customProvince.value" (valueChange)="pickPlace('customProvince', $event)" />
        </label>
        <label>Ciudad
          <app-search-select placeholder="Busca la ciudad" [options]="cityChoices()" [value]="form.controls.customCity.value" (valueChange)="pickPlace('customCity', $event)" />
        </label>
      </div>
      <label>Distrito
        <app-search-select placeholder="Busca el distrito" [options]="districtChoices()" [value]="form.controls.customDistrict.value" (valueChange)="pickPlace('customDistrict', $event)" />
      </label>
      <p class="note">Si tu localidad no aparece, escríbela. El texto manual se usa cuando no hay una opción de catálogo.</p>
      <div class="grid-2">
        <label>Región manual<input formControlName="customRegion"></label>
        <label>Provincia manual<input formControlName="customProvince"></label>
        <label>Ciudad manual<input formControlName="customCity"></label>
        <label>Distrito manual<input formControlName="customDistrict"></label>
      </div>
      <label>Dirección opcional<input formControlName="address"></label>
      <div class="grid-2">
        <label>Teléfono<input formControlName="phone"></label>
        <label>Correo profesional<input formControlName="professionalEmail" type="email"></label>
      </div>
      <div class="grid-2">
        <label>LinkedIn<input formControlName="linkedin"></label>
        <label>GitHub<input formControlName="github"></label>
        <label>GitLab<input formControlName="gitlab"></label>
        <label>Behance<input formControlName="behance"></label>
        <label>Dribbble<input formControlName="dribbble"></label>
        <label>ORCID<input formControlName="orcid"></label>
        <label>Google Scholar<input formControlName="googleScholar"></label>
        <label>Sitio web<input formControlName="website"></label>
      </div>
      @if (message()) { <p [class.error]="failed()">{{ message() }}</p> }
      <button class="btn" type="submit">Guardar perfil</button>
    </form>
  `
})
export class ProfilePage {
  private readonly api = inject(ApiService);
  private readonly forms = inject(FormBuilder);
  readonly countries = signal<Country[]>([]);
  readonly placeRegions = signal<PlaceOption[]>([]);
  readonly placeProvinces = signal<PlaceOption[]>([]);
  readonly placeCities = signal<PlaceOption[]>([]);
  readonly placeDistricts = signal<PlaceOption[]>([]);
  readonly message = signal('');
  readonly failed = signal(false);
  readonly form = this.forms.nonNullable.group({
    firstName: ['', Validators.required], lastName: ['', Validators.required], bio: [''], bioEn: [''], birthDate: [''], showBirthDate: [false],
    countryId: [''], regionId: [''], provinceId: [''], cityId: [''], districtId: [''],
    customRegion: [''], customProvince: [''], customCity: [''], customDistrict: [''], address: [''], phone: [''], professionalEmail: [''],
    linkedin: [''], github: [''], gitlab: [''], behance: [''], dribbble: [''], orcid: [''], googleScholar: [''], website: ['']
  });

  constructor() {
    this.api.countries().subscribe((items) => {
      this.countries.set(items);
      this.api.profile().subscribe((profile) => this.fill(profile));
    });
  }

  countryChoices(): Choice[] { return this.countries().map((item) => ({ id: item.id, label: item.name })); }
  regionChoices(): Choice[] { return this.placeRegions().map((item) => ({ id: item.id, label: item.name, hint: item.hint || 'Región' })); }
  provinceChoices(): Choice[] { return this.placeProvinces().map((item) => ({ id: item.id, label: item.name, hint: item.hint || 'Provincia' })); }
  cityChoices(): Choice[] { return this.placeCities().map((item) => ({ id: item.id, label: item.name, hint: item.hint || 'Ciudad' })); }
  districtChoices(): Choice[] { return this.placeDistricts().map((item) => ({ id: item.id, label: item.name, hint: item.hint || 'Distrito' })); }

  pickCountry(id: string): void {
    this.form.patchValue({ countryId: id, regionId: '', provinceId: '', cityId: '', districtId: '', customRegion: '', customProvince: '', customCity: '', customDistrict: '' });
    this.placeProvinces.set([]);
    this.placeCities.set([]);
    this.placeDistricts.set([]);
    this.loadPlaces();
  }

  pickRegion(id: string): void {
    this.form.patchValue({ regionId: '', customRegion: id, provinceId: '', cityId: '', districtId: '', customProvince: '', customCity: '', customDistrict: '' });
    this.loadPlaces(id);
  }

  pickPlace(field: 'customProvince' | 'customCity' | 'customDistrict', id: string): void {
    this.form.controls[field].setValue(id);
    if (field === 'customProvince') this.form.controls.provinceId.setValue('');
    if (field === 'customCity') this.form.controls.cityId.setValue('');
    if (field === 'customDistrict') this.form.controls.districtId.setValue('');
  }

  loadPlaces(region = this.form.controls.customRegion.value): void {
    const country = this.countries().find((item) => item.id === this.form.controls.countryId.value);
    if (!country) return;
    this.api.worldPlaces(country.name, region).subscribe((places) => {
      this.placeRegions.set(places.regions ?? []);
      this.placeProvinces.set(places.provinces ?? []);
      this.placeCities.set(places.cities ?? []);
      this.placeDistricts.set(places.districts ?? []);
    });
  }

  photo(event: Event): void {
    const file = (event.target as HTMLInputElement).files?.[0];
    if (!file) return;
    const body = new FormData();
    body.set('file', file);
    body.set('category', 'IMAGE');
    body.set('profilePhoto', 'true');
    this.api.upload(body).subscribe({
      next: () => { this.failed.set(false); this.message.set('Fotografía guardada. Se publica con el portafolio.'); },
      error: (err) => { this.failed.set(true); this.message.set(errorText(err)); }
    });
  }

  save(): void {
    const value = this.form.getRawValue();
    this.api.saveProfile({
      ...value,
      birthDate: blank(value.birthDate),
      countryId: blank(value.countryId), regionId: blank(value.regionId), provinceId: blank(value.provinceId),
      cityId: blank(value.cityId), districtId: blank(value.districtId),
      customRegion: blank(value.customRegion), customProvince: blank(value.customProvince), customCity: blank(value.customCity), customDistrict: blank(value.customDistrict),
      address: blank(value.address), phone: blank(value.phone), professionalEmail: blank(value.professionalEmail), bio: blank(value.bio), bioEn: blank(value.bioEn),
      linkedin: blank(value.linkedin), github: blank(value.github), gitlab: blank(value.gitlab), behance: blank(value.behance),
      dribbble: blank(value.dribbble), orcid: blank(value.orcid), googleScholar: blank(value.googleScholar), website: blank(value.website),
      otherLinks: []
    }).subscribe({
      next: () => { this.failed.set(false); this.message.set('Perfil guardado.'); },
      error: (err) => { this.failed.set(true); this.message.set(errorText(err)); }
    });
  }

  private fill(profile: ProfileResponse): void {
    const personal = profile.personal ?? {};
    const professional = profile.professional ?? {};
    this.form.patchValue({
      firstName: profile.firstName, lastName: profile.lastName,
      bio: personal.bio ?? '', bioEn: personal.bioEn ?? '', birthDate: personal.birthDate ?? '', showBirthDate: personal.showBirthDate,
      countryId: personal.countryId ?? '', regionId: personal.regionId ?? '', provinceId: personal.provinceId ?? '', cityId: personal.cityId ?? '', districtId: personal.districtId ?? '',
      customRegion: personal.customRegion ?? '', customProvince: personal.customProvince ?? '', customCity: personal.customCity ?? '', customDistrict: personal.customDistrict ?? '',
      address: personal.address ?? '', phone: personal.phone ?? '', professionalEmail: personal.professionalEmail ?? '',
      linkedin: professional.linkedin ?? '', github: professional.github ?? '', gitlab: professional.gitlab ?? '', behance: professional.behance ?? '',
      dribbble: professional.dribbble ?? '', orcid: professional.orcid ?? '', googleScholar: professional.googleScholar ?? '', website: professional.website ?? ''
    });
    this.loadPlaces();
  }
}

@Component({
  selector: 'app-academic',
  imports: [ReactiveFormsModule, SearchSelect, Steps],
  template: `
    <app-steps />
    <p class="page-kicker">Carrera</p>
    <h1 class="page-title">Facultad, carrera y ciclo</h1>
    <p class="note">Primero la facultad. Después aparecen sus carreras. Indica el ciclo o semestre y guarda. La carrera cambia las secciones del sitio y conserva el texto que ya escribiste.</p>
    <form [formGroup]="form" (ngSubmit)="save()">
      <p class="note">{{ facultyMark() }} Universidad del catálogo local y, si escribes al menos dos letras, también de {{ countryName() || 'tu país' }}.</p>
      <label>Universidad
        <app-search-select placeholder="Busca la universidad y el país" [options]="universityChoices()" [value]="form.controls.universityId.value" (queryChange)="searchUniversities($event)" (valueChange)="chooseUniversity($event)" />
      </label>
      <label>Facultad
        <span class="faculty-row">
          <svg class="faculty-mark" viewBox="0 0 24 24" aria-hidden="true"><path [attr.d]="facultyIcon()" /></svg>
          <app-search-select placeholder="Busca la facultad" [options]="facultyChoices()" [value]="form.controls.facultyId.value" (valueChange)="pickFaculty($event)" />
        </span>
      </label>
      <label>Carrera universitaria
        <app-search-select placeholder="Busca la carrera universitaria" [options]="careerChoices()" [value]="form.controls.careerId.value" (queryChange)="searchCareers($event)" (valueChange)="pickCareer($event)" />
      </label>
      <label>Carrera técnica
        <app-search-select placeholder="Busca la carrera técnica" [options]="technicalChoices()" [value]="technicalValue()" (queryChange)="searchTechnical($event)" (valueChange)="pickTechnical($event)" />
      </label>
      @if (careerDetail(); as detail) {
        <p class="note">{{ detail.description || detail.name }} @if (detail.iscedCode) { Clasificación ISCED {{ detail.iscedCode }}. } País de referencia: {{ countryName() || 'el que elegiste al crear la cuenta' }}.</p>
      }
      <label>Especialización
        <select formControlName="specializationId">
          <option value="">Sin especialización</option>
          @for (item of specializations(); track item.id) { <option [value]="item.id">{{ item.name }}</option> }
        </select>
      </label>
      <div class="grid-2">
        <label>Ciclo o semestre<input formControlName="semester"></label>
        <label>Estado
          <select formControlName="status">
            <option value="ESTUDIANTE">Estudiante</option>
            <option value="EGRESADO">Egresado</option>
            <option value="BACHILLER">Bachiller</option>
            <option value="TITULADO">Titulado</option>
            <option value="PROFESIONAL">Profesional</option>
            <option value="POSGRADO">Estudios de posgrado</option>
          </select>
        </label>
        <label>Año de ingreso<input formControlName="admissionYear" inputmode="numeric"></label>
        <label>Egreso estimado<input formControlName="expectedGraduation" type="date"></label>
      </div>
      <label>Cursos relevantes<textarea formControlName="relevantCourses"></textarea></label>
      <label>Áreas de interés<textarea formControlName="interests"></textarea></label>
      <label>Objetivos profesionales<textarea formControlName="professionalGoals"></textarea></label>
      <label class="check"><input type="checkbox" formControlName="licenseDeclared"><span>Declaro una habilitación profesional. La plataforma no la verifica ni convierte a un estudiante en profesional colegiado.</span></label>
      @if (message()) { <p [class.error]="failed()">{{ message() }}</p> }
      <button class="btn" type="submit">Guardar formación</button>
    </form>
  `
})
export class AcademicPage {
  private readonly api = inject(ApiService);
  private readonly forms = inject(FormBuilder);
  private readonly router = inject(Router);
  readonly faculties = signal<Faculty[]>([]);
  readonly careers = signal<{ id: string; name: string }[]>([]);
  readonly specializations = signal<Named[]>([]);
  readonly universities = signal<Named[]>([]);
  readonly worldUniversities = signal<{ name: string; country: string }[]>([]);
  readonly countryName = signal('');
  readonly careerDetail = signal<CareerDetail | null>(null);
  readonly technical = signal<WorldCareer[]>([]);
  readonly message = signal('');
  readonly failed = signal(false);
  readonly form = this.forms.nonNullable.group({
    universityQuery: [''], universityId: [''], universityName: [''], facultyId: [''], careerQuery: [''], careerId: [''], careerName: [''], careerKind: [''], specializationId: [''],
    semester: [''], status: ['ESTUDIANTE', Validators.required], admissionYear: [''], expectedGraduation: [''],
    relevantCourses: [''], interests: [''], professionalGoals: [''], licenseDeclared: [false]
  });

  constructor() {
    this.api.faculties().subscribe((items) => this.faculties.set(items));
    this.api.countries().subscribe((countries) => {
      this.api.profile().subscribe({
        next: (profile) => {
          const found = countries.find((item) => item.id === profile.personal?.countryId);
          this.countryName.set(found?.name ?? '');
          this.applyAcademic(profile);
        },
        error: () => this.loadCareers()
      });
    });
  }

  private applyAcademic(profile: ProfileResponse): void {
      const academic: Academic | null = profile.academic;
      if (!academic) {
        this.loadCareers();
        return;
      }
      this.form.patchValue({
        universityId: academic.universityId ?? '', universityName: academic.universityName ?? '', facultyId: academic.facultyId ?? '',
        careerId: academic.careerId ?? '', careerName: academic.careerName ?? '', careerKind: academic.careerKind ?? '', specializationId: academic.specializationId ?? '', semester: academic.semester ?? '',
        status: academic.status ?? 'ESTUDIANTE', admissionYear: academic.admissionYear ? String(academic.admissionYear) : '',
        expectedGraduation: academic.expectedGraduation ?? '', relevantCourses: academic.relevantCourses ?? '',
        interests: academic.interests ?? '', professionalGoals: academic.professionalGoals ?? '', licenseDeclared: academic.licenseDeclared
      });
      this.loadCareers();
      if (academic.careerKind === 'TECHNICAL' && academic.careerName) {
        this.technical.set([{ id: 'esco:saved', name: academic.careerName, kind: 'Técnica', hint: 'Técnica' }]);
      }
      if (academic.careerId) {
        this.loadSpecializations();
        this.api.career(academic.careerId).subscribe((item) => this.careerDetail.set(item));
      }
      if (academic.facultyId) this.searchTechnical(this.technicalSeed(this.faculties().find((item) => item.id === academic.facultyId)?.name ?? ''));
  }

  facultyChoices(): Choice[] {
    return this.faculties().map((item) => ({ id: item.id, label: item.name }));
  }

  careerChoices(): Choice[] {
    const faculty = this.faculties().find((item) => item.id === this.form.controls.facultyId.value);
    return this.careers().map((item) => ({ id: item.id, label: item.name, hint: faculty ? `Universitaria · ${faculty.name}` : 'Universitaria' }));
  }

  technicalChoices(): Choice[] {
    return this.technical().map((item) => ({ id: item.id, label: item.name, hint: item.hint || 'Técnica' }));
  }

  technicalValue(): string {
    if (this.form.controls.careerKind.value !== 'TECHNICAL') return '';
    return this.technical().find((item) => item.name === this.form.controls.careerName.value)?.id ?? '';
  }

  technicalSeed(name: string): string {
    if (/salud|médic|medic|enferm|odont/i.test(name)) return 'técnico en salud';
    if (/ingenier|sistema|comput/i.test(name)) return 'técnico en ingeniería';
    if (/derecho|jurid/i.test(name)) return 'técnico jurídico';
    if (/arquitect|diseñ|arte/i.test(name)) return 'técnico en diseño';
    if (/negoci|admin|econom/i.test(name)) return 'técnico en administración';
    if (/educa/i.test(name)) return 'técnico en educación';
    return name.trim().split(' ')[0] || 'técnico';
  }

  universityChoices(): Choice[] {
    const local = this.universities().map((item) => ({ id: item.id, label: item.name, hint: 'Catálogo' }));
    const world = this.worldUniversities().map((item) => ({ id: `world:${item.name}`, label: item.name, hint: item.country }));
    const savedName = this.form.controls.universityName.value;
    const savedId = this.form.controls.universityId.value || (savedName ? `world:${savedName}` : '');
    const saved = savedName && ![...local, ...world].some((item) => item.id === savedId || item.label === savedName)
      ? [{ id: savedId, label: savedName, hint: this.countryName() || 'Guardada' }]
      : [];
    return [...saved, ...local, ...world];
  }

  facultyIcon(): string {
    const name = this.faculties().find((item) => item.id === this.form.controls.facultyId.value)?.name ?? '';
    if (/salud|médic|medic|enferm|odont/i.test(name)) return 'M8 4h8v3a4 4 0 0 1-8 0V4zm-2 2H4v2a8 8 0 0 0 16 0V6h-2';
    if (/ingenier|sistema|comput/i.test(name)) return 'M4 6h16v10H4zM8 20h8M12 16v4';
    if (/derecho|jurid/i.test(name)) return 'M12 3v18M4 7h16M6 7l-2 6h6L8 7M16 7l-2 6h6l-2-6';
    if (/arquitect|diseñ|arte/i.test(name)) return 'M4 20l8-16 8 16H4zm4.5-4h7';
    if (/negoci|admin|econom/i.test(name)) return 'M4 18V9h4v9M10 18V5h4v13M16 18v-7h4v7';
    return 'M5 5h14v14H5zM8 9h8M8 13h5';
  }

  facultyMark(): string {
    const name = this.faculties().find((item) => item.id === this.form.controls.facultyId.value)?.name ?? '';
    if (/salud|médic|medic/i.test(name)) return 'Salud.';
    if (/ingenier/i.test(name)) return 'Ingeniería.';
    if (/derecho|jurid/i.test(name)) return 'Derecho.';
    if (/arquitect|diseñ|arte/i.test(name)) return 'Proyecto visual.';
    if (/negoci|admin|econom/i.test(name)) return 'Gestión.';
    return 'Formación.';
  }

  pickFaculty(id: string): void {
    this.form.controls.facultyId.setValue(id);
    this.form.patchValue({ careerId: '', careerName: '', careerKind: '' });
    this.careerDetail.set(null);
    this.loadCareers();
    const faculty = this.faculties().find((item) => item.id === id);
    this.searchTechnical(this.technicalSeed(faculty?.name ?? ''));
  }

  pickCareer(id: string): void {
    const career = this.careers().find((item) => item.id === id);
    this.form.patchValue({ careerId: id, careerName: career?.name ?? '', careerKind: id ? 'UNIVERSITY' : '' });
    this.loadSpecializations();
    if (id) this.api.career(id).subscribe((item) => this.careerDetail.set(item));
    else this.careerDetail.set(null);
  }

  pickTechnical(id: string): void {
    const item = this.technical().find((entry) => entry.id === id);
    this.form.patchValue({ careerId: '', specializationId: '', careerName: item?.name ?? '', careerKind: item ? 'TECHNICAL' : '' });
    this.careerDetail.set(null);
    this.specializations.set([]);
  }

  searchTechnical(query: string): void {
    if (query.trim().length < 2) {
      this.technical.set([]);
      return;
    }
    this.api.worldCareers(query, 'technical').subscribe({
      next: (items) => {
        const saved = this.form.controls.careerKind.value === 'TECHNICAL' ? this.form.controls.careerName.value : '';
        const extra = saved && !items.some((item) => item.name === saved) ? [{ id: 'esco:saved', name: saved, kind: 'Técnica', hint: 'Técnica' }] : [];
        this.technical.set([...extra, ...items]);
      },
      error: () => this.technical.set([])
    });
  }

  searchCareers(query: string): void {
    this.form.controls.careerQuery.setValue(query);
    this.loadCareers();
  }

  searchUniversities(query: string): void {
    this.api.universities(query).subscribe((page) => this.universities.set(page.content));
    const country = this.countryName();
    if (country && query.trim().length >= 2) {
      this.api.worldUniversities(country, query).subscribe({ next: (items) => this.worldUniversities.set(items), error: () => this.worldUniversities.set([]) });
    }
  }

  chooseUniversity(id: string): void {
    const world = this.worldUniversities().find((item) => `world:${item.name}` === id);
    if (world) {
      this.form.patchValue({ universityId: '', universityName: world.name });
      return;
    }
    const local = this.universities().find((item) => item.id === id);
    this.form.patchValue({ universityId: local?.id ?? '', universityName: local?.name ?? '' });
  }

  loadCareers(): void {
    const facultyId = blank(this.form.controls.facultyId.value) ?? undefined;
    this.api.careers(facultyId, this.form.controls.careerQuery.value).subscribe((page) => this.careers.set(page.content));
  }

  loadSpecializations(): void {
    const careerId = this.form.controls.careerId.value;
    this.specializations.set([]);
    if (careerId) this.api.career(careerId).subscribe((career) => this.specializations.set(career.specializations));
  }

  save(): void {
    const value = this.form.getRawValue();
    if (!value.careerId && !value.careerName) {
      this.failed.set(true);
      this.message.set('Selecciona una carrera universitaria o técnica para continuar con los archivos.');
      return;
    }
    const year = Number(value.admissionYear);
    this.api.saveAcademic({
      universityId: blank(value.universityId),
      universityName: blank(value.universityName),
      facultyId: blank(value.facultyId),
      careerId: blank(value.careerId),
      careerName: blank(value.careerName),
      careerKind: blank(value.careerKind),
      specializationId: blank(value.specializationId),
      semester: blank(value.semester),
      admissionYear: value.admissionYear && Number.isInteger(year) ? year : null,
      expectedGraduation: blank(value.expectedGraduation),
      status: value.status,
      relevantCourses: blank(value.relevantCourses),
      interests: blank(value.interests),
      professionalGoals: blank(value.professionalGoals),
      licenseDeclared: value.licenseDeclared
    }).subscribe({
      next: () => { void this.router.navigateByUrl('/app/documentos'); },
      error: (err) => { this.failed.set(true); this.message.set(errorText(err)); }
    });
  }
}

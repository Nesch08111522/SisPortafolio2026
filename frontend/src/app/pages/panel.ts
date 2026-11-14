import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { Academic, ApiService, Country, Dashboard, Faculty, Named, ProfileResponse } from '../core/api.service';
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
    <p class="page-kicker">Panel</p>
    <h1 class="page-title">Tu sitio</h1>
    <p class="lede">Para continuar, elige facultad y carrera, sube tus archivos y selecciona uno de los diez diseños.</p>
    @if (data(); as item) {
      <div class="progress" aria-label="Avance del perfil"><span [style.width.%]="item.completion"></span></div>
      <p class="note">Perfil completado al {{ item.completion }} %. Estado: {{ item.publicationStatus }}.</p>
      <div class="stats">
        <article class="stat"><b>{{ item.visits }}</b> visitas</article>
        <article class="stat"><b>{{ item.projects }}</b> proyectos</article>
        <article class="stat"><b>{{ item.courses }}</b> cursos</article>
        <article class="stat"><b>{{ item.certifications }}</b> certificaciones</article>
      </div>
      @if (item.publicUrl) {
        <p class="site-link">Tu sitio <a [href]="item.publicUrl" target="_blank" rel="noopener noreferrer">{{ item.publicUrl }}</a></p>
      }
      <div class="list" style="margin-top:1rem">
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
}

@Component({
  selector: 'app-profile',
  imports: [ReactiveFormsModule],
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
          <select formControlName="countryId" (change)="loadRegions()">
            <option value="">Selecciona</option>
            @for (item of countries(); track item.id) { <option [value]="item.id">{{ item.name }}</option> }
          </select>
        </label>
        <label>Región o departamento
          <select formControlName="regionId" (change)="loadProvinces()">
            <option value="">Sin catálogo</option>
            @for (item of regions(); track item.id) { <option [value]="item.id">{{ item.name }}</option> }
          </select>
        </label>
      </div>
      <div class="grid-2">
        <label>Provincia
          <select formControlName="provinceId">
            <option value="">Sin catálogo</option>
            @for (item of provinces(); track item.id) { <option [value]="item.id">{{ item.name }}</option> }
          </select>
        </label>
        <label>Ciudad
          <select formControlName="cityId" (change)="loadDistricts()">
            <option value="">Sin catálogo</option>
            @for (item of cities(); track item.id) { <option [value]="item.id">{{ item.name }}</option> }
          </select>
        </label>
      </div>
      <label>Distrito
        <select formControlName="districtId">
          <option value="">Sin catálogo</option>
          @for (item of districts(); track item.id) { <option [value]="item.id">{{ item.name }}</option> }
        </select>
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
  readonly regions = signal<Named[]>([]);
  readonly provinces = signal<Named[]>([]);
  readonly cities = signal<Named[]>([]);
  readonly districts = signal<Named[]>([]);
  readonly message = signal('');
  readonly failed = signal(false);
  readonly form = this.forms.nonNullable.group({
    firstName: ['', Validators.required], lastName: ['', Validators.required], bio: [''], bioEn: [''], birthDate: [''], showBirthDate: [false],
    countryId: [''], regionId: [''], provinceId: [''], cityId: [''], districtId: [''],
    customRegion: [''], customProvince: [''], customCity: [''], customDistrict: [''], address: [''], phone: [''], professionalEmail: [''],
    linkedin: [''], github: [''], gitlab: [''], behance: [''], dribbble: [''], orcid: [''], googleScholar: [''], website: ['']
  });

  constructor() {
    this.api.countries().subscribe((items) => this.countries.set(items));
    this.api.profile().subscribe((profile) => this.fill(profile));
  }

  loadRegions(): void {
    const countryId = this.form.controls.countryId.value;
    this.regions.set([]);
    this.cities.set([]);
    if (!countryId) return;
    this.api.regions(countryId).subscribe((items) => this.regions.set(items));
    this.api.cities(countryId).subscribe((items) => this.cities.set(items));
  }

  loadProvinces(): void {
    const regionId = this.form.controls.regionId.value;
    this.provinces.set([]);
    if (regionId) this.api.provinces(regionId).subscribe((items) => this.provinces.set(items));
  }

  loadDistricts(): void {
    const cityId = this.form.controls.cityId.value;
    this.districts.set([]);
    if (cityId) this.api.districts(cityId).subscribe((items) => this.districts.set(items));
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
    this.loadRegions();
    this.loadProvinces();
    this.loadDistricts();
  }
}

@Component({
  selector: 'app-academic',
  imports: [ReactiveFormsModule, Steps],
  template: `
    <app-steps />
    <p class="page-kicker">Carrera</p>
    <h1 class="page-title">Facultad, carrera y ciclo</h1>
    <p class="note">Primero la facultad. Después aparecen sus carreras. Indica el ciclo o semestre y guarda. La carrera cambia las secciones del sitio y conserva el texto que ya escribiste.</p>
    <form [formGroup]="form" (ngSubmit)="save()">
      <label>Buscar universidad<input formControlName="universityQuery"></label>
      <button class="btn-ghost" type="button" (click)="searchUniversities()">Buscar</button>
      <div class="chips">
        @for (item of universities(); track item.id) {
          <button class="chip" type="button" (click)="chooseUniversity(item)">{{ item.name }}</button>
        }
      </div>
      <label>Universidad, si no está en el catálogo<input formControlName="universityName"></label>
      <label>Facultad o área
        <select formControlName="facultyId" (change)="loadCareers()">
          <option value="">Todas</option>
          @for (item of faculties(); track item.id) { <option [value]="item.id">{{ item.name }}</option> }
        </select>
      </label>
      <label>Buscar carrera<input formControlName="careerQuery" (input)="loadCareers()"></label>
      <label>Carrera
        <select formControlName="careerId" (change)="loadSpecializations()">
          <option value="">Selecciona</option>
          @for (item of careers(); track item.id) { <option [value]="item.id">{{ item.name }}</option> }
        </select>
      </label>
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
  readonly message = signal('');
  readonly failed = signal(false);
  readonly form = this.forms.nonNullable.group({
    universityQuery: [''], universityId: [''], universityName: [''], facultyId: [''], careerQuery: [''], careerId: [''], specializationId: [''],
    semester: [''], status: ['ESTUDIANTE', Validators.required], admissionYear: [''], expectedGraduation: [''],
    relevantCourses: [''], interests: [''], professionalGoals: [''], licenseDeclared: [false]
  });

  constructor() {
    this.api.faculties().subscribe((items) => this.faculties.set(items));
    this.api.profile().subscribe((profile) => {
      const academic: Academic | null = profile.academic;
      if (!academic) {
        this.loadCareers();
        return;
      }
      this.form.patchValue({
        universityId: academic.universityId ?? '', universityName: academic.universityName ?? '', facultyId: academic.facultyId ?? '',
        careerId: academic.careerId ?? '', specializationId: academic.specializationId ?? '', semester: academic.semester ?? '',
        status: academic.status ?? 'ESTUDIANTE', admissionYear: academic.admissionYear ? String(academic.admissionYear) : '',
        expectedGraduation: academic.expectedGraduation ?? '', relevantCourses: academic.relevantCourses ?? '',
        interests: academic.interests ?? '', professionalGoals: academic.professionalGoals ?? '', licenseDeclared: academic.licenseDeclared
      });
      this.loadCareers();
      if (academic.careerId) this.loadSpecializations();
    });
  }

  searchUniversities(): void {
    this.api.universities(this.form.controls.universityQuery.value).subscribe((page) => this.universities.set(page.content));
  }

  chooseUniversity(item: Named): void {
    this.form.patchValue({ universityId: item.id, universityName: item.name });
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
    if (!value.careerId) {
      this.failed.set(true);
      this.message.set('Selecciona una carrera para continuar con los archivos.');
      return;
    }
    const year = Number(value.admissionYear);
    this.api.saveAcademic({
      universityId: blank(value.universityId),
      universityName: blank(value.universityName),
      facultyId: blank(value.facultyId),
      careerId: blank(value.careerId),
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

import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { ApiService, Country, PlaceOption } from '../core/api.service';
import { Choice, SearchSelect } from './search-select';
import { AuthService, errorText } from '../core/auth.service';
import { EmeraldStage } from './emerald-stage';

@Component({
  selector: 'app-register',
  imports: [ReactiveFormsModule, RouterLink, SearchSelect, EmeraldStage],
  template: `
    <main class="emerald" id="contenido">
      <app-emerald-stage variant="register" kicker="Tu futuro comienza aquí" title="Construye tu identidad. Proyecta tu talento." lede="No necesitas años de experiencia para demostrar lo que eres capaz de hacer." />
      <section class="emerald-form">
        <div class="emerald-card">
          <div class="form-head">
            <p class="eyebrow">Registro</p>
            <p class="step-label">Paso {{ step() }} / 02</p>
          </div>
          <div class="progress" aria-hidden="true"><span [style.width]="step() === 1 ? '50%' : '100%'"></span></div>
          @if (step() === 1) {
            <h1>Crea tu cuenta.</h1>
            <p class="lede">Comencemos con tus datos principales. El país y tu trayectoria se completan después.</p>
            <form [formGroup]="account" (ngSubmit)="createAccount()" novalidate>
              <p class="field-legend">Información personal</p>
              <div class="grid-2">
                <label>Nombres<input formControlName="firstName" autocomplete="given-name" placeholder="Tus nombres" [attr.aria-invalid]="invalid(account, 'firstName')"></label>
                <label>Apellidos<input formControlName="lastName" autocomplete="family-name" placeholder="Tus apellidos" [attr.aria-invalid]="invalid(account, 'lastName')"></label>
              </div>
              <label>Correo electrónico<input formControlName="email" type="email" autocomplete="email" placeholder="nombre@correo.com" [attr.aria-invalid]="invalid(account, 'email')"></label>
              @if (invalid(account, 'email')) { <p class="error" role="alert">Escribe un correo válido.</p> }
              <label>Contraseña
                <span class="secret">
                  <input formControlName="password" [type]="show() ? 'text' : 'password'" autocomplete="new-password" placeholder="Mínimo 10 caracteres" [attr.aria-invalid]="invalid(account, 'password')">
                  <button type="button" class="eye" (click)="show.set(!show())" [attr.aria-pressed]="show()" [attr.aria-label]="show() ? 'Ocultar contraseña' : 'Mostrar contraseña'">
                    <svg viewBox="0 0 24 24" aria-hidden="true"><path d="M2 12s3.5-7 10-7 10 7 10 7-3.5 7-10 7S2 12 2 12Z"/><circle cx="12" cy="12" r="3"/></svg>
                  </button>
                </span>
              </label>
              <p class="strength" [attr.data-score]="strength()">Fortaleza: {{ strengthLabel() }}</p>
              <label>Confirmar contraseña<input formControlName="confirmPassword" [type]="show() ? 'text' : 'password'" autocomplete="new-password" placeholder="Repite la contraseña" [attr.aria-invalid]="mismatch()"></label>
              @if (mismatch()) { <p class="error" role="alert">Las contraseñas no coinciden.</p> }
              <label class="check"><input type="checkbox" formControlName="acceptTerms"><span>Acepto los <a routerLink="/terminos">términos y condiciones</a>.</span></label>
              @if (message()) { <p class="error" role="alert">{{ message() }}</p> }
              <button class="btn wide" type="submit" [disabled]="busy()">{{ busy() ? 'Creando cuenta…' : 'Crear mi cuenta' }}</button>
            </form>
            <p class="switch-link">¿Ya tienes cuenta? <a routerLink="/iniciar-sesion">Iniciar sesión</a></p>
          } @else {
            <h1>Tu perfil.</h1>
            <p class="lede">Estos datos aparecen en tu sitio. Puedes completarlos ahora o seguir hacia la facultad.</p>
            <form [formGroup]="profile" (ngSubmit)="saveProfile()" novalidate>
              <label>País
                <app-search-select placeholder="Busca tu país" [options]="countryChoices()" [value]="profile.controls.countryCode.value" (valueChange)="pickCountry($event)" />
              </label>
              <label>Región o departamento
                <app-search-select placeholder="Busca tu región" [options]="placeChoices(regions())" [value]="profile.controls.region.value" (valueChange)="pickRegion($event)" />
              </label>
              <label>Ciudad
                <app-search-select placeholder="Busca tu ciudad" [options]="placeChoices(cities())" [value]="profile.controls.city.value" (valueChange)="profile.controls.city.setValue($event)" />
              </label>
              <label>Distrito, cuando corresponda
                <app-search-select placeholder="Busca tu distrito" [options]="placeChoices(districts())" [value]="profile.controls.district.value" (valueChange)="profile.controls.district.setValue($event)" />
              </label>
              <label>Si no aparece, escríbela<input formControlName="manualPlace" placeholder="Ciudad o distrito"></label>
              <label>Teléfono internacional<input formControlName="phone" autocomplete="tel" placeholder="+51 999 000 000"></label>
              <label>LinkedIn<input formControlName="linkedin" autocomplete="url" placeholder="https://www.linkedin.com/in/tu-perfil"></label>
              <label>GitHub<input formControlName="github" autocomplete="url" placeholder="https://github.com/tu-usuario"></label>
              <label>Fotografía profesional, opcional<input type="file" accept="image/png,image/jpeg,image/webp" (change)="photo($event)"></label>
              @if (message()) { <p [class.error]="failed()" role="status">{{ message() }}</p> }
              <button class="btn wide" type="submit" [disabled]="busy()">{{ busy() ? 'Guardando…' : 'Continuar a mi carrera' }}</button>
            </form>
            <p class="switch-link"><button type="button" class="text-btn" (click)="skip()">Completar el perfil después</button></p>
          }
        </div>
      </section>
    </main>
  `
})
export class Register {
  private readonly forms = inject(FormBuilder);
  private readonly auth = inject(AuthService);
  private readonly api = inject(ApiService);
  private readonly router = inject(Router);
  readonly step = signal(1);
  readonly show = signal(false);
  readonly busy = signal(false);
  readonly failed = signal(false);
  readonly message = signal('');
  readonly countries = signal<Country[]>([]);
  readonly regions = signal<PlaceOption[]>([]);
  readonly cities = signal<PlaceOption[]>([]);
  readonly districts = signal<PlaceOption[]>([]);
  private photoFile: File | null = null;
  readonly account = this.forms.nonNullable.group({
    firstName: ['', Validators.required],
    lastName: ['', Validators.required],
    email: ['', [Validators.required, Validators.email]],
    password: ['', Validators.required],
    confirmPassword: ['', Validators.required],
    acceptTerms: [false, Validators.requiredTrue]
  });
  readonly profile = this.forms.nonNullable.group({
    countryCode: [''], region: [''], city: [''], district: [''], manualPlace: [''], phone: [''], linkedin: [''], github: ['']
  });
  constructor() { this.api.countries().subscribe((items) => this.countries.set(items)); }
  countryChoices(): Choice[] { return this.countries().map((item) => ({ id: item.iso2, label: item.name })); }
  placeChoices(items: PlaceOption[]): Choice[] { return items.map((item) => ({ id: item.name, label: item.name, hint: item.hint })); }
  invalid(group: typeof this.account, name: 'firstName' | 'lastName' | 'email' | 'password'): boolean {
    const control = group.controls[name];
    return control.invalid && control.touched;
  }
  mismatch(): boolean {
    const value = this.account.getRawValue();
    return this.account.controls.confirmPassword.touched && value.password !== value.confirmPassword;
  }
  strength(): number {
    const value = this.account.controls.password.value;
    let score = 0;
    if (value.length >= 10) score += 1;
    if (/[A-Z]/.test(value)) score += 1;
    if (/[a-z]/.test(value)) score += 1;
    if (/\d/.test(value)) score += 1;
    if (/[^A-Za-z0-9]/.test(value)) score += 1;
    return score;
  }
  strengthLabel(): string {
    const score = this.strength();
    if (score >= 5) return 'Fuerte';
    if (score >= 3) return 'Aceptable';
    return 'Débil';
  }
  pickCountry(code: string): void {
    this.profile.controls.countryCode.setValue(code);
    this.profile.controls.region.setValue('');
    this.regions.set([]);
    this.cities.set([]);
    this.districts.set([]);
    const country = this.countries().find((item) => item.iso2 === code);
    if (country) this.loadPlaces(country.name);
  }
  pickRegion(name: string): void {
    this.profile.controls.region.setValue(name);
    const country = this.countries().find((item) => item.iso2 === this.profile.controls.countryCode.value);
    if (country) this.loadPlaces(country.name, name);
  }
  private loadPlaces(country: string, region = ''): void {
    this.api.worldPlaces(country, region).subscribe((places) => {
      this.regions.set(places.regions ?? []);
      this.cities.set(places.cities ?? []);
      this.districts.set(places.districts ?? []);
    });
  }
  photo(event: Event): void {
    this.photoFile = (event.target as HTMLInputElement).files?.[0] ?? null;
  }
  createAccount(): void {
    if (this.busy()) return;
    this.account.markAllAsTouched();
    const value = this.account.getRawValue();
    if (this.account.invalid || value.password !== value.confirmPassword || this.strength() < 5) {
      this.failed.set(true);
      this.message.set(this.strength() < 5 ? 'La contraseña necesita 10 caracteres, mayúscula, minúscula, número y un signo.' : 'Revisa los campos marcados.');
      return;
    }
    this.busy.set(true);
    this.message.set('');
    this.auth.register({ ...value, countryCode: '' }).subscribe({
      next: (response) => {
        this.auth.persist(response);
        this.busy.set(false);
        this.failed.set(false);
        this.step.set(2);
      },
      error: (err) => { this.busy.set(false); this.failed.set(true); this.message.set(errorText(err)); }
    });
  }
  saveProfile(): void {
    if (this.busy()) return;
    this.busy.set(true);
    const value = this.profile.getRawValue();
    const country = this.countries().find((item) => item.iso2 === value.countryCode);
    this.api.saveProfile({
      firstName: this.account.controls.firstName.value, lastName: this.account.controls.lastName.value,
      bio: null, bioEn: null, birthDate: null, showBirthDate: false,
      countryId: country?.id ?? null, regionId: null, provinceId: null, cityId: null, districtId: null,
      customRegion: value.region || null, customProvince: null,
      customCity: value.city || value.manualPlace || null, customDistrict: value.district || null, address: null,
      phone: value.phone || null, professionalEmail: null, linkedin: value.linkedin || null,
      github: value.github || null, gitlab: null, behance: null, dribbble: null, orcid: null, googleScholar: null, website: null, otherLinks: []
    }).subscribe({
      next: () => this.afterProfile(),
      error: (err) => { this.busy.set(false); this.failed.set(true); this.message.set(errorText(err)); }
    });
  }
  skip(): void { void this.router.navigateByUrl('/app/academico'); }
  private afterProfile(): void {
    if (!this.photoFile) {
      void this.router.navigateByUrl('/app/academico');
      return;
    }
    const body = new FormData();
    body.set('file', this.photoFile);
    body.set('category', 'IMAGE');
    body.set('profilePhoto', 'true');
    this.api.upload(body).subscribe({
      next: () => void this.router.navigateByUrl('/app/academico'),
      error: (err) => { this.busy.set(false); this.failed.set(true); this.message.set(errorText(err)); }
    });
  }
}

@Component({
  selector: 'app-login',
  imports: [ReactiveFormsModule, RouterLink, EmeraldStage],
  template: `
    <main class="emerald" id="contenido">
      <app-emerald-stage variant="login" />
      <section class="emerald-form">
        <div class="emerald-card">
          <div class="form-head">
            <p class="eyebrow">Acceso personal</p>
            <p class="step-label">Privado</p>
          </div>
          <h1>Bienvenido de nuevo.</h1>
          <p class="lede">Ingresa a tu espacio profesional y continúa construyendo tu futuro.</p>
          <form [formGroup]="form" (ngSubmit)="submit()" novalidate>
            <label>Correo electrónico<input formControlName="email" type="email" autocomplete="email" placeholder="nombre@correo.com" [attr.aria-invalid]="invalid('email')"></label>
            @if (invalid('email')) { <p class="error" role="alert">Escribe un correo válido.</p> }
            <label>Contraseña
              <span class="secret">
                <input formControlName="password" [type]="show() ? 'text' : 'password'" autocomplete="current-password" [attr.aria-invalid]="invalid('password')">
                <button type="button" class="eye" (click)="show.set(!show())" [attr.aria-pressed]="show()" [attr.aria-label]="show() ? 'Ocultar contraseña' : 'Mostrar contraseña'">
                  <svg viewBox="0 0 24 24" aria-hidden="true"><path d="M2 12s3.5-7 10-7 10 7 10 7-3.5 7-10 7S2 12 2 12Z"/><circle cx="12" cy="12" r="3"/></svg>
                </button>
              </span>
            </label>
            <div class="form-row">
              <label class="check"><input type="checkbox" formControlName="remember"><span>Recordar sesión en este equipo</span></label>
              <a routerLink="/recuperar">¿Olvidaste tu contraseña?</a>
            </div>
            @if (message()) { <p class="error" role="alert">{{ message() }}</p> }
            <button class="btn wide" type="submit" [disabled]="busy()">{{ busy() ? 'Ingresando…' : 'Iniciar sesión' }}</button>
          </form>
          <p class="switch-link">¿Nuevo en Portfolio Hub? <a routerLink="/crear-cuenta">Crear cuenta</a></p>
        </div>
      </section>
    </main>
  `
})
export class Login {
  private readonly forms = inject(FormBuilder);
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);
  readonly message = signal('');
  readonly show = signal(false);
  readonly busy = signal(false);
  readonly form = this.forms.nonNullable.group({
    email: ['', [Validators.required, Validators.email]],
    password: ['', Validators.required],
    remember: [false]
  });
  invalid(name: 'email' | 'password'): boolean {
    const control = this.form.controls[name];
    return control.invalid && control.touched;
  }
  submit(): void {
    if (this.busy()) return;
    this.form.markAllAsTouched();
    if (this.form.invalid) {
      this.message.set('Revisa el correo y la contraseña.');
      return;
    }
    this.busy.set(true);
    this.message.set('');
    const value = this.form.getRawValue();
    this.auth.login(value.email, value.password).subscribe({
      next: (response) => { this.auth.persist(response, value.remember); void this.router.navigateByUrl('/app'); },
      error: (err) => { this.busy.set(false); this.message.set(errorText(err)); }
    });
  }
}

@Component({
  selector: 'app-verify',
  imports: [RouterLink, EmeraldStage],
  template: `
    <main class="emerald" id="contenido">
      <app-emerald-stage variant="login" kicker="Correo" title="Tu cuenta queda lista." lede="Si el enlace es válido, el acceso queda confirmado." />
      <section class="emerald-form">
        <div class="emerald-card">
          <p class="eyebrow">Correo</p>
          <h1>Confirmar correo</h1>
          <p>{{ message() }}</p>
          @if (pending()) { <p>Entorno local: <a [href]="pending()">abrir enlace de confirmación</a></p> }
          <p class="switch-link"><a routerLink="/iniciar-sesion">Ir a iniciar sesión</a></p>
        </div>
      </section>
    </main>
  `
})
export class Verify {
  private readonly route = inject(ActivatedRoute);
  private readonly api = inject(ApiService);
  readonly message = signal('Revisa tu correo para confirmar la cuenta.');
  readonly pending = signal(this.route.snapshot.queryParamMap.get('pending') ?? '');
  constructor() {
    const token = this.route.snapshot.queryParamMap.get('token');
    if (token) {
      this.api.verify(token).subscribe({
        next: () => this.message.set('Correo confirmado. Ya puedes iniciar sesión.'),
        error: (err) => this.message.set(errorText(err))
      });
    }
  }
}

@Component({
  selector: 'app-forgot',
  imports: [ReactiveFormsModule, RouterLink, EmeraldStage],
  template: `
    <main class="emerald" id="contenido">
      <app-emerald-stage variant="login" kicker="Recuperación" title="Recupera el acceso a tu sitio." lede="Si el correo está registrado, enviaremos las instrucciones." />
      <section class="emerald-form">
        <div class="emerald-card">
          <p class="eyebrow">Recuperación</p>
          <h1>Recuperar contraseña</h1>
          <form [formGroup]="form" (ngSubmit)="submit()">
            <label>Correo electrónico<input formControlName="email" type="email" autocomplete="email" placeholder="nombre@correo.com"></label>
            <button class="btn wide" type="submit">Enviar instrucciones</button>
          </form>
          @if (message()) { <p role="status">{{ message() }}</p> }
          <p class="switch-link"><a routerLink="/iniciar-sesion">Volver al inicio de sesión</a></p>
        </div>
      </section>
    </main>
  `
})
export class Forgot {
  private readonly forms = inject(FormBuilder);
  private readonly api = inject(ApiService);
  readonly message = signal('');
  readonly form = this.forms.nonNullable.group({ email: ['', Validators.required] });
  submit(): void { this.api.forgot(this.form.controls.email.value).subscribe(() => this.message.set('Si el correo está registrado, enviaremos instrucciones.')); }
}

@Component({
  selector: 'app-reset',
  imports: [ReactiveFormsModule, RouterLink, EmeraldStage],
  template: `
    <main class="emerald" id="contenido">
      <app-emerald-stage variant="login" kicker="Clave" title="Elige una contraseña nueva." lede="Usa al menos 10 caracteres, con mayúscula, minúscula, número y un signo." />
      <section class="emerald-form">
        <div class="emerald-card">
          <p class="eyebrow">Clave</p>
          <h1>Nueva contraseña</h1>
          <form [formGroup]="form" (ngSubmit)="submit()">
            <label>Contraseña<input formControlName="password" type="password" autocomplete="new-password"></label>
            <label>Confirmación<input formControlName="confirmPassword" type="password" autocomplete="new-password"></label>
            <button class="btn wide" type="submit">Guardar</button>
          </form>
          @if (message()) { <p role="status">{{ message() }}</p> }
          <p class="switch-link"><a routerLink="/iniciar-sesion">Iniciar sesión</a></p>
        </div>
      </section>
    </main>
  `
})
export class Reset {
  private readonly forms = inject(FormBuilder);
  private readonly api = inject(ApiService);
  private readonly route = inject(ActivatedRoute);
  readonly message = signal('');
  readonly form = this.forms.nonNullable.group({ password: ['', Validators.required], confirmPassword: ['', Validators.required] });
  submit(): void {
    this.api.reset({ token: this.route.snapshot.queryParamMap.get('token'), ...this.form.getRawValue() }).subscribe({
      next: () => this.message.set('Contraseña actualizada.'),
      error: (err) => this.message.set(errorText(err))
    });
  }
}

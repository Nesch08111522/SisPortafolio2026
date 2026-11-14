import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { ApiService, Country, Named } from '../core/api.service';
import { AuthService, errorText } from '../core/auth.service';

@Component({
  selector: 'app-register',
  imports: [ReactiveFormsModule, RouterLink],
  template: `
    <main class="gate" id="contenido">
      <aside class="gate-aside">
        <a class="brand" routerLink="/"><span class="mark">P</span> Portfolio Hub</a>
        <p>Al crear la cuenta recibes el enlace de tu sitio. No hace falta confirmar el correo.</p>
      </aside>
      <section class="gate-panel">
      <div class="card">
      <p class="page-kicker">Registro</p>
      <h1>Crear cuenta</h1>
      <form [formGroup]="form" (ngSubmit)="submit()">
        <div class="grid-2">
          <label>Nombres<input formControlName="firstName" autocomplete="given-name"></label>
          <label>Apellidos<input formControlName="lastName" autocomplete="family-name"></label>
        </div>
        <label>Correo<input formControlName="email" type="email" autocomplete="email"></label>
        <label>Contraseña<input formControlName="password" type="password" autocomplete="new-password"></label>
        <label>Confirmación<input formControlName="confirmPassword" type="password" autocomplete="new-password"></label>
        <label>País
          <select formControlName="countryCode" (change)="loadCities()">
            <option value="">Selecciona</option>
            @for (country of countries(); track country.iso2) { <option [value]="country.iso2">{{ country.name }}</option> }
          </select>
        </label>
        <label>Ciudad
          <select formControlName="cityId">
            <option value="">No aparece en la lista</option>
            @for (city of cities(); track city.id) { <option [value]="city.id">{{ city.name }}</option> }
          </select>
        </label>
        <label>Ciudad, si no está en la lista<input formControlName="customCity" autocomplete="address-level2"></label>
        <label>Teléfono, con código de país<input formControlName="phone" autocomplete="tel" placeholder="+51 999 000 000"></label>
        <label>LinkedIn<input formControlName="linkedin" autocomplete="url" placeholder="https://www.linkedin.com/in/tu-perfil"></label>
        <label class="check"><input type="checkbox" formControlName="acceptTerms"><span>Acepto los <a routerLink="/terminos">términos y condiciones</a>.</span></label>
        @if (message()) { <p [class.error]="error()">{{ message() }}</p> }
        <button class="btn" type="submit">Crear cuenta</button>
      </form>
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
  readonly countries = signal<Country[]>([]);
  readonly cities = signal<Named[]>([]);
  readonly message = signal('');
  readonly error = signal(false);
  readonly form = this.forms.nonNullable.group({
    firstName: ['', Validators.required], lastName: ['', Validators.required], email: ['', [Validators.required, Validators.email]],
    password: ['', Validators.required], confirmPassword: ['', Validators.required], countryCode: ['', Validators.required],
    cityId: [''], customCity: [''], phone: [''], linkedin: [''], acceptTerms: [false, Validators.requiredTrue]
  });
  constructor() { this.api.countries().subscribe((items) => this.countries.set(items)); }
  loadCities(): void {
    const country = this.countries().find((item) => item.iso2 === this.form.controls.countryCode.value);
    this.cities.set([]);
    this.form.controls.cityId.setValue('');
    if (country) this.api.cities(country.id).subscribe((items) => this.cities.set(items));
  }
  submit(): void {
    if (this.form.invalid) { this.form.markAllAsTouched(); return; }
    const value = this.form.getRawValue();
    this.auth.register(value).subscribe({
      next: (response) => {
        this.auth.persist(response);
        const country = this.countries().find((item) => item.iso2 === value.countryCode);
        this.api.saveProfile({
          firstName: value.firstName, lastName: value.lastName, bio: null, bioEn: null, birthDate: null, showBirthDate: false,
          countryId: country?.id ?? null, regionId: null, provinceId: null, cityId: value.cityId || null, districtId: null,
          customRegion: null, customProvince: null, customCity: value.customCity || null, customDistrict: null, address: null,
          phone: value.phone || null, professionalEmail: null, linkedin: value.linkedin || null,
          github: null, gitlab: null, behance: null, dribbble: null, orcid: null, googleScholar: null, website: null, otherLinks: []
        }).subscribe({
          next: () => void this.router.navigateByUrl('/app/academico'),
          error: () => void this.router.navigateByUrl('/app/academico')
        });
      },
      error: (err) => { this.error.set(true); this.message.set(errorText(err)); }
    });
  }
}

@Component({
  selector: 'app-login',
  imports: [ReactiveFormsModule, RouterLink],
  template: `
    <main class="gate" id="contenido">
      <aside class="gate-aside">
        <a class="brand" routerLink="/"><span class="mark">P</span> Portfolio Hub</a>
        <p>Entra para seguir tu portafolio, no para empezar de cero.</p>
      </aside>
      <section class="gate-panel">
      <div class="card">
      <p class="page-kicker">Acceso</p>
      <h1>Iniciar sesión</h1>
      <form [formGroup]="form" (ngSubmit)="submit()">
        <label>Correo<input formControlName="email" type="email"></label>
        <label>Contraseña<input formControlName="password" type="password"></label>
        @if (message()) { <p class="error">{{ message() }}</p> }
        <button class="btn" type="submit">Entrar</button>
      </form>
      <p><a routerLink="/recuperar">Recuperar contraseña</a></p>
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
  readonly form = this.forms.nonNullable.group({ email: ['', Validators.required], password: ['', Validators.required] });
  submit(): void {
    this.auth.login(this.form.controls.email.value, this.form.controls.password.value).subscribe({
      next: (response) => { this.auth.persist(response); void this.router.navigateByUrl('/app'); },
      error: (err) => this.message.set(errorText(err))
    });
  }
}

@Component({
  selector: 'app-verify',
  imports: [RouterLink],
  template: `
    <main class="gate" id="contenido">
      <aside class="gate-aside">
        <a class="brand" routerLink="/"><span class="mark">P</span> Portfolio Hub</a>
        <p>Confirmamos el correo antes de abrir el panel.</p>
      </aside>
      <section class="gate-panel">
      <div class="card">
      <p class="page-kicker">Correo</p>
      <h1>Confirmar correo</h1>
      <p>{{ message() }}</p>
      @if (pending()) { <p>Entorno local: <a [href]="pending()">abrir enlace de confirmación</a></p> }
      <a routerLink="/iniciar-sesion">Ir a iniciar sesión</a>
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
  imports: [ReactiveFormsModule, RouterLink],
  template: `
    <main class="gate" id="contenido">
      <aside class="gate-aside">
        <a class="brand" routerLink="/"><span class="mark">P</span> Portfolio Hub</a>
        <p>Si el correo existe, las instrucciones salen de aquí.</p>
      </aside>
      <section class="gate-panel">
      <div class="card">
      <p class="page-kicker">Recuperación</p>
      <h1>Recuperar contraseña</h1>
      <form [formGroup]="form" (ngSubmit)="submit()">
        <label>Correo<input formControlName="email" type="email"></label>
        <button class="btn" type="submit">Enviar instrucciones</button>
      </form>
      @if (message()) { <p>{{ message() }}</p> }
      <a routerLink="/iniciar-sesion">Volver</a>
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
  imports: [ReactiveFormsModule, RouterLink],
  template: `
    <main class="gate" id="contenido">
      <aside class="gate-aside">
        <a class="brand" routerLink="/"><span class="mark">P</span> Portfolio Hub</a>
        <p>Elige una contraseña larga, con mayúscula, número y un signo.</p>
      </aside>
      <section class="gate-panel">
      <div class="card">
      <p class="page-kicker">Clave</p>
      <h1>Nueva contraseña</h1>
      <form [formGroup]="form" (ngSubmit)="submit()">
        <label>Contraseña<input formControlName="password" type="password"></label>
        <label>Confirmación<input formControlName="confirmPassword" type="password"></label>
        <button class="btn" type="submit">Guardar</button>
      </form>
      @if (message()) { <p>{{ message() }}</p> }
      <a routerLink="/iniciar-sesion">Iniciar sesión</a>
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

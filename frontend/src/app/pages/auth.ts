import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { ApiService, Country } from '../core/api.service';
import { AuthService, errorText } from '../core/auth.service';

@Component({
  selector: 'app-register',
  imports: [ReactiveFormsModule, RouterLink],
  template: `
    <main class="narrow card" id="contenido">
      <a routerLink="/">Portfolio Hub</a>
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
          <select formControlName="countryCode">
            <option value="">Selecciona</option>
            @for (country of countries(); track country.iso2) { <option [value]="country.iso2">{{ country.name }}</option> }
          </select>
        </label>
        <label class="check"><input type="checkbox" formControlName="acceptTerms"><span>Acepto los <a routerLink="/terminos">términos y condiciones</a>.</span></label>
        @if (message()) { <p [class.error]="error()">{{ message() }}</p> }
        <button class="btn" type="submit">Crear cuenta</button>
      </form>
    </main>
  `
})
export class Register {
  private readonly forms = inject(FormBuilder);
  private readonly auth = inject(AuthService);
  private readonly api = inject(ApiService);
  private readonly router = inject(Router);
  readonly countries = signal<Country[]>([]);
  readonly message = signal('');
  readonly error = signal(false);
  readonly form = this.forms.nonNullable.group({
    firstName: ['', Validators.required], lastName: ['', Validators.required], email: ['', [Validators.required, Validators.email]],
    password: ['', Validators.required], confirmPassword: ['', Validators.required], countryCode: ['', Validators.required], acceptTerms: [false, Validators.requiredTrue]
  });
  constructor() { this.api.countries().subscribe((items) => this.countries.set(items)); }
  submit(): void {
    if (this.form.invalid) { this.form.markAllAsTouched(); return; }
    this.auth.register(this.form.getRawValue()).subscribe({
      next: (response) => {
        const link = response['devVerificationUrl'];
        void this.router.navigate(['/verificar-correo'], { queryParams: link ? { pending: link } : { sent: 1 } });
      },
      error: (err) => { this.error.set(true); this.message.set(errorText(err)); }
    });
  }
}

@Component({
  selector: 'app-login',
  imports: [ReactiveFormsModule, RouterLink],
  template: `
    <main class="narrow card" id="contenido">
      <h1>Iniciar sesión</h1>
      <form [formGroup]="form" (ngSubmit)="submit()">
        <label>Correo<input formControlName="email" type="email"></label>
        <label>Contraseña<input formControlName="password" type="password"></label>
        @if (message()) { <p class="error">{{ message() }}</p> }
        <button class="btn" type="submit">Entrar</button>
      </form>
      <p><a routerLink="/recuperar">Recuperar contraseña</a></p>
      <form [formGroup]="resendForm" (ngSubmit)="resend()">
        <label>Reenviar confirmación<input formControlName="email" type="email"></label>
        <button class="btn-ghost" type="submit">Reenviar</button>
      </form>
      @if (notice()) { <p>{{ notice() }}</p> }
    </main>
  `
})
export class Login {
  private readonly forms = inject(FormBuilder);
  private readonly auth = inject(AuthService);
  private readonly api = inject(ApiService);
  private readonly router = inject(Router);
  readonly message = signal('');
  readonly notice = signal('');
  readonly form = this.forms.nonNullable.group({ email: ['', Validators.required], password: ['', Validators.required] });
  readonly resendForm = this.forms.nonNullable.group({ email: ['', Validators.required] });
  submit(): void {
    this.auth.login(this.form.controls.email.value, this.form.controls.password.value).subscribe({
      next: (response) => { this.auth.persist(response); void this.router.navigateByUrl(response.user.onboardingCompleted ? '/app' : '/app/academico'); },
      error: (err) => this.message.set(errorText(err))
    });
  }
  resend(): void {
    this.api.resend(this.resendForm.controls.email.value).subscribe(() => this.notice.set('Si la cuenta existe, enviaremos otro correo.'));
  }
}

@Component({
  selector: 'app-verify',
  imports: [RouterLink],
  template: `
    <main class="narrow card" id="contenido">
      <h1>Confirmar correo</h1>
      <p>{{ message() }}</p>
      @if (pending()) { <p>Entorno local: <a [href]="pending()">abrir enlace de confirmación</a></p> }
      <a routerLink="/iniciar-sesion">Ir a iniciar sesión</a>
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
    <main class="narrow card" id="contenido">
      <h1>Recuperar contraseña</h1>
      <form [formGroup]="form" (ngSubmit)="submit()">
        <label>Correo<input formControlName="email" type="email"></label>
        <button class="btn" type="submit">Enviar instrucciones</button>
      </form>
      @if (message()) { <p>{{ message() }}</p> }
      <a routerLink="/iniciar-sesion">Volver</a>
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
    <main class="narrow card" id="contenido">
      <h1>Nueva contraseña</h1>
      <form [formGroup]="form" (ngSubmit)="submit()">
        <label>Contraseña<input formControlName="password" type="password"></label>
        <label>Confirmación<input formControlName="confirmPassword" type="password"></label>
        <button class="btn" type="submit">Guardar</button>
      </form>
      @if (message()) { <p>{{ message() }}</p> }
      <a routerLink="/iniciar-sesion">Iniciar sesión</a>
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

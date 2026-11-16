import { Component, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { ApiService, ExampleCard } from '../core/api.service';
import { EmeraldStage } from './emerald-stage';

@Component({
  selector: 'app-welcome',
  imports: [RouterLink, EmeraldStage],
  template: `
    <main class="emerald emerald-welcome" id="contenido">
      <section class="emerald-form">
        <div class="emerald-card">
          <p class="brand"><span class="mark">P</span> Portfolio Hub</p>
          <p class="eyebrow">Identidad profesional</p>
          <h1>Tu talento merece ser visto.</h1>
          <p class="lede">Construye un sitio propio según tu facultad y tu carrera. Eliges un diseño, ordenas tus proyectos y publicas un enlace.</p>
          <div class="actions">
            <a class="btn" routerLink="/crear-cuenta">Crear mi portafolio</a>
            <a class="btn-ghost" routerLink="/iniciar-sesion">Iniciar sesión</a>
          </div>
          <p class="welcome-links">
            <a routerLink="/recuperar">Recuperar contraseña</a>
            <a routerLink="/ejemplos">Explorar ejemplos</a>
            <a routerLink="/privacidad">Privacidad</a>
            <a routerLink="/cookies">Cookies</a>
            <a routerLink="/terminos">Términos</a>
          </p>
        </div>
      </section>
      <app-emerald-stage variant="welcome" />
    </main>
  `
})
export class Welcome {}

@Component({
  selector: 'app-examples',
  imports: [RouterLink],
  template: `
    <main class="reading" id="contenido">
      <a class="brand" routerLink="/"><span class="mark">P</span> Portfolio Hub</a>
      <p class="page-kicker">Vitrina</p>
      <h1>Ejemplos publicados</h1>
      <p class="note">Solo aparecen portafolios que su autor decidió mostrar como ejemplo. Si la lista está vacía, todavía nadie ha publicado con esa opción.</p>
      <div class="design-grid">
        @for (item of items(); track item.slug) {
          <a class="card" [routerLink]="['/p', item.slug]"><strong>{{ item.name }}</strong><p>{{ item.seoDescription || item.title }}</p></a>
        } @empty { <p class="empty">Aún no hay ejemplos publicados.</p> }
      </div>
    </main>
  `
})
export class Examples {
  private readonly api = inject(ApiService);
  readonly items = signal<ExampleCard[]>([]);
  constructor() { this.api.examples().subscribe((items) => this.items.set(items)); }
}

@Component({
  selector: 'app-terms',
  imports: [RouterLink],
  template: `
    <main class="reading" id="contenido">
      <a class="brand" routerLink="/"><span class="mark">P</span> Portfolio Hub</a>
      <p class="page-kicker">Condiciones</p>
      <h1>Términos y condiciones</h1>
      <div class="card reading-card">
      <p>Portfolio Hub permite crear un sitio profesional a partir de información que tú aportas. Eres responsable de no publicar datos de pacientes, clientes, menores ni expedientes identificables.</p>
      <p>Los documentos que subes permanecen privados salvo que marques su visibilidad pública. Confirmar una extracción solo indica que revisaste los datos: la plataforma no autentica certificados ni colegiatura.</p>
      <p>Puedes despublicar tu portafolio cuando quieras. La versión vigente de estos términos es 2026-11.</p>
      <p><a routerLink="/privacidad">Privacidad</a> · <a routerLink="/cookies">Cookies</a></p>
      <a routerLink="/crear-cuenta">Volver al registro</a>
      </div>
    </main>
  `
})
export class Terms {}

@Component({
  selector: 'app-privacy',
  imports: [RouterLink],
  template: `
    <main class="reading" id="contenido">
      <a class="brand" routerLink="/"><span class="mark">P</span> Portfolio Hub</a>
      <p class="page-kicker">Datos</p>
      <h1>Política de privacidad</h1>
      <div class="card reading-card">
        <p>La cuenta guarda tu nombre, correo de acceso, contraseña cifrada y los datos de perfil que tú escribes. El correo y la contraseña no se publican en el sitio.</p>
        <p>El país, la región, la ciudad y el distrito se consultan en catálogos externos para ayudarte a elegirlos. Si no aparecen, puedes escribirlos. La universidad y la carrera siguen el mismo criterio.</p>
        <p>Los documentos permanecen privados hasta que confirmas su uso. Puedes despublicar el sitio cuando quieras. Versión 2026-11.</p>
        <p><a routerLink="/cookies">Cookies</a> · <a routerLink="/terminos">Términos</a></p>
      </div>
    </main>
  `
})
export class Privacy {}

@Component({
  selector: 'app-cookies',
  imports: [RouterLink],
  template: `
    <main class="reading" id="contenido">
      <a class="brand" routerLink="/"><span class="mark">P</span> Portfolio Hub</a>
      <p class="page-kicker">Navegador</p>
      <h1>Cookies</h1>
      <div class="card reading-card">
        <p>La sesión de quien inicia sesión se guarda en el navegador de ese equipo. No se muestra en los sitios públicos.</p>
        <p>La elección de cookies se recuerda en este navegador. Si aceptas la analítica, una visita guarda la ruta y el tipo de dispositivo, sin ubicación precisa.</p>
        <p>Puedes borrar esa preferencia desde el almacenamiento del navegador y la pregunta volverá a aparecer.</p>
        <p><a routerLink="/privacidad">Privacidad</a> · <a routerLink="/terminos">Términos</a></p>
      </div>
    </main>
  `
})
export class Cookies {}

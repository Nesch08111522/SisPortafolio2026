import { Component, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { ApiService, ExampleCard } from '../core/api.service';

@Component({
  selector: 'app-welcome',
  imports: [RouterLink],
  template: `
    <main class="welcome" id="contenido">
      <section class="welcome-copy">
        <div>
          <div class="brand"><span class="mark">P</span> Portfolio Hub</div>
          <h1>Bienvenido.</h1>
          <p class="lede">Este sistema gestiona portafolios, proyectos y sitios web. Eliges tu facultad, tu carrera y uno de diez diseños. Cada cuenta recibe su propio enlace.</p>
          <div class="actions">
            <a class="btn" routerLink="/crear-cuenta">Crear una cuenta</a>
            <a class="btn-ghost" routerLink="/iniciar-sesion">Iniciar sesión</a>
          </div>
          <p class="welcome-links">
            <a routerLink="/recuperar">Recuperar contraseña</a>
            <a routerLink="/ejemplos">Explorar ejemplos</a>
          </p>
        </div>
        <p class="note">Cada carrera recibe secciones, campos y diez variantes visuales. El contenido no se pierde al cambiar de diseño.</p>
      </section>
      <aside class="welcome-stage" aria-label="Familias de diseño">
        <article class="specimen" data-family="tech"><strong>Tecnológico</strong><span>Proyectos, repositorios y stack. No es solo un cambio de color.</span></article>
        <article class="specimen" data-family="academic"><strong>Académico</strong><span>Formación, investigación y congresos, con límites profesionales claros.</span></article>
        <article class="specimen" data-family="editorial"><strong>Editorial</strong><span>Áreas jurídicas, publicaciones y casos anonimizados.</span></article>
        <article class="specimen" data-family="visual"><strong>Visual</strong><span>Galerías, planos, renders y proceso de proyecto.</span></article>
      </aside>
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
      <a routerLink="/crear-cuenta">Volver al registro</a>
      </div>
    </main>
  `
})
export class Terms {}

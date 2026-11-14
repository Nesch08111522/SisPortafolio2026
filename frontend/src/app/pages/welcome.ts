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
          <h1>Bienvenido a Portfolio Hub.</h1>
          <p class="lede">Construye tu identidad profesional, demuestra tus conocimientos y convierte tus proyectos y logros en oportunidades. Crea un portafolio profesional adaptado a tu carrera, sin necesidad de programar.</p>
          <div class="actions">
            <a class="btn" routerLink="/crear-cuenta">Crear una cuenta</a>
            <a class="btn-ghost" routerLink="/iniciar-sesion">Iniciar sesión</a>
            <a class="btn-ghost" routerLink="/recuperar">Recuperar contraseña</a>
            <a class="btn-ghost" routerLink="/ejemplos">Explorar ejemplos</a>
          </div>
        </div>
        <p class="note">Cada carrera recibe secciones, campos y diez variantes visuales. El contenido no se pierde al cambiar de diseño.</p>
      </section>
      <aside class="welcome-stage" aria-label="Familias de diseño">
        <article class="specimen"><strong>Tecnológico</strong><span>Proyectos, repositorios y stack. No es solo un cambio de color.</span></article>
        <article class="specimen"><strong>Académico</strong><span>Formación, investigación y congresos, con límites profesionales claros.</span></article>
        <article class="specimen"><strong>Editorial</strong><span>Áreas jurídicas, publicaciones y casos anonimizados.</span></article>
        <article class="specimen"><strong>Visual</strong><span>Galerías, planos, renders y proceso de proyecto.</span></article>
      </aside>
    </main>
  `
})
export class Welcome {}

@Component({
  selector: 'app-examples',
  imports: [RouterLink],
  template: `
    <main class="page" id="contenido">
      <a routerLink="/">Volver</a>
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
    <main class="page narrow card" id="contenido">
      <h1>Términos y condiciones</h1>
      <p>Portfolio Hub permite crear un sitio profesional a partir de información que tú aportas. Eres responsable de no publicar datos de pacientes, clientes, menores ni expedientes identificables.</p>
      <p>Los documentos que subes permanecen privados salvo que marques su visibilidad pública. Confirmar una extracción solo indica que revisaste los datos: la plataforma no autentica certificados ni colegiatura.</p>
      <p>Puedes despublicar tu portafolio cuando quieras. La versión vigente de estos términos es 2026-11.</p>
      <a routerLink="/crear-cuenta">Volver al registro</a>
    </main>
  `
})
export class Terms {}

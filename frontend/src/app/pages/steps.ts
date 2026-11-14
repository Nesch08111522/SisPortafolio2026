import { Component } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';

@Component({
  selector: 'app-steps',
  imports: [RouterLink, RouterLinkActive],
  template: `
    <nav class="steps" aria-label="Creación del sitio">
      <a routerLink="/app/academico" routerLinkActive="current">1. Facultad y carrera</a>
      <a routerLink="/app/documentos" routerLinkActive="current">2. Archivos</a>
      <a routerLink="/app/disenos" routerLinkActive="current">3. Diseño</a>
      <a routerLink="/app/editor" routerLinkActive="current">4. Editar el sitio</a>
    </nav>
  `
})
export class Steps {}

import { Component } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';

@Component({
  selector: 'app-steps',
  imports: [RouterLink, RouterLinkActive],
  template: `
    <nav class="steps" aria-label="Progreso de configuración del portafolio">
      <a routerLink="/app/academico" routerLinkActive="current"><small>01</small><span>Facultad y carrera</span></a>
      <a routerLink="/app/documentos" routerLinkActive="current"><small>02</small><span>Archivos</span></a>
      <a routerLink="/app/disenos" routerLinkActive="current"><small>03</small><span>Diseño</span></a>
      <a routerLink="/app/editor" routerLinkActive="current"><small>04</small><span>Editar el sitio</span></a>
    </nav>
  `
})
export class Steps {}

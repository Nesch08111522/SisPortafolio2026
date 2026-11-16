import { Component, HostListener, Input, signal } from '@angular/core';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-emerald-stage',
  imports: [RouterLink],
  template: `
    <aside class="stage" [attr.data-variant]="variant">
      <a class="brand stage-brand" routerLink="/"><span class="mark">P</span> Portfolio Hub</a>
      @if (variant === 'welcome') {
        <p class="stage-kicker">Cinco familias, un solo sitio</p>
      } @else {
        <p class="stage-kicker">{{ kicker }}</p>
        <h2>{{ title }}</h2>
        <p class="stage-lede">{{ lede }}</p>
      }
      <div class="stage-scene" aria-hidden="true">
        <div class="stage-world" [style.transform]="pose()">
          @if (variant === 'welcome') {
            <article class="glass glass-a"><span>Ingeniería y Tecnología</span><strong>Sistemas</strong><i></i><i></i></article>
            <article class="glass glass-b"><span>Ciencias de la Salud</span><strong>Cuidado</strong><i></i></article>
            <article class="glass glass-c"><span>Derecho</span><strong>Criterio</strong><i></i><i></i></article>
            <article class="glass glass-d"><span>Arquitectura y Diseño</span><strong>Espacio</strong><i></i></article>
            <article class="glass glass-e"><span>Administración</span><strong>Gestión</strong><i></i><i></i></article>
          } @else {
            <article class="glass screen">
              <span>Portafolio</span>
              <strong>Inicio</strong>
              <i></i><i></i><i></i>
            </article>
            <article class="glass card-a"><span>Proyecto</span><strong>Caso de estudio</strong></article>
            <article class="glass card-b"><span>Certificado</span><strong>Formación</strong></article>
            <article class="glass card-c"><span>Habilidad</span><strong>Oficio</strong></article>
          }
          <span class="orb orb-a"></span>
          <span class="orb orb-b"></span>
          <span class="glow"></span>
        </div>
      </div>
      @if (variant === 'register') {
        <ol class="stage-steps">
          <li>01 — Crea tu cuenta</li>
          <li>02 — Define tu perfil</li>
          <li>03 — Publica tu portafolio</li>
        </ol>
      } @else if (variant === 'login') {
        <p class="stage-foot">Diseña · Construye · Destaca</p>
      } @else {
        <p class="stage-foot">Una composición por carrera. Un enlace por persona.</p>
      }
    </aside>
  `
})
export class EmeraldStage {
  @Input() variant: 'login' | 'register' | 'welcome' = 'login';
  @Input() kicker = 'Tu espacio profesional';
  @Input() title = 'Tu próximo capítulo comienza aquí.';
  @Input() lede = 'Accede a tus proyectos, conocimientos y logros. Tu talento tiene un lugar donde destacar.';
  private readonly x = signal(0);
  private readonly y = signal(0);

  pose(): string {
    if (typeof window !== 'undefined' && window.matchMedia('(prefers-reduced-motion: reduce)').matches) return 'none';
    return `rotateX(${8 - this.y()}deg) rotateY(${-16 + this.x()}deg)`;
  }

  @HostListener('pointermove', ['$event'])
  move(event: PointerEvent): void {
    if (window.matchMedia('(prefers-reduced-motion: reduce)').matches) return;
    if (window.matchMedia('(max-width: 900px)').matches) return;
    const bounds = (event.currentTarget as HTMLElement).getBoundingClientRect();
    this.x.set(((event.clientX - bounds.left) / bounds.width - 0.5) * 10);
    this.y.set(((event.clientY - bounds.top) / bounds.height - 0.5) * 8);
  }
}

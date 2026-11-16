import { Component, EventEmitter, Input, OnChanges, Output, signal } from '@angular/core';

export interface Choice { id: string; label: string; hint?: string; }

@Component({
  selector: 'app-search-select',
  template: `
    <div class="combo">
      <input role="combobox" [attr.aria-expanded]="open()" aria-autocomplete="list" [placeholder]="placeholder" [value]="text()" (input)="type($event)" (focus)="open.set(true)" (blur)="open.set(false)" />
      @if (open()) {
        <ul class="combo-list" role="listbox">
          @for (item of shown(); track item.id) {
            <li role="option" (mousedown)="choose(item, $event)">
              <span>{{ item.label }}</span>
              @if (item.hint) { <small>{{ item.hint }}</small> }
            </li>
          } @empty { <li class="combo-empty">Sin resultados</li> }
        </ul>
      }
    </div>
  `
})
export class SearchSelect implements OnChanges {
  @Input() placeholder = 'Buscar';
  @Input() options: Choice[] = [];
  @Input() value = '';
  @Output() readonly valueChange = new EventEmitter<string>();
  @Output() readonly queryChange = new EventEmitter<string>();
  readonly open = signal(false);
  readonly text = signal('');
  readonly shown = signal<Choice[]>([]);

  ngOnChanges(): void {
    const selected = this.options.find((item) => item.id === this.value);
    if (selected) this.text.set(selected.label);
    this.shown.set(this.filter(this.text()));
  }

  type(event: Event): void {
    const value = (event.target as HTMLInputElement).value;
    this.text.set(value);
    this.open.set(true);
    this.queryChange.emit(value);
    this.shown.set(this.filter(value));
    if (!value.trim()) this.valueChange.emit('');
  }

  choose(item: Choice, event: Event): void {
    event.preventDefault();
    this.text.set(item.label);
    this.valueChange.emit(item.id);
    this.open.set(false);
  }

  private filter(query: string): Choice[] {
    const needle = query.trim().toLowerCase();
    const list = needle
      ? this.options.filter((item) => `${item.label} ${item.hint ?? ''}`.toLowerCase().includes(needle))
      : this.options;
    return list.slice(0, 12);
  }
}

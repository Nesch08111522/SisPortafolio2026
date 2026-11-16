import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { ConsentBar } from './pages/consent';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, ConsentBar],
  template: '<router-outlet /><app-consent />'
})
export class App {}

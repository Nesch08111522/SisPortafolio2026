import { Routes } from '@angular/router';
import { adminGuard, authGuard, guestGuard } from './core/auth.service';
import { Forgot, Login, Register, Reset, Verify } from './pages/auth';
import { DesignsPage, EditorPage } from './pages/editor';
import { AcademicPage, DashboardPage, ProfilePage } from './pages/panel';
import { PublicFolioPage } from './pages/public-folio';
import { AdminPage, MessagesPage, PublishPage } from './pages/publish';
import { Shell } from './pages/shell';
import { DocumentsPage, StudioPage } from './pages/studio';
import { Examples, Terms, Welcome } from './pages/welcome';

export const routes: Routes = [
  { path: '', component: Welcome },
  { path: 'crear-cuenta', component: Register, canActivate: [guestGuard] },
  { path: 'iniciar-sesion', component: Login, canActivate: [guestGuard] },
  { path: 'recuperar', component: Forgot },
  { path: 'restablecer', component: Reset },
  { path: 'verificar-correo', component: Verify },
  { path: 'terminos', component: Terms },
  { path: 'ejemplos', component: Examples },
  { path: 'p/:slug', component: PublicFolioPage },
  {
    path: 'app',
    component: Shell,
    canActivate: [authGuard],
    children: [
      { path: '', component: DashboardPage },
      { path: 'perfil', component: ProfilePage },
      { path: 'academico', component: AcademicPage },
      { path: 'contenido', component: StudioPage },
      { path: 'documentos', component: DocumentsPage },
      { path: 'disenos', component: DesignsPage },
      { path: 'editor', component: EditorPage },
      { path: 'publicar', component: PublishPage },
      { path: 'mensajes', component: MessagesPage },
      { path: 'admin', component: AdminPage, canActivate: [adminGuard] }
    ]
  }
];

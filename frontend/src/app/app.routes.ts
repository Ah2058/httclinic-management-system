import { Routes } from '@angular/router';
import { authGuard } from './auth/auth.guard';
import { adminGuard } from './auth/admin.guard';

export const routes: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'home' },
  {
    path: 'home',
    loadComponent: () =>
      import('./home/home').then((m) => m.HomeComponent)
  },
  {
    path: 'login',
    loadComponent: () =>
      import('./login/login').then((m) => m.LoginComponent)
  },
  {
    path: 'patient-dashboard',
    loadComponent: () =>
      import('./patient-dashboard/patient-dashboard').then(
        (m) => m.PatientDashboardComponent
      )
  },
  {
    path: 'admin-dashboard',
    canActivate: [adminGuard],
    loadComponent: () =>
      import('./admin-dashboard/admin-dashboard').then(
        (m) => m.AdminDashboardComponent
      )
  },
  { path: '**', redirectTo: 'home' }
];

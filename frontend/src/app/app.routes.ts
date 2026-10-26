import { Routes } from '@angular/router';
import { authGuard, guestGuard, permissionGuard } from './core/auth.guard';

export const routes: Routes = [
  {
    path: 'login',
    canActivate: [guestGuard],
    title: 'Entrar · ERP GM Imobiliária',
    loadComponent: () => import('./features/login/login.component').then((m) => m.LoginComponent),
  },
  {
    path: '',
    canActivate: [authGuard],
    loadComponent: () => import('./layout/shell.component').then((m) => m.ShellComponent),
    children: [
      { path: '', pathMatch: 'full', redirectTo: 'locadores' },
      {
        path: 'locadores',
        canActivate: [permissionGuard('PEOPLE_READ')],
        title: 'Locadores · ERP GM Imobiliária',
        loadComponent: () => import('./features/cadastros/owners-page.component').then((m) => m.OwnersPageComponent),
      },
      {
        path: 'imoveis',
        canActivate: [permissionGuard('PROPERTIES_READ')],
        title: 'Imóveis · ERP GM Imobiliária',
        loadComponent: () =>
          import('./features/cadastros/properties-page.component').then((m) => m.PropertiesPageComponent),
      },
      {
        path: 'locatarios',
        canActivate: [permissionGuard('PEOPLE_READ')],
        title: 'Locatários · ERP GM Imobiliária',
        loadComponent: () => import('./features/cadastros/tenants-page.component').then((m) => m.TenantsPageComponent),
      },
      {
        path: 'sem-permissao',
        title: 'Sem permissão · ERP GM Imobiliária',
        loadComponent: () => import('./features/not-allowed.component').then((m) => m.NotAllowedComponent),
      },
      {
        path: '**',
        title: 'Página não encontrada · ERP GM Imobiliária',
        loadComponent: () => import('./features/not-found.component').then((m) => m.NotFoundComponent),
      },
    ],
  },
];

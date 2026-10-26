import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { Permission } from './api.types';
import { AuthService } from './auth.service';

/** Exige sessão. Só melhora a navegação: cada rota da API valida a sessão de novo. */
export const authGuard: CanActivateFn = async (_route, state) => {
  const auth = inject(AuthService);
  const router = inject(Router);
  await auth.restore();
  return auth.isAuthenticated() ? true : router.createUrlTree(['/login'], { queryParams: { voltar: state.url } });
};

export const guestGuard: CanActivateFn = async () => {
  const auth = inject(AuthService);
  const router = inject(Router);
  await auth.restore();
  return auth.isAuthenticated() ? router.createUrlTree(['/']) : true;
};

export function permissionGuard(permission: Permission): CanActivateFn {
  return () => {
    const auth = inject(AuthService);
    const router = inject(Router);
    return auth.can(permission) ? true : router.createUrlTree(['/sem-permissao']);
  };
}

import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';
import { AuthService } from './auth.service';

const SESSION_ENDPOINTS = ['/api/v1/auth/me', '/api/v1/auth/login', '/api/v1/auth/logout'];

/** Sessão expirada no meio do uso: volta para o login guardando a tela atual. */
export const sessionInterceptor: HttpInterceptorFn = (request, next) => {
  const auth = inject(AuthService);
  const router = inject(Router);
  return next(request).pipe(
    catchError((error: unknown) => {
      if (error instanceof HttpErrorResponse && error.status === 401
          && !SESSION_ENDPOINTS.some((endpoint) => request.url.startsWith(endpoint))) {
        auth.clear();
        void router.navigate(['/login'], { queryParams: { voltar: router.url, expirada: 1 } });
      }
      return throwError(() => error);
    }),
  );
};

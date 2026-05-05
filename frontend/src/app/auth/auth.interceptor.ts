import {
  HttpErrorResponse,
  HttpInterceptorFn
} from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';
import { AuthService } from './auth.service';

function shouldAttachAuthHeader(url: string): boolean {
  // Attach JWT only to submit-service calls.
  return url.startsWith('/api/submit/');
}

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const auth = inject(AuthService);
  const router = inject(Router);

  const token = auth.token();
  const withAuth =
    token && shouldAttachAuthHeader(req.url) && !req.headers.has('Authorization')
      ? req.clone({ setHeaders: { Authorization: `Bearer ${token}` } })
      : req;

  return next(withAuth).pipe(
    catchError((err: unknown) => {
      if (err instanceof HttpErrorResponse && err.status === 401) {
        auth.logout();
        // Avoid navigation loops if we're already on /login.
        if (router.url !== '/login') router.navigateByUrl('/login');
      }
      return throwError(() => err);
    })
  );
};


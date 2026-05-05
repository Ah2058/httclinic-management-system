import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { catchError, Observable, throwError } from 'rxjs';
import { PatientFormPayload, PatientFormResponse } from './patient-form.model';

// Use dev-server proxy to avoid CORS issues during local development.
// See `frontend/proxy.conf.json`.
const SUBMIT_ENDPOINT = '/api/submit/forms'; // Proxied to http://localhost:8081/api/submit/forms

@Injectable({ providedIn: 'root' })
export class PatientFormService {
  private readonly http = inject(HttpClient);

  getLastSubmission(): null {
    return null;
  }

  clearLastSubmission(): void {
    // Patient data is intentionally not persisted in browser storage.
  }

  submit(payload: PatientFormPayload): Observable<PatientFormResponse> {
    return this.http
      .post<PatientFormResponse>(SUBMIT_ENDPOINT, payload)
      .pipe(catchError((err: unknown) => throwError(() => this.normalizeError(err))));
  }

  private normalizeError(err: unknown): Error {
    if (err instanceof HttpErrorResponse) {
      const msg =
        typeof err.error === 'string'
          ? err.error
          : (err.message ?? 'Request failed');
      return new Error(msg);
    }
    return err instanceof Error ? err : new Error('Request failed');
  }
}

import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { catchError, Observable, throwError } from 'rxjs';
import { AdminUpdatePayload, PatientFormRecord } from './admin-form.model';

const FORMS_BASE = '/api/submit/forms';

@Injectable({ providedIn: 'root' })
export class AdminFormService {
  private readonly http = inject(HttpClient);

  getAllForms(): Observable<PatientFormRecord[]> {
    return this.http
      .get<PatientFormRecord[]>(`${FORMS_BASE}`)
      .pipe(catchError((err: unknown) => throwError(() => this.normalizeError(err))));
  }

  getFormById(id: number): Observable<PatientFormRecord> {
    return this.http
      .get<PatientFormRecord>(`${FORMS_BASE}/${id}`)
      .pipe(catchError((err: unknown) => throwError(() => this.normalizeError(err))));
  }

  updateAdminFields(
    id: number,
    payload: AdminUpdatePayload
  ): Observable<PatientFormRecord> {
    return this.http
      .patch<PatientFormRecord>(`${FORMS_BASE}/${id}/admin`, payload)
      .pipe(catchError((err: unknown) => throwError(() => this.normalizeError(err))));
  }

  downloadReportPdf(id: number, language?: string): Observable<Blob> {
    const params = language ? `?lang=${language}` : '';
    return this.http
      .get(`${FORMS_BASE}/${id}/report.pdf${params}`, {
        responseType: 'blob'
      })
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


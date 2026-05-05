import { HttpErrorResponse } from '@angular/common/http';
import {
  ChangeDetectionStrategy,
  Component,
  DestroyRef,
  computed,
  inject,
  signal
} from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import {
  NonNullableFormBuilder,
  ReactiveFormsModule,
  Validators
} from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';
import { timer } from 'rxjs';
import { AuthService } from '../auth/auth.service';
import { LanguageService } from '../i18n/language.service';
import { digitsOnlyValidator } from '../patient-dashboard/validators';
import { AdminFormService } from './admin-form.service';
import { PatientFormRecord, FormStatus } from './admin-form.model';
import { Language } from '../i18n/translations';
import { Symptom } from '../patient-dashboard/patient-form.model';

type LoadState = 'idle' | 'loading' | 'loaded' | 'error';
type ToastKind = 'success' | 'error' | 'info';
interface ToastState {
  kind: ToastKind;
  message: string;
}

const SYMPTOM_IMAGES: Record<Symptom, string> = {
  FEVER: '/symptoms/fever.svg',
  COUGH: '/symptoms/cough.svg',
  SHORTNESS_OF_BREATH: '/symptoms/breath.svg',
  HEADACHE: '/symptoms/headache.svg',
  DIZZINESS: '/symptoms/dizziness.svg',
  NAUSEA: '/symptoms/nausea.svg',
  CHEST_PAIN: '/symptoms/chest.svg',
  BACK_PAIN: '/symptoms/back.svg',
  RASH: '/symptoms/rash.svg'
};

@Component({
  selector: 'app-admin-dashboard',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [ReactiveFormsModule, RouterLink, CommonModule],
  templateUrl: './admin-dashboard.html',
  styleUrl: './admin-dashboard.css'
})
export class AdminDashboardComponent {
  private readonly fb = inject(NonNullableFormBuilder);
  private readonly adminForms = inject(AdminFormService);
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);
  private readonly destroyRef = inject(DestroyRef);
  protected readonly languageService = inject(LanguageService);
  protected currentLanguage = this.languageService.getCurrentLanguage();
  protected languages = this.languageService.getLanguages();

   protected readonly state = signal<LoadState>('idle');
   protected readonly errorMessage = signal<string | null>(null);
   protected readonly record = signal<PatientFormRecord | null>(null);
  protected readonly toast = signal<ToastState | null>(null);
  protected readonly allForms = signal<PatientFormRecord[]>([]);
  protected readonly showAllForms = signal(false);
  protected readonly updatedFormId = signal<number | null>(null);
  protected readonly tableFilterText = signal('');
  protected readonly tableFilterStatus = signal<'all' | FormStatus>('all');
  protected readonly filteredForms = computed(() => {
    const query = this.tableFilterText().trim().toLowerCase();
    const status = this.tableFilterStatus();

    return this.allForms().filter((form) => {
      const formStatus = this.normalizedStatus(form.status);
      const matchesStatus = status === 'all' || formStatus === status;
      if (!matchesStatus) return false;
      if (!query) return true;

      const haystack = [
        form.id,
        form.firstName,
        form.lastName,
        form.phoneNumber,
        formStatus
      ]
        .join(' ')
        .toLowerCase();

      return haystack.includes(query);
    });
  });

  protected readonly searchForm = this.fb.group({
    id: this.fb.control('', { validators: [Validators.required, digitsOnlyValidator()] })
  });

  protected readonly adminForm = this.fb.group({
    diagnosis: this.fb.control(''),
    notes: this.fb.control(''),
    requiredMedicine: this.fb.control('')
  });

   constructor() {
     this.loadAllForms();
   }

  private loadAllForms(): void {
    this.adminForms
      .getAllForms()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (forms) => {
          this.allForms.set(forms);
        },
        error: (err: unknown) => {
          console.error('Failed to load forms:', err);
        }
      });
  }

   protected changeLanguage(lang: Language): void {
     this.languageService.setLanguage(lang);
   }

   protected onLanguageChange(event: Event): void {
     event.preventDefault();
     event.stopPropagation();
     this.changeLanguage((event.target as HTMLSelectElement).value as Language);
   }

  protected t(key: string): string {
    return this.languageService.translate(key);
  }

  protected onLogout(): void {
    this.auth.logout();
    this.router.navigateByUrl('/login');
  }

  protected onPrintPage(): void {
    window.print();
  }

  protected toggleShowAllForms(): void {
    this.showAllForms.update((v) => !v);
  }

  protected onTableFilterTextChange(value: string): void {
    this.tableFilterText.set(value);
  }

  protected onTableFilterStatusChange(value: string): void {
    this.tableFilterStatus.set(value === 'new' || value === 'viewed' || value === 'done' ? value : 'all');
  }

  protected clearTableFilters(): void {
    this.tableFilterText.set('');
    this.tableFilterStatus.set('all');
  }

  protected loadFormFromList(id: number): void {
    this.showAllForms.set(false);
    this.searchForm.controls.id.setValue(id.toString());
    this.onSearch();
  }

    protected onSearch(): void {
      this.errorMessage.set(null);
      this.record.set(null);

      if (this.searchForm.invalid) {
        this.searchForm.markAllAsTouched();
        return;
      }

      const id = Number(this.searchForm.controls.id.value);
      if (!Number.isFinite(id) || id <= 0) {
        this.errorMessage.set(this.t('admin.required'));
        return;
      }

      this.state.set('loading');
      this.adminForms
        .getFormById(id)
        .pipe(takeUntilDestroyed(this.destroyRef))
        .subscribe({
          next: (rec) => {
            this.state.set('loaded');

            // Mark as viewed if status is new
            let recordToUpdate = rec;
            if (this.normalizedStatus(rec.status) === 'new') {
              recordToUpdate = { ...rec, status: 'viewed' };
              // Update the list immediately with viewed status
              this.updateFormInList(recordToUpdate);
              // Also update the backend
              this.updateFormStatus(rec.id, 'viewed');
            }

            this.record.set(recordToUpdate);
            this.adminForm.patchValue({
              diagnosis: rec.diagnosis ?? '',
              notes: rec.notes ?? '',
              requiredMedicine: rec.requiredMedicine ?? ''
            });
            this.toast.set({ kind: 'info', message: `${this.t('admin.record_loaded')} #${rec.id}.` });
            this.autoHideToast();
         },
         error: (err: unknown) => {
           this.state.set('error');
           const msg = this.toMessage(err);
           this.errorMessage.set(msg);
           this.toast.set({ kind: 'error', message: msg });
           this.autoHideToast();
         }
       });
   }

    protected onSaveAdmin(): void {
      const rec = this.record();
      if (!rec) return;

      const raw = this.adminForm.getRawValue();
      const trimOrNull = (v: string): string | null => {
        const t = v.trim();
        return t.length ? t : null;
      };

      console.log(`[AdminDashboard] Saving form ${rec.id} with status: done`);
      this.state.set('loading');
      this.adminForms
        .updateAdminFields(rec.id, {
          diagnosis: trimOrNull(raw.diagnosis),
          notes: trimOrNull(raw.notes),
          requiredMedicine: trimOrNull(raw.requiredMedicine),
          status: 'done'
        })
        .pipe(takeUntilDestroyed(this.destroyRef))
        .subscribe({
          next: (updated) => {
            const updatedRecord: PatientFormRecord = {
              ...rec,
              ...updated,
              diagnosis: trimOrNull(raw.diagnosis),
              notes: trimOrNull(raw.notes),
              requiredMedicine: trimOrNull(raw.requiredMedicine),
              status: 'done'
            };
            console.log(`[AdminDashboard] Save successful. Final status: ${updatedRecord.status}`, updatedRecord);
            this.state.set('loaded');
            // Update the status in the allForms list also
            this.updateFormInList(updatedRecord);
            this.showAllForms.set(true);
            this.record.set(null);
            this.adminForm.reset({
              diagnosis: '',
              notes: '',
              requiredMedicine: ''
            });
            this.toast.set({ kind: 'success', message: this.t('admin.saved_success') });
            this.autoHideToast();
          },
          error: (err: unknown) => {
            console.error(`[AdminDashboard] Save failed:`, err);
            this.state.set('error');
            const msg = this.toMessage(err);
            this.errorMessage.set(msg);
            this.toast.set({ kind: 'error', message: msg });
            this.autoHideToast();
          }
        });
    }

  protected onDownloadPdf(): void {
    const rec = this.record();
    if (!rec) return;

    this.adminForms
      .downloadReportPdf(rec.id, this.currentLanguage())
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (blob) => {
          const url = URL.createObjectURL(blob);
          const a = document.createElement('a');
          a.href = url;
          a.download = `report-${rec.id}.pdf`;
          a.click();
          URL.revokeObjectURL(url);

          this.toast.set({ kind: 'success', message: this.t('admin.downloaded') });
          this.autoHideToast();
        },
        error: (err: unknown) => {
          const msg = this.toMessage(err);
          this.errorMessage.set(msg);
          this.toast.set({ kind: 'error', message: msg });
          this.autoHideToast();
        }
      });
  }

  protected onPrintRecord(): void {
    const rec = this.record();
    if (!rec) return;

    const printWindow = window.open('', '', 'height=800,width=1000');
    if (!printWindow) return;

    const htmlContent = this.generatePrintHtml(rec);
    printWindow.document.write(htmlContent);
    printWindow.document.close();
    printWindow.print();
  }

   protected dismissToast(): void {
     this.toast.set(null);
   }

   protected getStatusLabel(status?: FormStatus): string {
     switch (this.normalizedStatus(status)) {
       case 'new':
         return this.t('admin.status_new');
       case 'viewed':
         return this.t('admin.status_viewed');
       case 'done':
         return this.t('admin.status_done');
       default:
         return this.t('admin.status_new');
     }
   }

   protected getStatusClass(status?: FormStatus): string {
     switch (this.normalizedStatus(status)) {
       case 'new':
         return 'status-new';
       case 'viewed':
         return 'status-viewed';
       case 'done':
         return 'status-done';
       default:
         return 'status-new';
     }
   }

   private updateFormStatus(id: number, status: FormStatus): void {
     this.adminForms
       .updateAdminFields(id, { status })
       .pipe(takeUntilDestroyed(this.destroyRef))
       .subscribe({
         error: (err: unknown) => console.error('Failed to update form status:', err)
       });
   }

    private updateFormInList(updated: PatientFormRecord): void {
      const forms = this.allForms();
      const index = forms.findIndex(f => f.id === updated.id);
      console.log(`[AdminDashboard] Updating form ${updated.id} in list. Status: ${updated.status}. Index: ${index}`);

      if (index !== -1) {
        const newForms = [...forms];
        newForms[index] = updated;
        this.allForms.set(newForms);

        // Mark the form as updated for animation effect
        this.updatedFormId.set(updated.id);
        console.log(`[AdminDashboard] Animation triggered for form ${updated.id}`);

        // Clear the updated state after animation completes (1.2s)
        timer(1200)
          .pipe(takeUntilDestroyed(this.destroyRef))
          .subscribe(() => {
            this.updatedFormId.set(null);
            console.log(`[AdminDashboard] Animation cleared for form ${updated.id}`);
          });
     } else {
        console.warn(`[AdminDashboard] Form ${updated.id} not found in allForms list`);
      }
    }

  private normalizedStatus(status?: FormStatus): FormStatus {
    return status === 'viewed' || status === 'done' ? status : 'new';
  }

  protected formatSymptomList(items: readonly Symptom[] | null | undefined): string {
    if (!items || !items.length) return this.t('common.none');
    return items.map((item) => this.enumLabel('enum.symptom', item)).join(', ');
  }

  protected formatList(
    items: readonly string[] | null | undefined,
    prefix: 'enum.allergy' | 'enum.medication' | 'enum.condition'
  ): string {
    if (!items || !items.length) return this.t('common.none');
    return items.map((item) => this.enumLabel(prefix, item)).join(', ');
  }

  protected symptomImage(symptom: Symptom): string {
    return SYMPTOM_IMAGES[symptom];
  }

  private enumLabel(prefix: string, value: string): string {
    return this.t(`${prefix}.${value}`);
  }

  private safe(value: unknown): string {
    if (value == null) return this.t('common.dash');
    const text = String(value).trim();
    return text.length ? this.escapeHtml(text) : this.t('common.dash');
  }

  private escapeHtml(value: string): string {
    return value
      .replaceAll('&', '&amp;')
      .replaceAll('<', '&lt;')
      .replaceAll('>', '&gt;')
      .replaceAll('"', '&quot;')
      .replaceAll("'", '&#39;');
  }

  private generatePrintHtml(rec: PatientFormRecord): string {
    const now = new Date().toLocaleString();

    const row = (label: string, value: string): string => `
      <div class="row"><div class="label">${this.escapeHtml(label)}:</div><div class="value">${value}</div></div>
    `;

    const symptoms = (rec.symptoms ?? [])
      .map(
        (symptom) =>
          `<span class="symptom-pill"><img src="${this.symptomImage(symptom)}" alt="" />${this.escapeHtml(
            this.enumLabel('enum.symptom', symptom)
          )}</span>`
      )
      .join('');

     return `
       <!DOCTYPE html>
       <html lang="en">
       <head>
         <meta charset="UTF-8" />
         <title>${this.escapeHtml(this.t('report.title'))} - ${rec.id}</title>
        <style>
          body { font-family: "Segoe UI", Tahoma, Arial, sans-serif; line-height: 1.45; color: #1f3550; margin: 20px; background: #f8fbff; }
          .sheet { background: #fff; border: 1px solid #d8e4f5; border-radius: 12px; padding: 22px; }
          .header { border-bottom: 2px solid #d8e4f5; padding-bottom: 12px; margin-bottom: 14px; }
          .header h1 { margin: 0; font-size: 1.35rem; }
          .meta { margin-top: 6px; color: #5f748f; font-size: .92rem; }
          .section { margin: 16px 0; border: 1px solid #e2ebf8; border-radius: 10px; overflow: hidden; }
          .section h2 { margin: 0; padding: 8px 12px; background: #edf4ff; font-size: 1.02rem; }
          .section-content { padding: 10px 12px; }
          .row { display: grid; grid-template-columns: 220px 1fr; gap: 10px; padding: 4px 0; border-bottom: 1px dashed #e7eef9; }
          .row:last-child { border-bottom: none; }
          .label { font-weight: 600; color: #2f4d73; }
          .value { color: #1f3550; }
          .symptom-pills { display: flex; flex-wrap: wrap; gap: 8px; }
          .symptom-pill { display: inline-flex; align-items: center; gap: 6px; padding: 4px 8px; border-radius: 999px; border: 1px solid #d2e1f5; background: #f6faff; font-size: .9rem; }
          .symptom-pill img { width: 14px; height: 14px; }
          .note { margin-top: 14px; color: #5f748f; font-size: .88rem; }
          @media print { body { margin: 0; background: #fff; } .sheet { border: none; border-radius: 0; } }
        </style>
      </head>
      <body>
        <div class="sheet">
          <div class="header">
            <h1>${this.escapeHtml(this.t('report.title'))}</h1>
            <div class="meta">${this.escapeHtml(this.t('admin.form'))} ID: ${rec.id}</div>
            <div class="meta">${this.escapeHtml(this.t('report.generated'))}: ${this.escapeHtml(now)}</div>
          </div>

          <section class="section">
            <h2>${this.escapeHtml(this.t('admin.personal'))}</h2>
            <div class="section-content">
              ${row(this.t('admin.first_name'), this.safe(rec.firstName))}
              ${row(this.t('admin.last_name'), this.safe(rec.lastName))}
              ${row(this.t('admin.date_of_birth'), this.safe(rec.dateOfBirth))}
              ${row(this.t('admin.phone'), this.safe(rec.phoneNumber))}
              ${row(this.t('admin.email'), this.safe(rec.emailAddress))}
              ${row(this.t('admin.address'), this.safe(`${rec.streetName} ${rec.streetNumber}, ${rec.city}, ${rec.postalCode}`))}
            </div>
          </section>

          <section class="section">
            <h2>${this.escapeHtml(this.t('admin.medical'))}</h2>
            <div class="section-content">
              ${row(this.t('admin.symptoms'), symptoms ? `<div class="symptom-pills">${symptoms}</div>` : this.t('common.none'))}
              ${row(this.t('admin.other_symptoms'), this.safe(rec.otherSymptoms))}
              ${row(this.t('admin.allergies'), this.safe(this.formatList(rec.allergies ?? null, 'enum.allergy')))}
              ${row(this.t('admin.other_allergies'), this.safe(rec.otherAllergies))}
              ${row(this.t('admin.medications'), this.safe(this.formatList(rec.medications ?? null, 'enum.medication')))}
              ${row(this.t('admin.other_meds'), this.safe(rec.otherMedications))}
              ${row(this.t('admin.pre_existing'), this.safe(this.formatList(rec.preExistingConditions ?? null, 'enum.condition')))}
              ${row(this.t('admin.other_conditions'), this.safe(rec.otherPreExistingConditions))}
            </div>
          </section>

          <section class="section">
            <h2>${this.escapeHtml(this.t('report.clinical_assessment'))}</h2>
            <div class="section-content">
              ${row(this.t('admin.diagnosis'), this.safe(rec.diagnosis))}
              ${row(this.t('admin.required_medicine'), this.safe(rec.requiredMedicine))}
              ${row(this.t('admin.notes'), this.safe(rec.notes))}
            </div>
          </section>

          <p class="note">${this.escapeHtml(this.t('report.auto_note'))}</p>
        </div>
      </body>
      </html>
    `;
  }

  private toMessage(err: unknown): string {
    if (err instanceof Error) return err.message;
    if (err instanceof HttpErrorResponse) return err.message;
    return 'Request failed. Please try again.';
  }

  private autoHideToast(): void {
    timer(4500)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(() => this.toast.set(null));
  }
}

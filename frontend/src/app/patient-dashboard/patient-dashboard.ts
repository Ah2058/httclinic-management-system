import { HttpErrorResponse } from '@angular/common/http';
import {
  ChangeDetectionStrategy,
  Component,
  DestroyRef,
  inject,
  signal
} from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import {
  FormControl,
  NonNullableFormBuilder,
  ReactiveFormsModule,
  Validators
} from '@angular/forms';
import { finalize, timer } from 'rxjs';
import {
  Allergy,
  Medication,
  PatientFormPayload,
  PreExistingCondition,
  Symptom
} from './patient-form.model';
import { PatientFormService } from './patient-form.service';
import {
  digitsOnlyValidator,
  phoneValidator,
  uppercaseValidator
} from './validators';
import { LanguageService } from '../i18n/language.service';
import { SignaturePadComponent } from '../signature-pad/signature-pad';
import { Language } from '../i18n/translations';

type SubmitState = 'idle' | 'submitting' | 'success' | 'error';

interface ChipOption<T extends string> {
  value: T;
  labelKey: string;
  image?: string;
}

type ToastKind = 'success' | 'error' | 'info';
interface ToastState {
  kind: ToastKind;
  message: string;
}

const SYMPTOMS: readonly ChipOption<Symptom>[] = [
  { value: 'FEVER', labelKey: 'enum.symptom.FEVER', image: '/symptoms/fever.svg' },
  { value: 'COUGH', labelKey: 'enum.symptom.COUGH', image: '/symptoms/cough.svg' },
  {
    value: 'SHORTNESS_OF_BREATH',
    labelKey: 'enum.symptom.SHORTNESS_OF_BREATH',
    image: '/symptoms/breath.svg'
  },
  {
    value: 'HEADACHE',
    labelKey: 'enum.symptom.HEADACHE',
    image: '/symptoms/headache.svg'
  },
  {
    value: 'DIZZINESS',
    labelKey: 'enum.symptom.DIZZINESS',
    image: '/symptoms/dizziness.svg'
  },
  { value: 'NAUSEA', labelKey: 'enum.symptom.NAUSEA', image: '/symptoms/nausea.svg' },
  {
    value: 'CHEST_PAIN',
    labelKey: 'enum.symptom.CHEST_PAIN',
    image: '/symptoms/chest.svg'
  },
  { value: 'BACK_PAIN', labelKey: 'enum.symptom.BACK_PAIN', image: '/symptoms/back.svg' },
  { value: 'RASH', labelKey: 'enum.symptom.RASH', image: '/symptoms/rash.svg' }
] as const;

const ALLERGIES: readonly ChipOption<Allergy>[] = [
  { value: 'POLLEN', labelKey: 'enum.allergy.POLLEN' },
  { value: 'HOUSE_DUST', labelKey: 'enum.allergy.HOUSE_DUST' },
  { value: 'ANIMAL_HAIR', labelKey: 'enum.allergy.ANIMAL_HAIR' },
  { value: 'PENICILLIN', labelKey: 'enum.allergy.PENICILLIN' },
  { value: 'NUTS', labelKey: 'enum.allergy.NUTS' },
  { value: 'LATEX', labelKey: 'enum.allergy.LATEX' }
] as const;

const MEDICATIONS: readonly ChipOption<Medication>[] = [
  { value: 'IBUPROFEN', labelKey: 'enum.medication.IBUPROFEN' },
  { value: 'ASPIRIN', labelKey: 'enum.medication.ASPIRIN' },
  { value: 'INSULIN', labelKey: 'enum.medication.INSULIN' },
  { value: 'PARACETAMOL', labelKey: 'enum.medication.PARACETAMOL' },
  { value: 'METFORMIN', labelKey: 'enum.medication.METFORMIN' }
] as const;

const CONDITIONS: readonly ChipOption<PreExistingCondition>[] = [
  { value: 'DIABETES', labelKey: 'enum.condition.DIABETES' },
  { value: 'ASTHMA', labelKey: 'enum.condition.ASTHMA' },
  { value: 'HIGH_BLOOD_PRESSURE', labelKey: 'enum.condition.HIGH_BLOOD_PRESSURE' },
  { value: 'HEART_DISEASE', labelKey: 'enum.condition.HEART_DISEASE' },
  { value: 'THYROID', labelKey: 'enum.condition.THYROID' }
] as const;

@Component({
  selector: 'app-patient-dashboard',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [ReactiveFormsModule, SignaturePadComponent],
  templateUrl: './patient-dashboard.html',
  styleUrl: './patient-dashboard.css'
})
export class PatientDashboardComponent {
  private readonly fb = inject(NonNullableFormBuilder);
  private readonly patientFormService = inject(PatientFormService);
  private readonly destroyRef = inject(DestroyRef);
  protected readonly languageService = inject(LanguageService);

  protected readonly submitState = signal<SubmitState>('idle');
  protected readonly submitError = signal<string | null>(null);
  protected readonly lastResponseId = signal<number | null>(null);
  protected readonly toast = signal<ToastState | null>(null);
  protected currentLanguage = this.languageService.getCurrentLanguage();
  protected languages = this.languageService.getLanguages();

  protected readonly symptomsOptions = SYMPTOMS;
  protected readonly allergiesOptions = ALLERGIES;
  protected readonly medicationsOptions = MEDICATIONS;
  protected readonly conditionsOptions = CONDITIONS;

   protected readonly form = this.fb.group({
     firstName: this.fb.control('', {
       validators: [Validators.required, uppercaseValidator()]
     }),
     lastName: this.fb.control('', {
       validators: [Validators.required, uppercaseValidator()]
     }),
     dateOfBirth: this.fb.control('', { validators: [Validators.required] }),
     streetName: this.fb.control('', {
       validators: [Validators.required, uppercaseValidator()]
     }),
     streetNumber: this.fb.control('', {
       validators: [Validators.required, digitsOnlyValidator()]
     }),
     city: this.fb.control('', { validators: [Validators.required, uppercaseValidator()] }),
     postalCode: this.fb.control('', {
       validators: [Validators.required, digitsOnlyValidator()]
     }),
     phoneNumber: this.fb.control('', {
       validators: [Validators.required, phoneValidator()]
     }),
     emailAddress: this.fb.control('', { validators: [Validators.email] }),
     symptoms: this.fb.control<readonly Symptom[]>([]),
     otherSymptoms: this.fb.control(''),
     allergies: this.fb.control<readonly Allergy[]>([]),
     otherAllergies: this.fb.control(''),
     medications: this.fb.control<readonly Medication[]>([]),
     otherMedications: this.fb.control(''),
     preExistingConditions: this.fb.control<readonly PreExistingCondition[]>([]),
     otherPreExistingConditions: this.fb.control(''),
     signatureDataUrl: this.fb.control(''),
     dataProtectionAgreed: this.fb.control(false, { validators: [Validators.requiredTrue] })
   });

  constructor() {
    this.bindUppercase('firstName');
    this.bindUppercase('lastName');
    this.bindUppercase('streetName');
    this.bindUppercase('city');
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

  protected onResetForm(): void {
    this.form.setValue(this.emptyFormValue());
    this.form.markAsPristine();
    this.form.markAsUntouched();
    this.submitState.set('idle');
    this.submitError.set(null);
    this.lastResponseId.set(null);
    this.toast.set(null);
  }

  protected onToggleSymptom(value: Symptom): void {
    this.toggleArrayControl(this.form.controls.symptoms, value);
  }

  protected onToggleAllergy(value: Allergy): void {
    this.toggleArrayControl(this.form.controls.allergies, value);
  }

  protected onToggleMedication(value: Medication): void {
    this.toggleArrayControl(this.form.controls.medications, value);
  }

  protected onToggleCondition(value: PreExistingCondition): void {
    this.toggleArrayControl(this.form.controls.preExistingConditions, value);
  }

  protected isSelected<T extends string>(
    control: FormControl<readonly T[]>,
    value: T
  ): boolean {
    return control.value.includes(value);
  }

  protected onSignatureChange(dataUrl: string): void {
    this.form.controls.signatureDataUrl.setValue(dataUrl);
  }

  protected clearSignature(): void {
    this.form.controls.signatureDataUrl.setValue('');
  }

  protected onPrintPage(): void {
    window.print();
  }

   protected onSubmit(): void {
     this.form.markAllAsTouched();
     this.submitError.set(null);
     this.lastResponseId.set(null);
     this.toast.set(null);

     if (this.form.invalid) return;

     this.submitState.set('submitting');
     this.toast.set({ kind: 'info', message: this.t('patient.submitting') });
     const payload = this.toPayload();

     this.patientFormService
       .submit(payload)
       .pipe(
         finalize(() => {
           if (this.submitState() === 'submitting') this.submitState.set('idle');
         }),
         takeUntilDestroyed(this.destroyRef)
       )
       .subscribe({
         next: (resp) => {
           this.lastResponseId.set(resp.id);
           this.submitState.set('success');
           const referenceId = resp.id != null ? ` ${this.t('patient.reference_id')}: ${resp.id}` : '';
           this.toast.set({
             kind: 'success',
             message: `${this.t('patient.submitted')}${referenceId}`
           });
           timer(2500)
             .pipe(takeUntilDestroyed(this.destroyRef))
             .subscribe(() => this.onResetForm());
           this.autoHideToast();
         },
         error: (err: unknown) => {
           this.submitState.set('error');
           const msg = this.errorMessage(err);
           this.submitError.set(msg);
           this.toast.set({ kind: 'error', message: msg });
           this.autoHideToast();
         }
       });
   }

  protected dismissToast(): void {
    this.toast.set(null);
  }

  protected clearLocal(): void {
    this.patientFormService.clearLastSubmission();
    this.onResetForm();
  }

   private emptyFormValue(): {
     firstName: string;
     lastName: string;
     dateOfBirth: string;
     streetName: string;
     streetNumber: string;
     city: string;
     postalCode: string;
     phoneNumber: string;
     emailAddress: string;
     symptoms: readonly Symptom[];
     otherSymptoms: string;
     allergies: readonly Allergy[];
     otherAllergies: string;
     medications: readonly Medication[];
     otherMedications: string;
     preExistingConditions: readonly PreExistingCondition[];
     otherPreExistingConditions: string;
     signatureDataUrl: string;
     dataProtectionAgreed: boolean;
   } {
     return {
       firstName: '',
       lastName: '',
       dateOfBirth: '',
       streetName: '',
       streetNumber: '',
       city: '',
       postalCode: '',
       phoneNumber: '',
       emailAddress: '',
       symptoms: [],
       otherSymptoms: '',
       allergies: [],
       otherAllergies: '',
       medications: [],
       otherMedications: '',
       preExistingConditions: [],
       otherPreExistingConditions: '',
       signatureDataUrl: '',
       dataProtectionAgreed: false
     };
   }

   private toPayload(): PatientFormPayload {
     const raw = this.form.getRawValue();
     const trimOrNull = (v: string): string | null => {
       const t = v.trim();
       return t.length ? t : null;
     };
     const toOptionalArray = <T extends string>(arr: readonly T[]): T[] | null =>
       arr.length ? [...arr] : null;

     const emailTrimmed = raw.emailAddress.trim();

     return {
       firstName: raw.firstName.trim(),
       lastName: raw.lastName.trim(),
       dateOfBirth: raw.dateOfBirth,
       streetName: raw.streetName.trim(),
       streetNumber: raw.streetNumber.trim(),
       city: raw.city.trim(),
       postalCode: raw.postalCode.trim(),
       phoneNumber: raw.phoneNumber.trim(),
       ...(emailTrimmed.length ? { emailAddress: emailTrimmed } : {}),
       symptoms: toOptionalArray(raw.symptoms),
       otherSymptoms: trimOrNull(raw.otherSymptoms),
       allergies: toOptionalArray(raw.allergies),
       otherAllergies: trimOrNull(raw.otherAllergies),
       medications: toOptionalArray(raw.medications),
       otherMedications: trimOrNull(raw.otherMedications),
       preExistingConditions: toOptionalArray(raw.preExistingConditions),
       otherPreExistingConditions: trimOrNull(raw.otherPreExistingConditions),
       signatureDataUrl: trimOrNull(raw.signatureDataUrl),
       dataProtectionAgreed: raw.dataProtectionAgreed
     };
   }

  private toggleArrayControl<T extends string>(
    control: FormControl<readonly T[]>,
    value: T
  ): void {
    const set = new Set(control.value);
    if (set.has(value)) set.delete(value);
    else set.add(value);
    control.setValue([...set]);
    control.markAsDirty();
    control.markAsTouched();
  }

  private bindUppercase(
    key: 'firstName' | 'lastName' | 'streetName' | 'city'
  ): void {
    const control = this.form.controls[key];
    control.valueChanges.pipe(takeUntilDestroyed(this.destroyRef)).subscribe((v) => {
      const next = v.toUpperCase();
      if (v !== next) control.setValue(next, { emitEvent: false });
    });
  }

  private errorMessage(err: unknown): string {
    if (err instanceof Error) return err.message;
    if (err instanceof HttpErrorResponse) return err.message;
    return `${this.t('patient.error')}: request failed.`;
  }

  private autoHideToast(): void {
    timer(4500)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(() => this.toast.set(null));
  }
}

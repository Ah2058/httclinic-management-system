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
  NonNullableFormBuilder,
  ReactiveFormsModule,
  Validators
} from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { AuthService } from '../auth/auth.service';
import { LanguageService } from '../i18n/language.service';
import { timer } from 'rxjs';
import { Language } from '../i18n/translations';

type SubmitState = 'idle' | 'submitting' | 'error';

type ToastKind = 'success' | 'error' | 'info';
interface ToastState {
  kind: ToastKind;
  message: string;
}

@Component({
  selector: 'app-login',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [ReactiveFormsModule],
  templateUrl: './login.html',
  styleUrl: './login.css'
})
export class LoginComponent {
  private readonly fb = inject(NonNullableFormBuilder);
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);
  private readonly destroyRef = inject(DestroyRef);
  protected readonly languageService = inject(LanguageService);

  protected readonly submitState = signal<SubmitState>('idle');
  protected readonly errorMessage = signal<string | null>(null);
  protected readonly toast = signal<ToastState | null>(null);
  protected currentLanguage = this.languageService.getCurrentLanguage();
  protected languages = this.languageService.getLanguages();

  protected readonly form = this.fb.group({
    username: this.fb.control('', { validators: [Validators.required] }),
    password: this.fb.control('', { validators: [Validators.required] })
  });

  protected changeLanguage(lang: Language) {
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

   protected onSubmit(): void {
     this.errorMessage.set(null);
     if (this.form.invalid) {
       this.form.markAllAsTouched();
       return;
     }

     const { username, password } = this.form.getRawValue();
     this.submitState.set('submitting');
     this.toast.set({ kind: 'info', message: this.t('login.authenticating') });

     // Disable form to prevent double submission
     this.form.disable();

     this.auth
       .login(username.trim(), password)
       .pipe(takeUntilDestroyed(this.destroyRef))
       .subscribe({
         next: () => {
           this.submitState.set('idle');
           this.form.enable();
           this.toast.set({ kind: 'success', message: this.t('login.success') });

           // Navigate faster without extra delay
           const returnUrl =
             this.route.snapshot.queryParamMap.get('returnUrl') ??
             '/admin-dashboard';

           // Use setTimeout with 0 delay to navigate immediately after state updates
           setTimeout(() => {
             this.router.navigateByUrl(returnUrl);
           }, 100);
         },
         error: (err: unknown) => {
           this.submitState.set('error');
           this.form.enable();
           const msg = this.toMessage(err);
           this.errorMessage.set(msg);
           this.toast.set({ kind: 'error', message: msg });
           this.autoHideToast();
         }
       });
   }

  protected dismissToast(): void {
    this.toast.set(null);
  }

  protected onPrintPage(): void {
    window.print();
  }

  private toMessage(err: unknown): string {
    if (err instanceof Error) return err.message;
    if (err instanceof HttpErrorResponse) return err.message;
    return this.t('login.failed');
  }

  private autoHideToast(): void {
    timer(4000)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(() => this.toast.set(null));
  }
}

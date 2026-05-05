import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ReactiveFormsModule } from '@angular/forms';
import { Router, ActivatedRoute } from '@angular/router';
import { signal } from '@angular/core';
import { of, Subject, throwError } from 'rxjs';
import { LoginComponent } from './login';
import { AuthService } from '../auth/auth.service';
import { LanguageService } from '../i18n/language.service';
import { describe, it, expect, beforeEach, vi } from 'vitest';

describe('LoginComponent', () => {
  let component: LoginComponent;
  let fixture: ComponentFixture<LoginComponent>;
  let authService: { login: ReturnType<typeof vi.fn> };
  let router: { navigateByUrl: ReturnType<typeof vi.fn> };
  let languageService: any;

  beforeEach(async () => {
    const authServiceMock = {
      login: vi.fn()
    };
    const routerMock = {
      navigateByUrl: vi.fn()
    };
    const languageServiceMock = {
      getCurrentLanguage: vi.fn().mockReturnValue(signal('en')),
      getLanguages: vi.fn().mockReturnValue(['en', 'de']),
      setLanguage: vi.fn(),
      translate: vi.fn((key: string) => key)
    };
    const activatedRouteMock = {
      snapshot: {
        queryParamMap: {
          get: vi.fn().mockReturnValue(null)
        }
      }
    };

    await TestBed.configureTestingModule({
      imports: [LoginComponent, ReactiveFormsModule],
      providers: [
        { provide: AuthService, useValue: authServiceMock },
        { provide: Router, useValue: routerMock },
        { provide: LanguageService, useValue: languageServiceMock },
        { provide: ActivatedRoute, useValue: activatedRouteMock }
      ]
    }).compileComponents();

    authService = TestBed.inject(AuthService) as any;
    router = TestBed.inject(Router) as any;
    languageService = TestBed.inject(LanguageService);

    fixture = TestBed.createComponent(LoginComponent);
    component = fixture.componentInstance;
  });

  describe('Component Initialization', () => {
    it('should create the login component', () => {
      expect(component).toBeTruthy();
    });

    it('should initialize form with empty username and password', () => {
      expect(component['form'].get('username')?.value).toBe('');
      expect(component['form'].get('password')?.value).toBe('');
    });

    it('should initialize submitState as idle', () => {
      expect(component['submitState']()).toBe('idle');
    });

    it('should initialize errorMessage as null', () => {
      expect(component['errorMessage']()).toBeNull();
    });

    it('should initialize toast as null', () => {
      expect(component['toast']()).toBeNull();
    });
  });

  describe('Form Validation', () => {
    it('should have required validators on username field', () => {
      const usernameControl = component['form'].get('username');
      usernameControl?.setValue('');
      expect(usernameControl?.hasError('required')).toBeTruthy();
    });

    it('should have required validators on password field', () => {
      const passwordControl = component['form'].get('password');
      passwordControl?.setValue('');
      expect(passwordControl?.hasError('required')).toBeTruthy();
    });

    it('should mark form as invalid when username is empty', () => {
      component['form'].get('username')?.setValue('');
      component['form'].get('password')?.setValue('testpassword');
      expect(component['form'].invalid).toBeTruthy();
    });

    it('should mark form as invalid when password is empty', () => {
      component['form'].get('username')?.setValue('testuser');
      component['form'].get('password')?.setValue('');
      expect(component['form'].invalid).toBeTruthy();
    });

    it('should mark form as valid when all fields are filled', () => {
      component['form'].get('username')?.setValue('testuser');
      component['form'].get('password')?.setValue('testpassword');
      expect(component['form'].valid).toBeTruthy();
    });
  });

  describe('Login Submission', () => {
    beforeEach(() => {
      component['form'].get('username')?.setValue('testuser');
      component['form'].get('password')?.setValue('testpassword');
    });

    it('should not submit if form is invalid', () => {
      component['form'].get('username')?.setValue('');
      component['onSubmit']();
      expect(authService.login).not.toHaveBeenCalled();
    });

    it('should set submitState to submitting when form is valid', () => {
      const pendingLogin = new Subject<string>();
      authService.login.mockReturnValue(pendingLogin.asObservable());
      component['onSubmit']();
      expect(component['submitState']()).toBe('submitting');
    });

    it('should call authService.login with correct credentials', () => {
      authService.login.mockReturnValue(of('test-token'));
      component['onSubmit']();
      expect(authService.login).toHaveBeenCalledWith('testuser', 'testpassword');
    });

    it('should trim username before sending', () => {
      component['form'].get('username')?.setValue('  testuser  ');
      authService.login.mockReturnValue(of('test-token'));
      component['onSubmit']();
      expect(authService.login).toHaveBeenCalledWith('testuser', 'testpassword');
    });

    it('should set submitState to idle on successful login', async () => {
      authService.login.mockReturnValue(of('test-token'));
      component['onSubmit']();
      await new Promise((resolve) => setTimeout(resolve, 120));
      expect(component['submitState']()).toBe('idle');
    });

    it('should show success toast on successful login', async () => {
      authService.login.mockReturnValue(of('test-token'));
      component['onSubmit']();
      await new Promise((resolve) => setTimeout(resolve, 120));
      expect(component['toast']()?.kind).toBe('success');
    });

    it('should navigate to admin-dashboard on successful login', async () => {
      authService.login.mockReturnValue(of('test-token'));
      component['onSubmit']();
      await new Promise((resolve) => setTimeout(resolve, 120));
      expect(router.navigateByUrl).toHaveBeenCalledWith('/admin-dashboard');
    });

    it('should set submitState to error on login failure', async () => {
      const testError = new Error('Invalid credentials');
      authService.login.mockReturnValue(throwError(() => testError));
      component['onSubmit']();
      await new Promise((resolve) => setTimeout(resolve, 20));
      expect(component['submitState']()).toBe('error');
    });

    it('should show error toast on login failure', async () => {
      const testError = new Error('Invalid credentials');
      authService.login.mockReturnValue(throwError(() => testError));
      component['onSubmit']();
      await new Promise((resolve) => setTimeout(resolve, 20));
      expect(component['toast']()?.kind).toBe('error');
    });

    it('should clear error message before submission', async () => {
      component['errorMessage'].set('Previous error');
      authService.login.mockReturnValue(of('test-token'));
      component['onSubmit']();
      await new Promise((resolve) => setTimeout(resolve, 20));
      expect(component['errorMessage']()).toBeNull();
    });
  });

  describe('Language Switching', () => {
    it('should change language when changeLanguage is called', () => {
      component['changeLanguage']('de');
      expect(languageService.setLanguage).toHaveBeenCalledWith('de');
    });

    it('should translate keys using language service', () => {
      const translation = component['t']('login.title');
      expect(languageService.translate).toHaveBeenCalledWith('login.title');
    });
  });

  describe('Toast Dismissal', () => {
    it('should dismiss toast when dismissToast is called', () => {
      component['toast'].set({ kind: 'success', message: 'Test' });
      component['dismissToast']();
      expect(component['toast']()).toBeNull();
    });
  });

  describe('Print Functionality', () => {
    it('should call window.print when onPrintPage is called', () => {
      vi.spyOn(window, 'print').mockImplementation(() => {});
      component['onPrintPage']();
      expect(window.print).toHaveBeenCalled();
    });
  });
});


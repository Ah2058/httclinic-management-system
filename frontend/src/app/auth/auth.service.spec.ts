import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { AuthService, LoginResponse } from './auth.service';
import { describe, it, expect, beforeEach, afterEach, vi } from 'vitest';

describe('AuthService', () => {
  let service: AuthService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [AuthService]
    });

    service = TestBed.inject(AuthService);
    httpMock = TestBed.inject(HttpTestingController);

    // Clear localStorage before each test
    localStorage.clear();
  });

  afterEach(() => {
    httpMock.verify();
    localStorage.clear();
  });

  describe('Initialization', () => {
    it('should be created', () => {
      expect(service).toBeTruthy();
    });

    it('should initialize with no token', () => {
      expect(service.token()).toBeNull();
    });

    it('should initialize as not authenticated', () => {
      expect(service.isAuthenticated()).toBeFalsy();
    });

    it('should initialize with empty roles', () => {
      expect(service.roles()).toEqual([]);
    });
  });

  describe('Login', () => {
    const testToken = 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJyb2xlcyI6WyJST0xFX0FETUlOIl0sInN1YiI6InRlc3R1c2VyIn0.test';
    const loginCredentials = { username: 'testuser', password: 'testpassword' };

    it('should successfully login with valid credentials', async () => {
      const loginPromise = new Promise<string>((resolve, reject) => {
        service.login(loginCredentials.username, loginCredentials.password).subscribe({
          next: (token) => {
            expect(token).toBe(testToken);
            expect(service.isAuthenticated()).toBeTruthy();
            resolve(token);
          },
          error: (err) => reject(err)
        });
      });

      const req = httpMock.expectOne('/api/auth/login');
      expect(req.request.method).toBe('POST');
      expect(req.request.body).toEqual(loginCredentials);

      const response: LoginResponse = { token: testToken };
      req.flush(response);

      await loginPromise;
    });

    it('should store token in localStorage on successful login', async () => {
      const loginPromise = new Promise<void>((resolve, reject) => {
        service.login(loginCredentials.username, loginCredentials.password).subscribe({
          next: () => {
            expect(localStorage.getItem('auth:jwt')).toBe(testToken);
            resolve();
          },
          error: (err) => reject(err)
        });
      });

      const req = httpMock.expectOne('/api/auth/login');
      req.flush({ token: testToken });

      await loginPromise;
    });

    it('should update isAuthenticated signal after login', async () => {
      expect(service.isAuthenticated()).toBeFalsy();

      const loginPromise = new Promise<void>((resolve, reject) => {
        service.login(loginCredentials.username, loginCredentials.password).subscribe({
          next: () => {
            expect(service.isAuthenticated()).toBeTruthy();
            resolve();
          },
          error: (err) => reject(err)
        });
      });

      const req = httpMock.expectOne('/api/auth/login');
      req.flush({ token: testToken });

      await loginPromise;
    });

    it('should extract roles from token after login', async () => {
      const loginPromise = new Promise<void>((resolve, reject) => {
        service.login(loginCredentials.username, loginCredentials.password).subscribe({
          next: () => {
            expect(service.roles()).toContain('ROLE_ADMIN');
            resolve();
          },
          error: (err) => reject(err)
        });
      });

      const req = httpMock.expectOne('/api/auth/login');
      req.flush({ token: testToken });

      await loginPromise;
    });

    it('should handle login error', async () => {
      const errorPromise = new Promise<Error>((resolve, reject) => {
        service.login(loginCredentials.username, loginCredentials.password).subscribe({
          next: () => reject(new Error('should error')),
          error: (err) => {
            expect(err).toBeInstanceOf(Error);
            resolve(err);
          }
        });
      });

      const req = httpMock.expectOne('/api/auth/login');
      req.error(new ErrorEvent('Unauthorized'), { status: 401, statusText: 'Unauthorized' });

      await errorPromise;
    });

    it('should handle 401 unauthorized response', async () => {
      const errorPromise = new Promise<Error>((resolve, reject) => {
        service.login('wronguser', 'wrongpass').subscribe({
          next: () => reject(new Error('should error')),
          error: (err) => {
            expect(err.message).toContain('Invalid credentials');
            resolve(err);
          }
        });
      });

      const req = httpMock.expectOne('/api/auth/login');
      req.flush('Invalid credentials', { status: 401, statusText: 'Unauthorized' });

      await errorPromise;
    });

    it('should not set token in case of network error', async () => {
      const errorPromise = new Promise<void>((resolve, reject) => {
        service.login(loginCredentials.username, loginCredentials.password).subscribe({
          next: () => reject(new Error('should error')),
          error: () => {
            expect(service.token()).toBeNull();
            expect(service.isAuthenticated()).toBeFalsy();
            resolve();
          }
        });
      });

      const req = httpMock.expectOne('/api/auth/login');
      req.error(new ErrorEvent('Network error'));

      await errorPromise;
    });
  });

  describe('Logout', () => {
    it('should clear token on logout', () => {
      // Set a token first
      localStorage.setItem('auth:jwt', 'test-token');

      // Create a new service instance to load the token
      TestBed.resetTestingModule();
      TestBed.configureTestingModule({
        imports: [HttpClientTestingModule],
        providers: [AuthService]
      });
      const testService = TestBed.inject(AuthService);
      expect(testService.isAuthenticated()).toBeTruthy();

      // Logout
      testService.logout();

      expect(testService.isAuthenticated()).toBeFalsy();
      expect(testService.token()).toBeNull();
    });

    it('should remove token from localStorage on logout', () => {
      localStorage.setItem('auth:jwt', 'test-token');

      service.logout();

      expect(localStorage.getItem('auth:jwt')).toBeNull();
    });

    it('should handle logout when localStorage is unavailable', () => {
      vi.spyOn(localStorage, 'removeItem').mockImplementation(() => {
        throw new Error('QuotaExceededError');
      });

      // Should not throw
      expect(() => service.logout()).not.toThrow();
      expect(service.token()).toBeNull();
    });
  });

  describe('Token Management', () => {
    it('should load token from localStorage on initialization', () => {
      const testToken = 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJyb2xlcyI6WyJST0xFX0FETUlOIl0sInN1YiI6InRlc3R1c2VyIn0.test';
      localStorage.setItem('auth:jwt', testToken);

      // Create new service to test
      TestBed.resetTestingModule();
      TestBed.configureTestingModule({
        imports: [HttpClientTestingModule],
        providers: [AuthService]
      });

      const newService = TestBed.inject(AuthService);
      expect(newService.token()).toBe(testToken);
      expect(newService.isAuthenticated()).toBeTruthy();
    });

    it('should ignore empty token in localStorage', () => {
      localStorage.setItem('auth:jwt', '   ');

      TestBed.resetTestingModule();
      TestBed.configureTestingModule({
        imports: [HttpClientTestingModule],
        providers: [AuthService]
      });

      const newService = TestBed.inject(AuthService);
      expect(newService.token()).toBeNull();
      expect(newService.isAuthenticated()).toBeFalsy();
    });

    it('should handle localStorage access errors during initialization', () => {
      vi.spyOn(localStorage, 'getItem').mockImplementation(() => {
        throw new Error('SecurityError');
      });

      TestBed.resetTestingModule();
      TestBed.configureTestingModule({
        imports: [HttpClientTestingModule],
        providers: [AuthService]
      });

      const newService = TestBed.inject(AuthService);
      expect(newService.token()).toBeNull();
      expect(newService.isAuthenticated()).toBeFalsy();
    });
  });

  describe('Role Management', () => {
    const adminToken = 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJyb2xlcyI6WyJST0xFX0FETUlOIl0sInN1YiI6ImFkbWluIn0.test';
    const userToken = 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJyb2xlcyI6WyJST0xFX1VTRVIiXSwic3ViIjoidXNlcm1lIn0.test';

    it('should extract roles from JWT token', () => {
      service.login('admin', 'password').subscribe();

      const req = httpMock.expectOne('/api/auth/login');
      req.flush({ token: adminToken });

      expect(service.roles()).toContain('ROLE_ADMIN');
    });

    it('should check if user has specific role', () => {
      service.login('admin', 'password').subscribe();

      const req = httpMock.expectOne('/api/auth/login');
      req.flush({ token: adminToken });

      expect(service.hasRole('ROLE_ADMIN')).toBeTruthy();
      expect(service.hasRole('ROLE_USER')).toBeFalsy();
    });

    it('should check if user is admin', () => {
      service.login('admin', 'password').subscribe();

      const req = httpMock.expectOne('/api/auth/login');
      req.flush({ token: adminToken });

      expect(service.isAdmin()).toBeTruthy();
    });

    it('should return false for isAdmin when not admin', () => {
      service.login('user', 'password').subscribe();

      const req =httpMock.expectOne('/api/auth/login');
      req.flush({ token: userToken });

      expect(service.isAdmin()).toBeFalsy();
    });

    it('should return empty roles array for invalid token', async () => {
      // Set an invalid token
      const rolePromise = new Promise<void>((resolve, reject) => {
        service.login('test', 'test').subscribe({
          next: () => {
            // Invalid token payload
            expect(service.roles()).toEqual([]);
            expect(service.hasRole('ROLE_ADMIN')).toBeFalsy();
            resolve();
          },
          error: (err) => reject(err)
        });
      });

      const req = httpMock.expectOne('/api/auth/login');
      // Send back an invalid token format
      req.flush({ token: 'invalid.token.format' });

      await rolePromise;
    });

    it('should handle malformed JWT gracefully', async () => {
      const rolePromise = new Promise<void>((resolve, reject) => {
        service.login('test', 'test').subscribe({
          next: () => {
            expect(service.roles()).toEqual([]);
            resolve();
          },
          error: (err) => reject(err)
        });
      });

      const req = httpMock.expectOne('/api/auth/login');
      req.flush({ token: 'not-a-jwt' });

      await rolePromise;
    });
  });

  describe('Error Handling', () => {
    it('should normalize HttpErrorResponse with string message', async () => {
      const errorPromise = new Promise<Error>((resolve, reject) => {
        service.login('user', 'pass').subscribe({
          next: () => reject(new Error('should error')),
          error: (err) => {
            expect(err).toBeInstanceOf(Error);
            expect(err.message).toContain('Invalid');
            resolve(err);
          }
        });
      });

      const req = httpMock.expectOne('/api/auth/login');
      req.flush('Invalid credentials', { status: 401, statusText: 'Unauthorized' });

      await errorPromise;
    });

    it('should normalize HttpErrorResponse with default message', async () => {
      const errorPromise = new Promise<Error>((resolve, reject) => {
        service.login('user', 'pass').subscribe({
          next: () => reject(new Error('should error')),
          error: (err) => {
            expect(err).toBeInstanceOf(Error);
            resolve(err);
          }
        });
      });

      const req = httpMock.expectOne('/api/auth/login');
      req.error(new ErrorEvent('error'));

      await errorPromise;
    });
  });
});


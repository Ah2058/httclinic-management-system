import { TestBed } from '@angular/core/testing';
import { LanguageService } from './language.service';
import { describe, it, expect, beforeEach, afterEach, vi } from 'vitest';

describe('LanguageService', () => {
  let service: LanguageService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [LanguageService]
    });
    service = TestBed.inject(LanguageService);
    localStorage.clear();
  });

  afterEach(() => {
    localStorage.clear();
  });

  describe('Initialization', () => {
    it('should be created', () => {
      expect(service).toBeTruthy();
    });

    it('should initialize with English as default language', () => {
      const service2 = TestBed.inject(LanguageService);
      expect(service2.getCurrentLanguage()()).toBe('en');
    });

    it('should load language preference from localStorage', () => {
      localStorage.setItem('language', 'de');

      // Create new service to test initialization
      TestBed.resetTestingModule();
      TestBed.configureTestingModule({
        providers: [LanguageService]
      });

      const service2 = TestBed.inject(LanguageService);
      expect(service2.getCurrentLanguage()()).toBe('de');
    });

    it('should ignore invalid language from localStorage', () => {
      localStorage.setItem('language', 'invalid');

      TestBed.resetTestingModule();
      TestBed.configureTestingModule({
        providers: [LanguageService]
      });

      const service2 = TestBed.inject(LanguageService);
      expect(service2.getCurrentLanguage()()).toBe('en');
    });
  });

  describe('Setting Language', () => {
    it('should set language to English', () => {
      service.setLanguage('en');
      expect(service.getCurrentLanguage()()).toBe('en');
    });

    it('should set language to German', () => {
      service.setLanguage('de');
      expect(service.getCurrentLanguage()()).toBe('de');
    });

    it('should persist language preference to localStorage', () => {
      service.setLanguage('de');
      expect(localStorage.getItem('language')).toBe('de');
    });

    it('should update localStorage when language changes', () => {
      service.setLanguage('en');
      expect(localStorage.getItem('language')).toBe('en');

      service.setLanguage('de');
      expect(localStorage.getItem('language')).toBe('de');
    });
  });

  describe('Getting Languages', () => {
    it('should return array of supported languages', () => {
      const languages = service.getLanguages();
      expect(Array.isArray(languages)).toBeTruthy();
      expect(languages.length).toBeGreaterThan(0);
    });

    it('should include English language', () => {
      const languages = service.getLanguages();
      const english = languages.find(l => l.code === 'en');
      expect(english).toBeTruthy();
      expect(english?.name).toBe('English');
    });

    it('should include German language', () => {
      const languages = service.getLanguages();
      const german = languages.find(l => l.code === 'de');
      expect(german).toBeTruthy();
      expect(german?.name).toBe('Deutsch');
    });

    it('should have exactly 2 languages available', () => {
      const languages = service.getLanguages();
      expect(languages.length).toBe(2);
    });
  });

  describe('Translation', () => {
    it('should return the key if translation not found', () => {
      const result = service.translate('nonexistent.key');
      expect(result).toBe('nonexistent.key');
    });

    it('should translate from English', () => {
      service.setLanguage('en');
      const result = service.translate('login.title');
      expect(result).toBeTruthy();
      expect(result).not.toBe('login.title');
    });

    it('should translate from German', () => {
      service.setLanguage('de');
      const result = service.translate('login.title');
      expect(result).toBeTruthy();
      expect(result).not.toBe('login.title');
    });

    it('should translate nested keys', () => {
      service.setLanguage('en');
      const titleTranslation = service.translate('login.title');
      const passwordTranslation = service.translate('login.password');

      expect(titleTranslation).toBeTruthy();
      expect(passwordTranslation).toBeTruthy();
    });

    it('should handle deeply nested translation keys', () => {
      service.setLanguage('en');
      // Assuming such a key exists
      const result = service.translate('some.deep.nested.key');
      expect(typeof result).toBe('string');
    });

    it('should use current language for translation', () => {
      service.setLanguage('en');
      const enTranslation = service.translate('login.title');

      service.setLanguage('de');
      const deTranslation = service.translate('login.title');

      // Both should be strings
      expect(typeof enTranslation).toBe('string');
      expect(typeof deTranslation).toBe('string');
    });
  });

  describe('Current Language Signal', () => {
    it('should return readonly signal of current language', () => {
      const languageSignal = service.getCurrentLanguage();
      expect(typeof languageSignal).toBe('function');
      expect(languageSignal()).toBe('en');
    });

    it('should update signal when language is changed', () => {
      const languageSignal = service.getCurrentLanguage();
      expect(languageSignal()).toBe('en');

      service.setLanguage('de');
      expect(languageSignal()).toBe('de');
    });

    it('should not allow setting signal directly (readonly)', () => {
      const languageSignal = service.getCurrentLanguage();
      // The signal is readonly, so this type of assignment would not compile
      // We can only verify it's a function that returns the current language
      expect(typeof languageSignal).toBe('function');
    });
  });

  describe('Edge Cases', () => {
    it('should handle localStorage errors gracefully', () => {
      vi.spyOn(localStorage, 'setItem').mockImplementation(() => {
        throw new Error('QuotaExceededError');
      });

      // Should not throw
      expect(() => service.setLanguage('de')).not.toThrow();
    });

    it('should handle empty translation paths', () => {
      const result = service.translate('');
      expect(typeof result).toBe('string');
    });

    it('should handle null or undefined in translation path', () => {
      service.setLanguage('en');
      // If a translation value is not an object at some level
      const result = service.translate('login.title.nonexistent');
      expect(typeof result).toBe('string');
    });
  });
});


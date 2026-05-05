import { Injectable, signal } from '@angular/core';
import { TRANSLATIONS, type Language } from './translations';

@Injectable({ providedIn: 'root' })
export class LanguageService {
  private currentLanguage = signal<Language>('en');
  private translationCache = new Map<string, string>();
  private cacheVersion = 0;

  constructor() {
    // Load saved language preference
    const saved = localStorage.getItem('language') as Language;
    if (saved && (saved === 'en' || saved === 'de')) {
      this.currentLanguage.set(saved);
    }
  }

  getCurrentLanguage() {
    return this.currentLanguage.asReadonly();
  }

  setLanguage(lang: Language) {
    this.currentLanguage.set(lang);
    localStorage.setItem('language', lang);
    // Clear cache on language change
    this.translationCache.clear();
    this.cacheVersion++;
  }

  translate(key: string): string {
    // Check cache first for faster lookups
    const cached = this.translationCache.get(key);
    if (cached !== undefined) {
      return cached;
    }

    const lang = this.currentLanguage();
    const keys = key.split('.');
    let value: any = TRANSLATIONS[lang];

    for (const k of keys) {
      if (value && typeof value === 'object' && k in value) {
        value = value[k];
      } else {
        this.translationCache.set(key, key);
        return key; // Fallback to key if translation not found
      }
    }

    const result = typeof value === 'string' ? value : key;
    // Cache the result
    this.translationCache.set(key, result);
    return result;
  }

  getLanguages(): Array<{ code: Language; name: string }> {
    return [
      { code: 'en', name: 'English' },
      { code: 'de', name: 'Deutsch' }
    ];
  }
}


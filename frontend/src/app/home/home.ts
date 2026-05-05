import { Component, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { LanguageService } from '../i18n/language.service';
import { CommonModule } from '@angular/common';
import { Language } from '../i18n/translations';

@Component({
  selector: 'app-home',
  imports: [RouterLink, CommonModule],
  templateUrl: './home.html',
  styleUrl: './home.css'
})
export class HomeComponent {
  protected languageService = inject(LanguageService);
  protected currentLanguage = this.languageService.getCurrentLanguage();
  protected languages = this.languageService.getLanguages();

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

  protected onPrintPage(): void {
    window.print();
  }
}


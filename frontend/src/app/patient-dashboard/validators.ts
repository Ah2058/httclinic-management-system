import { AbstractControl, ValidationErrors, ValidatorFn } from '@angular/forms';

export function uppercaseValidator(): ValidatorFn {
  return (control: AbstractControl<string>): ValidationErrors | null => {
    const value = control.value;
    if (!value) return null;

    return value === value.toUpperCase() ? null : { uppercase: true };
  };
}

export function digitsOnlyValidator(): ValidatorFn {
  const re = /^\d+$/;
  return (control: AbstractControl<string>): ValidationErrors | null => {
    const value = control.value;
    if (!value) return null;
    return re.test(value) ? null : { digitsOnly: true };
  };
}

export function phoneValidator(): ValidatorFn {
  // Allows digits plus optional +, spaces, dashes.
  const re = /^[\d+\-\s]+$/;
  return (control: AbstractControl<string>): ValidationErrors | null => {
    const value = control.value;
    if (!value) return null;
    return re.test(value) ? null : { phone: true };
  };
}


import {
  Allergy,
  Medication,
  PreExistingCondition,
  Symptom
} from '../patient-dashboard/patient-form.model';

export type FormStatus = 'new' | 'viewed' | 'done';

export interface AdminUpdatePayload {
  diagnosis?: string | null;
  notes?: string | null;
  requiredMedicine?: string | null;
  status?: FormStatus;
}

export interface PatientFormRecord {
  id: number;

  firstName: string;
  lastName: string;
  dateOfBirth: string;
  streetName: string;
  streetNumber: string;
  city: string;
  postalCode: string;
  phoneNumber: string;
  emailAddress?: string | null;

  symptoms?: Symptom[] | null;
  otherSymptoms?: string | null;
  allergies?: Allergy[] | null;
  otherAllergies?: string | null;
  medications?: Medication[] | null;
  otherMedications?: string | null;
    preExistingConditions?: PreExistingCondition[] | null;
    otherPreExistingConditions?: string | null;

    diagnosis?: string | null;
    notes?: string | null;
    requiredMedicine?: string | null;
    signatureDataUrl?: string | null;
    status?: FormStatus;
}




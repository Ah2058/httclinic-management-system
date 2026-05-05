export type Symptom =
  | 'FEVER'
  | 'COUGH'
  | 'SHORTNESS_OF_BREATH'
  | 'HEADACHE'
  | 'DIZZINESS'
  | 'NAUSEA'
  | 'CHEST_PAIN'
  | 'BACK_PAIN'
  | 'RASH';

export type Allergy =
  | 'POLLEN'
  | 'HOUSE_DUST'
  | 'ANIMAL_HAIR'
  | 'PENICILLIN'
  | 'NUTS'
  | 'LATEX';

export type Medication =
  | 'IBUPROFEN'
  | 'ASPIRIN'
  | 'INSULIN'
  | 'PARACETAMOL'
  | 'METFORMIN';

export type PreExistingCondition =
  | 'DIABETES'
  | 'ASTHMA'
  | 'HIGH_BLOOD_PRESSURE'
  | 'HEART_DISEASE'
  | 'THYROID';

export interface PatientFormPayload {
  firstName: string;
  lastName: string;
  dateOfBirth: string;
  streetName: string;
  streetNumber: string;
  city: string;
  postalCode: string;
  phoneNumber: string;
  emailAddress?: string;
  symptoms?: Symptom[] | null;
  otherSymptoms?: string | null;
  allergies?: Allergy[] | null;
  otherAllergies?: string | null;
  medications?: Medication[] | null;
  otherMedications?: string | null;
  preExistingConditions?: PreExistingCondition[] | null;
  otherPreExistingConditions?: string | null;
  signatureDataUrl?: string | null;
  dataProtectionAgreed: boolean;
}

export interface PatientFormResponse extends PatientFormPayload {
  id: number;
  diagnosis?: string | null;
  notes?: string | null;
}


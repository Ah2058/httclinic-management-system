# 📋 Patient Form JSON Examples

## Example 1: Simple Patient (Minimal Data)

```json
{
  "firstName": "MICHAEL",
  "lastName": "JOHNSON",
  "dateOfBirth": "1988-07-12",
  "streetName": "QUEENS",
  "streetNumber": "789",
  "city": "OTTAWA",
  "postalCode": "98765",
  "phoneNumber": "+1-613-555-4321",
  "emailAddress": "michael.johnson@example.com"
}
```

**Response (200 OK):**
```json
{
  "id": 2,
  "firstName": "MICHAEL",
  "lastName": "JOHNSON",
  "dateOfBirth": "1988-07-12",
  "streetName": "QUEENS",
  "streetNumber": "789",
  "city": "OTTAWA",
  "postalCode": "98765",
  "phoneNumber": "+1-613-555-4321",
  "emailAddress": "michael.johnson@example.com",
  "symptoms": null,
  "otherSymptoms": null,
  "allergies": null,
  "otherAllergies": null,
  "medications": null,
  "otherMedications": null,
  "preExistingConditions": null,
  "otherPreExistingConditions": null,
  "diagnosis": null,
  "notes": null
}
```

---

## Example 2: Patient with Symptoms & Allergies

```json
{
  "firstName": "SARAH",
  "lastName": "WILLIAMS",
  "dateOfBirth": "1995-02-18",
  "streetName": "PINE",
  "streetNumber": "234",
  "city": "CALGARY",
  "postalCode": "55555",
  "phoneNumber": "+1-403-555-6789",
  "emailAddress": "sarah.w@example.com",
  "symptoms": ["HEADACHE", "DIZZINESS", "NAUSEA"],
  "otherSymptoms": "Light sensitivity",
  "allergies": ["NUTS", "HOUSE_DUST"],
  "otherAllergies": "Sulfa drugs"
}
```

**Response (200 OK):**
```json
{
  "id": 3,
  "firstName": "SARAH",
  "lastName": "WILLIAMS",
  "dateOfBirth": "1995-02-18",
  "streetName": "PINE",
  "streetNumber": "234",
  "city": "CALGARY",
  "postalCode": "55555",
  "phoneNumber": "+1-403-555-6789",
  "emailAddress": "sarah.w@example.com",
  "symptoms": ["HEADACHE", "DIZZINESS", "NAUSEA"],
  "otherSymptoms": "Light sensitivity",
  "allergies": ["NUTS", "HOUSE_DUST"],
  "otherAllergies": "Sulfa drugs",
  "medications": null,
  "otherMedications": null,
  "preExistingConditions": null,
  "otherPreExistingConditions": null,
  "diagnosis": null,
  "notes": null
}
```

---

## Example 3: Senior Patient with Multiple Conditions

```json
{
  "firstName": "ROBERT",
  "lastName": "BROWN",
  "dateOfBirth": "1950-11-25",
  "streetName": "ELM",
  "streetNumber": "567",
  "city": "WINNIPEG",
  "postalCode": "77777",
  "phoneNumber": "+1-204-555-1111",
  "emailAddress": "r.brown@example.com",
  "symptoms": ["CHEST_PAIN", "SHORTNESS_OF_BREATH"],
  "otherSymptoms": "Occasional dizziness when standing",
  "allergies": ["PENICILLIN", "LATEX"],
  "otherAllergies": "ACE inhibitors cause cough",
  "medications": ["ASPIRIN", "INSULIN", "METFORMIN"],
  "otherMedications": "Lisinopril 10mg daily, Atorvastatin 20mg daily",
  "preExistingConditions": ["DIABETES", "HIGH_BLOOD_PRESSURE", "HEART_DISEASE"],
  "otherPreExistingConditions": "History of stroke 5 years ago"
}
```

**Response (200 OK):**
```json
{
  "id": 4,
  "firstName": "ROBERT",
  "lastName": "BROWN",
  "dateOfBirth": "1950-11-25",
  "streetName": "ELM",
  "streetNumber": "567",
  "city": "WINNIPEG",
  "postalCode": "77777",
  "phoneNumber": "+1-204-555-1111",
  "emailAddress": "r.brown@example.com",
  "symptoms": ["CHEST_PAIN", "SHORTNESS_OF_BREATH"],
  "otherSymptoms": "Occasional dizziness when standing",
  "allergies": ["PENICILLIN", "LATEX"],
  "otherAllergies": "ACE inhibitors cause cough",
  "medications": ["ASPIRIN", "INSULIN", "METFORMIN"],
  "otherMedications": "Lisinopril 10mg daily, Atorvastatin 20mg daily",
  "preExistingConditions": ["DIABETES", "HIGH_BLOOD_PRESSURE", "HEART_DISEASE"],
  "otherPreExistingConditions": "History of stroke 5 years ago",
  "diagnosis": null,
  "notes": null
}
```

---

## Example 4: Young Patient with Respiratory Issues

```json
{
  "firstName": "EMILY",
  "lastName": "DAVIS",
  "dateOfBirth": "2005-09-30",
  "streetName": "MAPLE",
  "streetNumber": "345",
  "city": "MONTREAL",
  "postalCode": "33333",
  "phoneNumber": "+1-514-555-2222",
  "emailAddress": "emily.davis@example.com",
  "symptoms": ["FEVER", "COUGH", "SHORTNESS_OF_BREATH"],
  "otherSymptoms": "Mild chest discomfort when coughing",
  "allergies": ["POLLEN", "ANIMAL_HAIR"],
  "otherAllergies": "",
  "medications": ["IBUPROFEN"],
  "otherMedications": "Albuterol inhaler as needed",
  "preExistingConditions": ["ASTHMA"],
  "otherPreExistingConditions": ""
}
```

**Response (200 OK):**
```json
{
  "id": 5,
  "firstName": "EMILY",
  "lastName": "DAVIS",
  "dateOfBirth": "2005-09-30",
  "streetName": "MAPLE",
  "streetNumber": "345",
  "city": "MONTREAL",
  "postalCode": "33333",
  "phoneNumber": "+1-514-555-2222",
  "emailAddress": "emily.davis@example.com",
  "symptoms": ["FEVER", "COUGH", "SHORTNESS_OF_BREATH"],
  "otherSymptoms": "Mild chest discomfort when coughing",
  "allergies": ["POLLEN", "ANIMAL_HAIR"],
  "otherAllergies": "",
  "medications": ["IBUPROFEN"],
  "otherMedications": "Albuterol inhaler as needed",
  "preExistingConditions": ["ASTHMA"],
  "otherPreExistingConditions": "",
  "diagnosis": null,
  "notes": null
}
```

---

## Example 5: Patient with Rash and Multiple Allergies

```json
{
  "firstName": "DAVID",
  "lastName": "MILLER",
  "dateOfBirth": "1992-04-08",
  "streetName": "BIRCH",
  "streetNumber": "654",
  "city": "QUEBEC",
  "postalCode": "44444",
  "phoneNumber": "+1-418-555-3333",
  "emailAddress": "david.miller@example.com",
  "symptoms": ["RASH", "HEADACHE", "FEVER"],
  "otherSymptoms": "Itching all over body",
  "allergies": ["NUTS", "POLLEN", "LATEX", "ANIMAL_HAIR"],
  "otherAllergies": "Shellfish, Eggs",
  "medications": ["PARACETAMOL"],
  "otherMedications": "Antihistamine cream (hydrocortisone 1%)",
  "preExistingConditions": [],
  "otherPreExistingConditions": "Eczema history"
}
```

**Response (200 OK):**
```json
{
  "id": 6,
  "firstName": "DAVID",
  "lastName": "MILLER",
  "dateOfBirth": "1992-04-08",
  "streetName": "BIRCH",
  "streetNumber": "654",
  "city": "QUEBEC",
  "postalCode": "44444",
  "phoneNumber": "+1-418-555-3333",
  "emailAddress": "david.miller@example.com",
  "symptoms": ["RASH", "HEADACHE", "FEVER"],
  "otherSymptoms": "Itching all over body",
  "allergies": ["NUTS", "POLLEN", "LATEX", "ANIMAL_HAIR"],
  "otherAllergies": "Shellfish, Eggs",
  "medications": ["PARACETAMOL"],
  "otherMedications": "Antihistamine cream (hydrocortisone 1%)",
  "preExistingConditions": [],
  "otherPreExistingConditions": "Eczema history",
  "diagnosis": null,
  "notes": null
}
```

---

## Example 6: Adult with Back Pain

```json
{
  "firstName": "PATRICIA",
  "lastName": "MARTINEZ",
  "dateOfBirth": "1975-06-14",
  "streetName": "CEDAR",
  "streetNumber": "876",
  "city": "HAMILTON",
  "postalCode": "66666",
  "phoneNumber": "+1-905-555-4444",
  "emailAddress": "p.martinez@example.com",
  "symptoms": ["BACK_PAIN", "HEADACHE"],
  "otherSymptoms": "Stiffness in lower back, especially in morning",
  "allergies": ["HOUSE_DUST"],
  "otherAllergies": "",
  "medications": ["IBUPROFEN"],
  "otherMedications": "Muscle relaxant (Cyclobenzaprine) as needed",
  "preExistingConditions": ["HIGH_BLOOD_PRESSURE"],
  "otherPreExistingConditions": "Chronic lower back pain"
}
```

---

## Postman Request Template

**URL:** `http://localhost:8081/api/submit/api/forms`

**Method:** `POST`

**Headers:**
```
Content-Type: application/json
```

**Body (Copy one of the examples above):**

---

## Valid Enum Values

### Symptoms Options:
```
"FEVER"
"COUGH"
"SHORTNESS_OF_BREATH"
"HEADACHE"
"DIZZINESS"
"NAUSEA"
"CHEST_PAIN"
"BACK_PAIN"
"RASH"
```

### Allergies Options:
```
"POLLEN"
"HOUSE_DUST"
"ANIMAL_HAIR"
"PENICILLIN"
"NUTS"
"LATEX"
```

### Medications Options:
```
"IBUPROFEN"
"ASPIRIN"
"INSULIN"
"PARACETAMOL"
"METFORMIN"
```

### Pre-Existing Conditions Options:
```
"DIABETES"
"ASTHMA"
"HIGH_BLOOD_PRESSURE"
"HEART_DISEASE"
"THYROID"
```

---

## Tips for Testing

✅ **Use uppercase for names and addresses**
✅ **Date format must be YYYY-MM-DD**
✅ **Phone format: +1-XXX-XXX-XXXX**
✅ **Email must be valid**
✅ **Arrays can be empty [] or omitted**
✅ **"otherXXX" fields are optional text**

---

## Common Testing Scenarios

| Scenario | Focus | Example # |
|----------|-------|-----------|
| Minimal data | No medical history | Example 1 |
| Acute symptoms | Immediate complaint | Example 2 |
| Complex case | Multiple conditions | Example 3 |
| Young patient | Asthma management | Example 4 |
| Allergic reaction | Multiple allergies | Example 5 |
| Chronic pain | Long-term condition | Example 6 |



package infrax.teama.submit_service.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Pattern;
import lombok.*;
import java.time.LocalDate;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PatientForm {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Personal Information
    @Column(nullable = false)
    @Pattern(regexp = "^[A-ZÄÖÜ ]+$", message = "First name must be uppercase letters only")
    private String firstName;

    @Column(nullable = false)
    @Pattern(regexp = "^[A-ZÄÖÉÈÀÂÊÎÔÛÙÏÜ ]+$", message = "Last name must be uppercase letters only")
    private String lastName;

    @Column(nullable = false)
    private LocalDate dateOfBirth;

    @Column(nullable = false)
    @Pattern(regexp = "^[A-ZÄÖÜ0-9 ]+$", message = "Street name must be uppercase letters only")
    private String streetName;

    @Column(nullable = false)
    private String streetNumber;

    @Column(nullable = false)
    @Pattern(regexp = "^[A-ZÄÖÜ ]+$", message = "City must be uppercase letters only")
    private String city;

    @Column(nullable = false)
    private String postalCode;

    @Column(nullable = false)
    private String phoneNumber;

    private String emailAddress;

    // Medical Information
    @ElementCollection(fetch = FetchType.EAGER)
    private List<Symptom> symptoms;
    private String otherSymptoms;

    @ElementCollection(fetch = FetchType.EAGER)
    private List<Allergy> allergies;
    private String otherAllergies;

    @ElementCollection(fetch = FetchType.EAGER)
    private List<Medication> medications;
    private String otherMedications;

    @ElementCollection(fetch = FetchType.EAGER)
    private List<PreExistingCondition> preExistingConditions;
    private String otherPreExistingConditions;


    // Admin fields
    private String diagnosis;
    private String notes;
    private String requiredMedicine;

    @Builder.Default
    @Column(nullable = false)
    private String status = "new";  // new, viewed, done

    @PrePersist
    void ensureDefaults() {
        if (status == null || status.isBlank()) {
            status = "new";
        }
    }

    public enum Symptom {
        FEVER, COUGH, SHORTNESS_OF_BREATH, HEADACHE, DIZZINESS, NAUSEA, CHEST_PAIN, BACK_PAIN, RASH
    }
    public enum Allergy {
        POLLEN, HOUSE_DUST, ANIMAL_HAIR, PENICILLIN, NUTS, LATEX
    }
    public enum Medication {
        IBUPROFEN, ASPIRIN, INSULIN, PARACETAMOL, METFORMIN
    }
    public enum PreExistingCondition {
        DIABETES, ASTHMA, HIGH_BLOOD_PRESSURE, HEART_DISEASE, THYROID
    }
}

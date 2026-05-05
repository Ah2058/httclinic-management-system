package infrax.teama.submit_service.dto;

import infrax.teama.submit_service.model.PatientForm;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PatientFormResponseDto {
    private Long id;

    // Personal Information
    private String firstName;
    private String lastName;
    private LocalDate dateOfBirth;
    private String streetName;
    private String streetNumber;
    private String city;
    private String postalCode;
    private String phoneNumber;
    private String emailAddress;

    // Medical Information
    private List<String> symptoms;
    private String otherSymptoms;
    private List<String> allergies;
    private String otherAllergies;
    private List<String> medications;
    private String otherMedications;
    private List<String> preExistingConditions;
    private String otherPreExistingConditions;

    // Admin fields
    private String diagnosis;
    private String notes;
    private String requiredMedicine;
    private String status;

    public static PatientFormResponseDto fromEntity(PatientForm form) {
        return PatientFormResponseDto.builder()
                .id(form.getId())
                .firstName(form.getFirstName())
                .lastName(form.getLastName())
                .dateOfBirth(form.getDateOfBirth())
                .streetName(form.getStreetName())
                .streetNumber(form.getStreetNumber())
                .city(form.getCity())
                .postalCode(form.getPostalCode())
                .phoneNumber(form.getPhoneNumber())
                .emailAddress(form.getEmailAddress())
                .symptoms(convertEnumList(form.getSymptoms()))
                .otherSymptoms(form.getOtherSymptoms())
                .allergies(convertEnumList(form.getAllergies()))
                .otherAllergies(form.getOtherAllergies())
                .medications(convertEnumList(form.getMedications()))
                .otherMedications(form.getOtherMedications())
                .preExistingConditions(convertEnumList(form.getPreExistingConditions()))
                .otherPreExistingConditions(form.getOtherPreExistingConditions())
                .diagnosis(form.getDiagnosis())
                .notes(form.getNotes())
                .requiredMedicine(form.getRequiredMedicine())
                .status(form.getStatus())
                .build();
    }

    private static <E extends Enum<?>> List<String> convertEnumList(List<E> enumList) {
        if (enumList == null || enumList.isEmpty()) {
            return null;
        }
        return enumList.stream()
                .filter(e -> e != null)
                .map(Enum::name)
                .toList();
    }
}


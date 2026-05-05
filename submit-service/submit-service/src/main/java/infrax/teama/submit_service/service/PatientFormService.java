package infrax.teama.submit_service.service;

import infrax.teama.submit_service.dto.AdminUpdateRequest;
import infrax.teama.submit_service.dto.PatientFormRequest;
import infrax.teama.submit_service.model.PatientForm;
import infrax.teama.submit_service.repository.PatientFormRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PatientFormService {
    private final PatientFormRepository repository;

    @Transactional
    public PatientForm submitForm(PatientFormRequest request) {
        PatientForm form = PatientForm.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .dateOfBirth(request.getDateOfBirth())
                .streetName(request.getStreetName())
                .streetNumber(request.getStreetNumber())
                .city(request.getCity())
                .postalCode(request.getPostalCode())
                .phoneNumber(request.getPhoneNumber())
                .emailAddress(request.getEmailAddress())
                .symptoms(request.getSymptoms())
                .otherSymptoms(request.getOtherSymptoms())
                .allergies(request.getAllergies())
                .otherAllergies(request.getOtherAllergies())
                .medications(request.getMedications())
                .otherMedications(request.getOtherMedications())
                .preExistingConditions(request.getPreExistingConditions())
                .otherPreExistingConditions(request.getOtherPreExistingConditions())
                .status("new")
                .build();
        log.info("Submitting new form for patient: {} {}", request.getFirstName(), request.getLastName());
        return repository.save(form);
    }

    public List<PatientForm> getAllForms() {
        return repository.findAll();
    }

    public Optional<PatientForm> getForm(Long id) {
        return repository.findById(id);
    }

    @Transactional
    public Optional<PatientForm> updateAdminFields(Long id, AdminUpdateRequest req) {
        log.info("Updating admin fields for form ID: {}, status: {}", id, req.getStatus());
        return repository.findById(id).map(form -> {
            form.setDiagnosis(req.getDiagnosis());
            form.setNotes(req.getNotes());
            form.setRequiredMedicine(req.getRequiredMedicine());
            if (req.getStatus() != null) {
                log.info("Setting status from {} to {}", form.getStatus(), req.getStatus());
                form.setStatus(req.getStatus());
            }
            PatientForm saved = repository.save(form);
            log.info("Form {} updated successfully with status: {}", id, saved.getStatus());
            return saved;
        });
    }
}


package infrax.teama.submit_service.repository;

import infrax.teama.submit_service.model.PatientForm;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PatientFormRepository extends JpaRepository<PatientForm, Long> {
}


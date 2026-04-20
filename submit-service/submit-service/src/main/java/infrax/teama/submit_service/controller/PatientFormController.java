package infrax.teama.submit_service.controller;

import infrax.teama.submit_service.dto.AdminUpdateRequest;
import infrax.teama.submit_service.dto.PatientFormRequest;
import infrax.teama.submit_service.model.PatientForm;
import infrax.teama.submit_service.service.PatientFormService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/forms")
@RequiredArgsConstructor
public class PatientFormController {
    private final PatientFormService service;

    @PostMapping
    public ResponseEntity<PatientForm> submitForm(@Valid @RequestBody PatientFormRequest request) {
        PatientForm saved = service.submitForm(request);
        return ResponseEntity.ok(saved);
    }

    // Admin: get all forms
    @GetMapping
    public ResponseEntity<List<PatientForm>> getAllForms() {
        return ResponseEntity.ok(service.getAllForms());
    }

    // Admin: update diagnosis and notes
    @PatchMapping("/{id}/admin")
    public ResponseEntity<PatientForm> updateAdminFields(@PathVariable Long id, @RequestBody AdminUpdateRequest req) {
        return service.updateAdminFields(id, req)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}


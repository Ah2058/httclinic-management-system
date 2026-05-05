package infrax.teama.submit_service.controller;

import infrax.teama.submit_service.dto.AdminUpdateRequest;
import infrax.teama.submit_service.dto.PatientFormRequest;
import infrax.teama.submit_service.dto.PatientFormResponseDto;
import infrax.teama.submit_service.model.PatientForm;
import infrax.teama.submit_service.service.PatientFormService;
import infrax.teama.submit_service.service.ReportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

//@CrossOrigin(origins = "http://localhost:4200")
@Slf4j
@RestController
@RequestMapping("/api/submit/forms")
@RequiredArgsConstructor
public class PatientFormController {

    private final PatientFormService service;
    private final ReportService reportService;

    // Patient: submit form - NO login required
    @PostMapping
    public ResponseEntity<PatientFormResponseDto> submitForm(
            @Valid @RequestBody PatientFormRequest request) {
        log.info("Received form submission request: {}", request);
        PatientForm saved = service.submitForm(request);
        log.info("Form submitted successfully with ID: {}", saved.getId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(PatientFormResponseDto.fromEntity(saved));
    }

    // Admin: get all forms
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<List<PatientFormResponseDto>> getAllForms() {
        log.info("Fetching all patient forms");
        List<PatientForm> forms = service.getAllForms();
        return ResponseEntity.ok(forms.stream()
                .map(PatientFormResponseDto::fromEntity)
                .toList());
    }

    // Admin: get single form
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<PatientFormResponseDto> getForm(@PathVariable Long id) {
        log.info("Fetching form with ID: {}", id);
        try {
            return service.getForm(id)
                    .map(form -> ResponseEntity.ok(PatientFormResponseDto.fromEntity(form)))
                    .orElseGet(() -> {
                        log.warn("Form with ID {} not found", id);
                        return ResponseEntity.notFound().build();
                    });
        } catch (Exception e) {
            log.error("Error fetching form with ID {}: {}", id, e.getMessage(), e);
            throw e;
        }
    }

    // Admin: update diagnosis and notes
    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}/admin")
    public ResponseEntity<PatientFormResponseDto> updateAdminFields(
            @PathVariable Long id,
            @RequestBody AdminUpdateRequest req) {
        log.info("Updating admin fields for form ID: {}", id);
        return service.updateAdminFields(id, req)
                .map(form -> ResponseEntity.ok(PatientFormResponseDto.fromEntity(form)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Admin: export PDF
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}/report.pdf")
    public ResponseEntity<byte[]> getReportPdf(
            @PathVariable Long id,
            @RequestParam(value = "lang", defaultValue = "en") String language) {
        log.info("Generating PDF report for form ID: {} with language: {}", id, language);
        return reportService.generatePatientFormReportPdf(id, language)
                .map(pdfBytes -> ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_PDF_VALUE)
                        .header(HttpHeaders.CONTENT_DISPOSITION,
                                "attachment; filename=\"report-" + id + ".pdf\"")
                        .body(pdfBytes))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
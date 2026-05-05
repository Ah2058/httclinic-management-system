package infrax.teama.submit_service.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ReportService {
    private final PatientFormService patientFormService;
    private final SimplePdfReportGenerator pdf;

    public Optional<byte[]> generatePatientFormReportPdf(Long id) {
        return patientFormService.getForm(id).map(pdf::generatePatientFormReport);
    }

    public Optional<byte[]> generatePatientFormReportPdf(Long id, String language) {
        return patientFormService.getForm(id).map(f -> pdf.generatePatientFormReport(f, language));
    }
}

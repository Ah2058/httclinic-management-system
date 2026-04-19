package infrax.teama.form_submission.service;

import infrax.teama.form_submission.dto.FormSubmissionRequest;
import infrax.teama.form_submission.dto.FormSubmissionResponse;
import infrax.teama.form_submission.model.FormSubmission;
import infrax.teama.form_submission.model.SubmissionStatus;
import infrax.teama.form_submission.repository.FormSubmissionRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * ═══════════════════════════════════════════════════════════════════
 * SERVICE: FormSubmissionService
 * ═══════════════════════════════════════════════════════════════════
 *
 * ZUSAMMENFASSUNG:
 * Geschaefts-Logik-Schicht. Hier passiert ALLES, was ueber reines
 * CRUD hinausgeht – Validierungen, Status-Uebergaenge und spaeter
 * z.B. Mailversand oder Event-Publishing.
 *
 * Der Controller weiss nichts ueber die Datenbank, und das Repository
 * weiss nichts ueber HTTP. Dazwischen vermittelt dieser Service.
 *
 * WARUM @Transactional?
 *   Sorgt dafuer, dass jede Methode in EINER Datenbank-Transaktion
 *   ablaeuft. Wirft eine Methode mitten drin eine RuntimeException,
 *   wird die Transaktion automatisch zurueckgerollt – d.h. KEINE
 *   halben Daten in der DB.
 *
 * WARUM @RequiredArgsConstructor (Lombok)?
 *   Erzeugt einen Konstruktor mit allen `final`-Feldern. Spring
 *   injiziert das Repository automatisch ueber diesen Konstruktor –
 *   das nennt man "Constructor Injection" und ist der empfohlene
 *   DI-Stil (besser als Field-Injection per @Autowired).
 * ═══════════════════════════════════════════════════════════════════
 */
@Service                      // Spring-Stereotyp: markiert die Klasse als Service-Bean.
@RequiredArgsConstructor      // Lombok: erzeugt Konstruktor fuer alle final-Felder.
@Transactional                // Alle public-Methoden laufen in einer DB-Transaktion.
public class FormSubmissionService {

    // `final` = wird einmal im Konstruktor gesetzt und nie wieder veraendert.
    // Durch @RequiredArgsConstructor erzeugt Lombok einen Konstruktor,
    // in den Spring beim Start die Repository-Bean hineinreicht.
    private final FormSubmissionRepository repository;

    /**
     * Speichert ein neues Formular in der Datenbank und gibt die
     * persistierte Version (inkl. generierter ID) als Response-DTO zurueck.
     *
     * Aufruf-Flow:
     *   Controller -> diese Methode -> Repository.save -> DB
     *   DB -> Repository -> diese Methode -> Controller -> JSON -> Client
     */
    public FormSubmissionResponse create(FormSubmissionRequest request) {

        // Umwandlung DTO -> Entity mit dem Lombok-Builder.
        // createdAt und status werden per @PrePersist in der Entity automatisch gesetzt.
        FormSubmission entity = FormSubmission.builder()
                .name(request.name())
                .email(request.email())
                .subject(request.subject())
                .message(request.message())
                .build();

        // save(...) gibt das persistierte Objekt zurueck – jetzt mit ID,
        // createdAt (aus @PrePersist) und status = NEW.
        FormSubmission saved = repository.save(entity);

        // Entity -> Response-DTO (nie die Entity nach aussen geben!).
        return FormSubmissionResponse.from(saved);
    }

    /**
     * Liefert alle Einreichungen zurueck.
     *
     * In echt wuerde man paginieren (Pageable), fuer den Einstieg
     * reicht uns hier eine flache Liste.
     */
    @Transactional(readOnly = true)   // Performance-Hinweis: reine Lese-Transaktion.
    public List<FormSubmissionResponse> findAll() {
        return repository.findAll().stream()
                .map(FormSubmissionResponse::from)  // Methoden-Referenz = kurze Lambda-Form
                .toList();
    }

    /**
     * Liefert alle Einreichungen mit einem bestimmten Bearbeitungsstatus.
     * Nuetzlich z.B. fuer ein Dashboard, das nur offene Anfragen anzeigt.
     */
    @Transactional(readOnly = true)
    public List<FormSubmissionResponse> findByStatus(SubmissionStatus status) {
        return repository.findByStatusOrderByCreatedAtDesc(status).stream()
                .map(FormSubmissionResponse::from)
                .toList();
    }

    /**
     * Liefert genau EIN Formular anhand seiner ID.
     *
     * Wirft eine EntityNotFoundException, wenn die ID nicht existiert.
     * Der GlobalExceptionHandler faengt sie ab und wandelt sie in
     * eine HTTP-404-Antwort um.
     */
    @Transactional(readOnly = true)
    public FormSubmissionResponse findById(Long id) {
        FormSubmission entity = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Formular mit ID " + id + " wurde nicht gefunden"));
        return FormSubmissionResponse.from(entity);
    }

    /**
     * Aendert den Bearbeitungsstatus eines Formulars.
     * (z.B. NEW -> PROCESSED, wenn ein Mitarbeiter geantwortet hat.)
     */
    public FormSubmissionResponse updateStatus(Long id, SubmissionStatus newStatus) {
        FormSubmission entity = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Formular mit ID " + id + " wurde nicht gefunden"));

        entity.setStatus(newStatus);

        // save(...) ist in einer @Transactional-Methode nicht zwingend noetig,
        // weil Hibernate am Ende der Transaktion automatisch "Dirty Checking"
        // macht und geaenderte Entities zurueckschreibt. Wir rufen es trotzdem
        // explizit auf, damit der Code fuer Einsteiger verstaendlicher bleibt.
        FormSubmission saved = repository.save(entity);

        return FormSubmissionResponse.from(saved);
    }

    /**
     * Loescht ein Formular.
     *
     * Wir pruefen VORHER, ob es existiert, um dem Client ein sauberes
     * 404 zurueckzugeben. Ohne den Check wuerde deleteById still
     * nichts tun und irrefuehrend HTTP 204 liefern.
     */
    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new EntityNotFoundException(
                    "Formular mit ID " + id + " wurde nicht gefunden");
        }
        repository.deleteById(id);
    }
}

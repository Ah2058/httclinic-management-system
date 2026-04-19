package infrax.teama.form_submission.controller;

import infrax.teama.form_submission.dto.FormSubmissionRequest;
import infrax.teama.form_submission.dto.FormSubmissionResponse;
import infrax.teama.form_submission.model.SubmissionStatus;
import infrax.teama.form_submission.service.FormSubmissionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

/**
 * ═══════════════════════════════════════════════════════════════════
 * CONTROLLER: FormSubmissionController
 * ═══════════════════════════════════════════════════════════════════
 *
 * ZUSAMMENFASSUNG:
 * Der EINZIGE Beruehrungspunkt zwischen Aussenwelt (HTTP) und der
 * Anwendung. Faengt Requests ab, leitet sie an den Service weiter
 * und wandelt dessen Antwort in eine HTTP-Response um.
 *
 * ENDPOINTS IM UEBERBLICK:
 *   POST   /api/submissions              -> neues Formular anlegen     (201 Created)
 *   GET    /api/submissions              -> alle Formulare auflisten   (200 OK)
 *   GET    /api/submissions?status=NEW   -> Filter nach Status         (200 OK)
 *   GET    /api/submissions/{id}         -> ein Formular abrufen       (200 / 404)
 *   PATCH  /api/submissions/{id}/status  -> Bearbeitungsstatus aendern (200 / 404)
 *   DELETE /api/submissions/{id}         -> Formular loeschen          (204 / 404)
 *
 * @Valid AUF DEM REQUEST-DTO:
 *   Triggert die in FormSubmissionRequest definierten Validierungen
 *   (@NotBlank, @Email, @Size). Schlaegt eine davon fehl, wirft Spring
 *   eine MethodArgumentNotValidException, die unser GlobalExceptionHandler
 *   zu einer sauberen HTTP-400-Antwort umformuliert.
 * ═══════════════════════════════════════════════════════════════════
 */
@RestController                           // = @Controller + @ResponseBody -> JSON standardmaessig.
@RequestMapping("/api/submissions")       // Gemeinsames URL-Praefix fuer alle Methoden.
@RequiredArgsConstructor                  // Lombok: Konstruktor-Injection fuer den Service.
public class FormSubmissionController {

    // Via Konstruktor injiziert (dank @RequiredArgsConstructor).
    private final FormSubmissionService service;

    /**
     * Neues Formular anlegen.
     *
     * HTTP 201 Created + Location-Header auf die neu erzeugte
     * Ressource – so verlangen es die REST-Konventionen.
     */
    @PostMapping
    public ResponseEntity<FormSubmissionResponse> create(
            @Valid @RequestBody FormSubmissionRequest request) {
            //      ^^^^^^^^^^^^ JSON-Body wird in das DTO deserialisiert.
            // ^^^^^ Validierung gegen die Constraints im DTO wird ausgeloest.

        FormSubmissionResponse created = service.create(request);

        // Location-Header: URL, unter der man die erzeugte Ressource finden kann.
        URI location = URI.create("/api/submissions/" + created.id());

        return ResponseEntity
                .created(location)   // HTTP 201 + Location-Header
                .body(created);      // Body = das Response-DTO (wird zu JSON)
    }

    /**
     * Alle Formulare auflisten.
     *
     * Optional kann der Client mit `?status=NEW` nach Bearbeitungsstatus
     * filtern: GET /api/submissions?status=NEW.
     *
     * `required = false` bedeutet, der Parameter muss nicht angegeben werden.
     */
    @GetMapping
    public List<FormSubmissionResponse> findAll(
            @RequestParam(required = false) SubmissionStatus status) {

        // Wenn kein Status angegeben ist, geben wir alles zurueck;
        // sonst filtern wir ueber das Repository.
        return (status == null)
                ? service.findAll()
                : service.findByStatus(status);
    }

    /**
     * Ein einzelnes Formular per ID abfragen.
     *
     * @PathVariable greift den Teil der URL ab, der in {geschweiften
     * Klammern} steht – hier also die {id}.
     */
    @GetMapping("/{id}")
    public FormSubmissionResponse findById(@PathVariable Long id) {
        return service.findById(id);
    }

    /**
     * Status eines Formulars aendern (z.B. NEW -> PROCESSED).
     *
     * Warum PATCH statt PUT?
     *   PUT:   "ich ueberschreibe die komplette Ressource"
     *   PATCH: "ich aendere nur EIN Feld, alles andere bleibt wie es ist"
     *
     * Der neue Status kommt als Query-Parameter, weil er nur ein
     * einzelner Wert ist (kein vollstaendiges DTO noetig):
     *   PATCH /api/submissions/42/status?status=PROCESSED
     */
    @PatchMapping("/{id}/status")
    public FormSubmissionResponse updateStatus(
            @PathVariable Long id,
            @RequestParam SubmissionStatus status) {
        return service.updateStatus(id, status);
    }

    /**
     * Formular loeschen.
     *
     * HTTP 204 No Content = Erfolg, aber keine Antwort-Body.
     * Passt, weil es nichts zurueckzugeben gibt.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}

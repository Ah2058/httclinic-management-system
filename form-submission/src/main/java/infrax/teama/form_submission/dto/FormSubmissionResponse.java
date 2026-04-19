package infrax.teama.form_submission.dto;

import infrax.teama.form_submission.model.FormSubmission;
import infrax.teama.form_submission.model.SubmissionStatus;

import java.time.Instant;

/**
 * ═══════════════════════════════════════════════════════════════════
 * DTO: FormSubmissionResponse  (Data Transfer Object – "Ausgang")
 * ═══════════════════════════════════════════════════════════════════
 *
 * ZUSAMMENFASSUNG:
 * Die Darstellung, die der Client nach einem erfolgreichen Submit
 * zurueckbekommt. Enthaelt ausschliesslich Felder, die der Client
 * SEHEN DARF.
 *
 * TRENNUNG REQUEST vs. RESPONSE:
 *   - Request hat keine id/createdAt/status (Client liefert sie nicht).
 *   - Response hat alles, damit der Client seinen Eintrag verifizieren
 *     bzw. spaeter wieder ueber die ID abrufen kann.
 *
 * STATISCHE FACTORY-METHODE `from(...)`:
 *   Kleine Helfer-Methode, die eine Entity in ein Response-DTO
 *   umwandelt. Wir packen die Umwandlung NICHT in die Entity selbst,
 *   denn die Entity soll nichts ueber DTOs wissen (= saubere Trennung).
 *   Alternative waere ein MapStruct-Mapper – fuer dieses kleine
 *   Projekt aber overkill.
 * ═══════════════════════════════════════════════════════════════════
 */
public record FormSubmissionResponse(

        Long id,                    // eindeutige ID aus der DB (vom Server vergeben)
        String name,                // Absendername
        String email,               // Absender-E-Mail
        String subject,             // Betreff
        String message,             // Nachrichtentext
        Instant createdAt,          // Zeitpunkt der Einreichung (UTC)
        SubmissionStatus status     // aktueller Bearbeitungsstatus

) {

    /**
     * Konvertiert eine persistierte Entity in ein Response-DTO.
     *
     * Wird im Service-Code an den Stellen verwendet, an denen etwas
     * nach aussen gegeben wird. Dadurch bleibt die Umwandlung an einer
     * EINZIGEN Stelle und aenderbar ohne Copy-and-Paste im Service.
     */
    public static FormSubmissionResponse from(FormSubmission entity) {
        return new FormSubmissionResponse(
                entity.getId(),
                entity.getName(),
                entity.getEmail(),
                entity.getSubject(),
                entity.getMessage(),
                entity.getCreatedAt(),
                entity.getStatus()
        );
    }
}

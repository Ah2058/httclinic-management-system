package infrax.teama.form_submission.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * ═══════════════════════════════════════════════════════════════════
 * DTO: FormSubmissionRequest  (Data Transfer Object – "Eingang")
 * ═══════════════════════════════════════════════════════════════════
 *
 * ZUSAMMENFASSUNG:
 * Repraesentiert das JSON-Objekt, das der Client an den Endpoint
 * POST /api/submissions schickt. Spring/Jackson deserialisiert die
 * JSON-Body automatisch in eine Instanz dieser Klasse.
 *
 * WARUM NICHT DIE ENTITY DIREKT VERWENDEN?
 *   - Entkopplung: Aenderungen an der DB-Tabelle duerfen die API
 *     nicht automatisch brechen.
 *   - Sicherheit: Der Client darf id, createdAt, status niemals
 *     selbst setzen. In der Entity waeren diese Felder aber sichtbar.
 *   - Validierung: Wir bringen hier strengere Regeln an (@NotBlank,
 *     @Email), die nur fuer die API-Eingabe gelten.
 *
 * JAVA-RECORD STATT KLASSE:
 *   Ein Java-`record` erzeugt automatisch:
 *     - Konstruktor mit allen Feldern
 *     - Getter-Methoden (name(), email(), ...)   <- Achtung: ohne "get"-Praefix
 *     - equals(), hashCode(), toString()
 *   Dadurch sparen wir Lombok an dieser Stelle komplett ein.
 *
 * VALIDIERUNG:
 *   Wird erst scharfgeschaltet, wenn der Controller das Objekt mit
 *   @Valid entgegennimmt. Ohne @Valid werden die Constraints ignoriert!
 * ═══════════════════════════════════════════════════════════════════
 */
public record FormSubmissionRequest(

        // @NotBlank = darf nicht null sein UND nicht nur aus Leerzeichen bestehen.
        @NotBlank(message = "Name darf nicht leer sein")
        @Size(max = 100, message = "Name darf hoechstens 100 Zeichen lang sein")
        String name,

        // @Email prueft die Struktur (etwas@etwas.tld).
        @NotBlank(message = "E-Mail darf nicht leer sein")
        @Email(message = "E-Mail muss ein gueltiges Format haben")
        @Size(max = 255, message = "E-Mail darf hoechstens 255 Zeichen lang sein")
        String email,

        @NotBlank(message = "Betreff darf nicht leer sein")
        @Size(min = 3, max = 200, message = "Betreff muss 3 bis 200 Zeichen lang sein")
        String subject,

        @NotBlank(message = "Nachricht darf nicht leer sein")
        @Size(min = 10, max = 5000, message = "Nachricht muss 10 bis 5000 Zeichen lang sein")
        String message

) {
}

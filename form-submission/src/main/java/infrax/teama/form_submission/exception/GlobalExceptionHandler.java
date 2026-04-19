package infrax.teama.form_submission.exception;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * ═══════════════════════════════════════════════════════════════════
 * ADVICE: GlobalExceptionHandler
 * ═══════════════════════════════════════════════════════════════════
 *
 * ZUSAMMENFASSUNG:
 * Faengt Exceptions, die aus irgendeinem Controller hochblubbern,
 * und wandelt sie in strukturierte JSON-Fehlerantworten um.
 * Dadurch sieht der Client IMMER ein konsistentes Fehlerformat
 * statt Java-Stacktraces oder der Spring-Whitelabel-Error-Page.
 *
 * WARUM @RestControllerAdvice?
 *   = @ControllerAdvice + @ResponseBody. Gilt PROJEKTWEIT fuer
 *   alle @RestController. Die Rueckgaben werden automatisch als
 *   JSON ausgeliefert – ideal fuer eine REST-API.
 *
 * AUSGABEFORMAT (Beispiel 400):
 *   {
 *     "timestamp": "2026-04-19T10:12:33Z",
 *     "status":    400,
 *     "error":     "Bad Request",
 *     "fieldErrors": {
 *       "email":   "E-Mail muss ein gueltiges Format haben",
 *       "message": "Nachricht muss 10 bis 5000 Zeichen lang sein"
 *     }
 *   }
 * ═══════════════════════════════════════════════════════════════════
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Wird ausgeloest, wenn @Valid im Controller eine Verletzung erkennt
     * (z.B. leeres Pflichtfeld oder ungueltiges E-Mail-Format).
     *
     * Wir sammeln alle Feldfehler in einem Dictionary,
     * damit der Client gezielt die richtigen Formularfelder rot
     * einfaerben kann.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(
            MethodArgumentNotValidException ex) {

        // feldName -> Fehlermeldung
        Map<String, String> fieldErrors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(err ->
                fieldErrors.put(err.getField(), err.getDefaultMessage()));

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(buildBody(HttpStatus.BAD_REQUEST,
                        Map.of("fieldErrors", fieldErrors)));
    }

    /**
     * Wird vom Service geworfen, wenn eine angeforderte ID nicht
     * existiert. Wir uebersetzen das in ein sauberes HTTP 404.
     */
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(
            EntityNotFoundException ex) {

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(buildBody(HttpStatus.NOT_FOUND,
                        Map.of("message", ex.getMessage())));
    }

    /**
     * Gemeinsamer Aufbau des JSON-Bodys fuer ALLE Fehlerantworten.
     *
     * LinkedHashMap = behaelt die Einfuege-Reihenfolge bei, damit die
     * JSON-Ausgabe immer in der gleichen Reihenfolge erscheint
     * (timestamp, status, error, ...). Wirkt fuer den Leser vertrauter.
     */
    private Map<String, Object> buildBody(HttpStatus status, Map<String, Object> extra) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", Instant.now().toString());
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.putAll(extra);
        return body;
    }
}

package infrax.teama.form_submission.repository;

import infrax.teama.form_submission.model.FormSubmission;
import infrax.teama.form_submission.model.SubmissionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * ═══════════════════════════════════════════════════════════════════
 * REPOSITORY: FormSubmissionRepository
 * ═══════════════════════════════════════════════════════════════════
 *
 * ZUSAMMENFASSUNG:
 * Datenzugriffs-Schicht ("DAO") fuer FormSubmission. Wir muessen
 * KEINE SQL-Abfragen selbst schreiben – Spring Data JPA erzeugt
 * zur Laufzeit automatisch eine Implementierung dieses Interface
 * und injiziert sie ueberall dort, wo wir sie per @Autowired oder
 * Konstruktor-Injection anfordern.
 *
 * WAS WIR GESCHENKT BEKOMMEN (vererbt von JpaRepository):
 *   - save(entity)        -> INSERT bzw. UPDATE
 *   - findById(id)        -> SELECT ... WHERE id = ?
 *   - findAll()           -> SELECT *
 *   - deleteById(id)      -> DELETE
 *   - count(), existsById(id), ...
 *
 * ABFRAGEN AUS METHODENNAMEN ("Derived Queries"):
 *   Spring Data parst den Methodennamen `findByStatusOrderByCreatedAtDesc`
 *   und baut daraus automatisch:
 *     SELECT * FROM form_submissions
 *     WHERE status = ?
 *     ORDER BY created_at DESC
 *
 *   Siehe:
 *   https://docs.spring.io/spring-data/jpa/reference/repositories/query-methods.html
 * ═══════════════════════════════════════════════════════════════════
 */
@Repository  // Optional, da JpaRepository bereits als Spring-Bean erkannt wird.
             // Wir setzen es trotzdem, weil es Absicht dokumentiert.
public interface FormSubmissionRepository
        extends JpaRepository<FormSubmission, Long> {
        //                    ^^^^^^^^^^^^^^  ^^^^
        //                    Entity-Typ      Typ des Primaerschluessels

    /**
     * Liefert alle Einreichungen in einem bestimmten Status zurueck.
     * Reihenfolge: neueste zuerst (createdAt DESC).
     *
     * Wir brauchen diese Methode z.B. fuer ein Dashboard, das nur
     * offene ("NEW") Anfragen anzeigt.
     */
    List<FormSubmission> findByStatusOrderByCreatedAtDesc(SubmissionStatus status);
}

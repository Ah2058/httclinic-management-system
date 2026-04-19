package infrax.teama.form_submission.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

/**
 * ═══════════════════════════════════════════════════════════════════
 * ENTITY: FormSubmission  (JPA-Entity)
 * ═══════════════════════════════════════════════════════════════════
 *
 * ZUSAMMENFASSUNG:
 * Abbild einer Zeile in der Datenbanktabelle `form_submissions`.
 * Jede Instanz repraesentiert genau EIN vom Client abgeschicktes
 * Kontaktformular.
 *
 * ROLLE IN DER SCHICHTENARCHITEKTUR:
 *   Controller -> Service -> Repository -> ENTITY -> DB
 *
 *   Entities sind die einzigen Objekte, die Hibernate/JPA direkt in
 *   SQL uebersetzt. Sie verlassen die Service-Schicht nie nach aussen;
 *   dafuer gibt es DTOs (FormSubmissionRequest / -Response).
 *
 * AUTOMATISCHE SPALTEN VIA @PrePersist:
 *   - createdAt wird gesetzt, wenn der Datensatz zum ERSTEN Mal
 *     gespeichert wird. Der Client darf das nicht beeinflussen.
 *   - status startet immer auf NEW.
 * ═══════════════════════════════════════════════════════════════════
 */
@Entity                              // Markiert die Klasse als von JPA verwaltete Entitaet (= eine DB-Tabelle).
@Table(name = "form_submissions")    // Expliziter Tabellenname (statt Klassenname in Snake-Case).
@Getter                              // Lombok: erzeugt getName(), getEmail(), ... zur Kompilezeit.
@Setter                              // Lombok: erzeugt setName(), setEmail(), ... zur Kompilezeit.
@NoArgsConstructor                   // Lombok: parameterloser Konstruktor – von JPA zwingend vorausgesetzt.
@AllArgsConstructor                  // Lombok: Konstruktor mit allen Feldern (praktisch in Tests).
@Builder                             // Lombok: Builder-Pattern – macht das Erzeugen von Objekten lesbar.
public class FormSubmission {

    // Primaerschluessel. @GeneratedValue: Datenbank vergibt die ID automatisch.
    // IDENTITY = nutzt AUTO_INCREMENT in MariaDB.
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Absendername. @Column(nullable = false) erzeugt NOT NULL in der DB.
    @Column(nullable = false, length = 100)
    private String name;

    // Absender-E-Mail. length = 255, weil RFC das als Maximum definiert.
    @Column(nullable = false, length = 255)
    private String email;

    // Betreff des Formulars.
    @Column(nullable = false, length = 200)
    private String subject;

    // Nachrichtentext. columnDefinition = "TEXT" erlaubt lange Eintraege
    // (bis ~64 KB in MariaDB) statt nur VARCHAR(255).
    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    // Zeitstempel der Einreichung. updatable = false verhindert, dass
    // dieser Wert spaeter versehentlich ueberschrieben wird.
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    // Bearbeitungsstatus. @Enumerated(STRING): Enum wird als Text
    // ('NEW', 'PROCESSED', ...) gespeichert. Bei ORDINAL wuerde nur
    // die Position (0, 1, 2) gespeichert – gefaehrlich beim Umsortieren!
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SubmissionStatus status;

    /**
     * Wird von JPA automatisch aufgerufen, BEVOR die Entitaet zum ersten
     * Mal in die DB geschrieben wird. Wir setzen hier Default-Werte,
     * damit Controller/Service nicht daran denken muessen.
     */
    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
        if (this.status == null) {
            this.status = SubmissionStatus.NEW;
        }
    }
}

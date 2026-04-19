package infrax.teama.form_submission.model;

/**
 * ═══════════════════════════════════════════════════════════════════
 * ENUM: SubmissionStatus
 * ═══════════════════════════════════════════════════════════════════
 *
 * ZUSAMMENFASSUNG:
 * Aufzählungstyp (Enum), der die moeglichen Bearbeitungszustaende
 * eines eingereichten Formulars abbildet. Wird in FormSubmission
 * als Feld verwendet.
 *
 * LEBENSZYKLUS EINES FORMULARS:
 *   NEW  ─►  PROCESSED  ─►  ARCHIVED
 *
 * WARUM EIN ENUM STATT EINES STRINGS?
 *   - Typsicherheit: Der Compiler verhindert Tippfehler ("new" vs "NEW").
 *   - Endliche Menge: Es gibt nur genau diese drei Zustaende.
 *   - Dokumentiert die erlaubten Werte direkt im Code.
 * ═══════════════════════════════════════════════════════════════════
 */
public enum SubmissionStatus {

    // Das Formular wurde gerade erst gespeichert und ist noch unbearbeitet.
    NEW,

    // Ein Mitarbeiter hat das Formular angesehen bzw. beantwortet.
    PROCESSED,

    // Der Vorgang ist abgeschlossen; das Formular liegt nur noch zur Historie vor.
    ARCHIVED
}

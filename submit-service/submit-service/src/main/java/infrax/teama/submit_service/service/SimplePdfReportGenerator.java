package infrax.teama.submit_service.service;

import infrax.teama.submit_service.model.PatientForm;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Dependency-free PDF generator for single-page medical reports.
 * Uses built-in Helvetica font and simple line-based layout.
 */
@Component
public class SimplePdfReportGenerator {
    private static final int MAX_LINES = 64;
    private static final int MAX_LABEL_WIDTH = 22;
    private static final int MAX_CONTENT_CHARS = 92;
    private static final DateTimeFormatter DATE = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private static final Map<String, Map<String, String>> TRANSLATIONS = Map.ofEntries(
            Map.entry("en", Map.ofEntries(
                    Map.entry("title", "CLINIC MEDICAL REPORT"),
                    Map.entry("form_id", "Form ID"),
                    Map.entry("generated_at", "Generated at"),
                    Map.entry("personal_info", "PERSONAL INFORMATION"),
                    Map.entry("first_name", "First name"),
                    Map.entry("last_name", "Last name"),
                    Map.entry("date_of_birth", "Date of birth"),
                    Map.entry("phone", "Phone"),
                    Map.entry("email", "Email"),
                    Map.entry("address", "Address"),
                    Map.entry("medical_info", "MEDICAL INFORMATION"),
                    Map.entry("symptoms", "Symptoms"),
                    Map.entry("other_symptoms", "Other symptoms"),
                    Map.entry("allergies", "Allergies"),
                    Map.entry("other_allergies", "Other allergies"),
                    Map.entry("medications", "Medications"),
                    Map.entry("other_medications", "Other medications"),
                    Map.entry("pre_existing", "Pre-existing"),
                    Map.entry("other_conditions", "Other conditions"),
                    Map.entry("clinical_assessment", "CLINICAL ASSESSMENT"),
                    Map.entry("diagnosis", "Diagnosis"),
                    Map.entry("required_medicine", "Required medicine"),
                    Map.entry("notes", "Notes"),
                    Map.entry("auto_note", "Auto-generated document. Verify all details before distribution."),
                    Map.entry("truncated", "... report truncated to fit one page"),
                    Map.entry("none", "None")
            )),
            Map.entry("de", Map.ofEntries(
                    Map.entry("title", "KLINISCHER BERICHT"),
                    Map.entry("form_id", "Formular-ID"),
                    Map.entry("generated_at", "Erstellt am"),
                    Map.entry("personal_info", "PERSOENLICHE INFORMATIONEN"),
                    Map.entry("first_name", "Vorname"),
                    Map.entry("last_name", "Nachname"),
                    Map.entry("date_of_birth", "Geburtsdatum"),
                    Map.entry("phone", "Telefon"),
                    Map.entry("email", "E-Mail"),
                    Map.entry("address", "Adresse"),
                    Map.entry("medical_info", "MEDIZINISCHE INFORMATIONEN"),
                    Map.entry("symptoms", "Symptome"),
                    Map.entry("other_symptoms", "Andere Symptome"),
                    Map.entry("allergies", "Allergien"),
                    Map.entry("other_allergies", "Andere Allergien"),
                    Map.entry("medications", "Medikamente"),
                    Map.entry("other_medications", "Andere Medikamente"),
                    Map.entry("pre_existing", "Vorerkrankungen"),
                    Map.entry("other_conditions", "Andere Erkrankungen"),
                    Map.entry("clinical_assessment", "KLINISCHE BEWERTUNG"),
                    Map.entry("diagnosis", "Diagnose"),
                    Map.entry("required_medicine", "Erforderliche Medikamente"),
                    Map.entry("notes", "Notizen"),
                    Map.entry("auto_note", "Automatisch generiertes Dokument. Vor der Verteilung alle Angaben pruefen."),
                    Map.entry("truncated", "... Bericht wurde gekuerzt um auf eine Seite zu passen"),
                    Map.entry("none", "Keine")
            ))
    );

    public byte[] generatePatientFormReport(PatientForm form) {
        return generatePatientFormReport(form, "en");
    }

    public byte[] generatePatientFormReport(PatientForm form, String language) {
        List<String> lines = buildLines(form, language);
        if (lines.size() > MAX_LINES) {
            List<String> truncated = new ArrayList<>(lines.subList(0, MAX_LINES - 1));
            truncated.add(t("truncated", language));
            lines = truncated;
        }

        String content = buildContentStream(lines);
        byte[] contentBytes = content.getBytes(StandardCharsets.US_ASCII);

        List<byte[]> objects = new ArrayList<>();
        objects.add(obj(1, "<< /Type /Catalog /Pages 2 0 R >>"));
        objects.add(obj(2, "<< /Type /Pages /Kids [3 0 R] /Count 1 >>"));
        objects.add(obj(3, "<< /Type /Page /Parent 2 0 R /MediaBox [0 0 612 792] /Resources << /Font << /F1 4 0 R >> >> /Contents 5 0 R >>"));
        objects.add(obj(4, "<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica >>"));
        objects.add(streamObj(5, contentBytes));

        return buildPdf(objects);
    }

    private List<String> buildLines(PatientForm f, String language) {
        List<String> out = new ArrayList<>();
        out.add(t("title", language));
        out.add(repeat("-", 82));
        out.add(t("form_id", language) + "      : " + safe(f.getId()));
        out.add(t("generated_at", language) + " : " + DATE_TIME.format(LocalDateTime.now()));
        out.add("");

        section(out, t("personal_info", language));
        kv(out, t("first_name", language), safe(f.getFirstName()));
        kv(out, t("last_name", language), safe(f.getLastName()));
        kv(out, t("date_of_birth", language), f.getDateOfBirth() == null ? "-" : DATE.format(f.getDateOfBirth()));
        kv(out, t("phone", language), safe(f.getPhoneNumber()));
        kv(out, t("email", language), safe(f.getEmailAddress()));
        kv(out, t("address", language), safe(f.getStreetName()) + " " + safe(f.getStreetNumber()) + ", " + safe(f.getCity()) + ", " + safe(f.getPostalCode()));
        out.add("");

        section(out, t("medical_info", language));
        kv(out, t("symptoms", language), joinEnums(f.getSymptoms(), language));
        kv(out, t("other_symptoms", language), safe(f.getOtherSymptoms()));
        kv(out, t("allergies", language), joinEnums(f.getAllergies(), language));
        kv(out, t("other_allergies", language), safe(f.getOtherAllergies()));
        kv(out, t("medications", language), joinEnums(f.getMedications(), language));
        kv(out, t("other_medications", language), safe(f.getOtherMedications()));
        kv(out, t("pre_existing", language), joinEnums(f.getPreExistingConditions(), language));
        kv(out, t("other_conditions", language), safe(f.getOtherPreExistingConditions()));
        out.add("");

        section(out, t("clinical_assessment", language));
        kv(out, t("diagnosis", language), safe(f.getDiagnosis()));
        kv(out, t("required_medicine", language), safe(f.getRequiredMedicine()));
        kv(out, t("notes", language), safe(f.getNotes()));
        out.add("");
        out.add(repeat("-", 82));
        out.add(t("auto_note", language));

        return out;
    }

    private static String t(String key, String language) {
        Map<String, String> langMap = TRANSLATIONS.getOrDefault(language, TRANSLATIONS.get("en"));
        return langMap.getOrDefault(key, key);
    }

    private void section(List<String> out, String title) {
        out.add(title);
        out.add(repeat("-", title.length()));
    }

    private void kv(List<String> out, String label, String value) {
        String prefix = padRight(label, MAX_LABEL_WIDTH) + " : ";
        List<String> wrapped = wrapText(value, MAX_CONTENT_CHARS);
        if (wrapped.isEmpty()) {
            out.add(prefix + "-");
            return;
        }
        out.add(prefix + wrapped.get(0));
        String continuationPrefix = repeat(" ", MAX_LABEL_WIDTH + 3);
        for (int i = 1; i < wrapped.size(); i++) {
            out.add(continuationPrefix + wrapped.get(i));
        }
    }

    private static List<String> wrapText(String text, int maxLen) {
        List<String> lines = new ArrayList<>();
        String value = text == null ? "-" : text.trim();
        if (value.isEmpty()) {
            lines.add("-");
            return lines;
        }
        String[] words = value.split("\\s+");
        StringBuilder current = new StringBuilder();
        for (String word : words) {
            if (current.length() == 0) {
                current.append(word);
                continue;
            }
            if (current.length() + 1 + word.length() <= maxLen) {
                current.append(' ').append(word);
            } else {
                lines.add(current.toString());
                current = new StringBuilder(word);
            }
        }
        if (current.length() > 0) lines.add(current.toString());
        return lines;
    }

    private static String joinEnums(List<?> values, String language) {
        if (values == null || values.isEmpty()) return t("none", language);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < values.size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append(humanize(values.get(i)));
        }
        return sb.toString();
    }

    private static String humanize(Object value) {
        if (value == null) return "-";
        String s = String.valueOf(value);
        String[] parts = s.split("_");
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < parts.length; i++) {
            String p = parts[i].toLowerCase();
            if (p.isEmpty()) continue;
            if (i > 0) out.append(' ');
            out.append(Character.toUpperCase(p.charAt(0))).append(p.substring(1));
        }
        return out.length() == 0 ? s : out.toString();
    }

    private static String safe(Object v) {
        if (v == null) return "-";
        String s = String.valueOf(v).trim();
        return s.isEmpty() ? "-" : s;
    }

    private static String repeat(String value, int count) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < count; i++) sb.append(value);
        return sb.toString();
    }

    private static String padRight(String input, int width) {
        if (input.length() >= width) return input;
        return input + repeat(" ", width - input.length());
    }

    private static String buildContentStream(List<String> lines) {
        StringBuilder sb = new StringBuilder();
        sb.append("BT\n");
        sb.append("/F1 10 Tf\n");
        sb.append("52 760 Td\n");
        for (int i = 0; i < lines.size(); i++) {
            String escaped = escapePdfString(lines.get(i));
            sb.append("(").append(escaped).append(") Tj\n");
            if (i != lines.size() - 1) sb.append("0 -11 Td\n");
        }
        sb.append("ET\n");
        return sb.toString();
    }

    private static String escapePdfString(String s) {
        return s
                .replace("\\", "\\\\")
                .replace("(", "\\(")
                .replace(")", "\\)");
    }

    private static byte[] obj(int n, String body) {
        String s = n + " 0 obj\n" + body + "\nendobj\n";
        return s.getBytes(StandardCharsets.US_ASCII);
    }

    private static byte[] streamObj(int n, byte[] stream) {
        String header = n + " 0 obj\n<< /Length " + stream.length + " >>\nstream\n";
        String footer = "\nendstream\nendobj\n";
        byte[] h = header.getBytes(StandardCharsets.US_ASCII);
        byte[] f = footer.getBytes(StandardCharsets.US_ASCII);

        byte[] out = new byte[h.length + stream.length + f.length];
        System.arraycopy(h, 0, out, 0, h.length);
        System.arraycopy(stream, 0, out, h.length, stream.length);
        System.arraycopy(f, 0, out, h.length + stream.length, f.length);
        return out;
    }

    private static byte[] buildPdf(List<byte[]> objects) {
        List<Integer> offsets = new ArrayList<>();
        List<byte[]> chunks = new ArrayList<>();

        byte[] header = "%PDF-1.4\n".getBytes(StandardCharsets.US_ASCII);
        chunks.add(header);

        int offset = header.length;
        for (byte[] o : objects) {
            offsets.add(offset);
            chunks.add(o);
            offset += o.length;
        }

        int xrefOffset = offset;
        StringBuilder xref = new StringBuilder();
        xref.append("xref\n");
        xref.append("0 ").append(objects.size() + 1).append("\n");
        xref.append(String.format("%010d %05d f \n", 0, 65535));
        for (int off : offsets) {
            xref.append(String.format("%010d %05d n \n", off, 0));
        }

        String trailer =
                "trailer\n" +
                        "<< /Size " + (objects.size() + 1) + " /Root 1 0 R >>\n" +
                        "startxref\n" +
                        xrefOffset + "\n" +
                        "%%EOF\n";

        byte[] xrefBytes = xref.toString().getBytes(StandardCharsets.US_ASCII);
        byte[] trailerBytes = trailer.getBytes(StandardCharsets.US_ASCII);
        chunks.add(xrefBytes);
        chunks.add(trailerBytes);

        int total = 0;
        for (byte[] c : chunks) total += c.length;
        byte[] out = new byte[total];
        int pos = 0;
        for (byte[] c : chunks) {
            System.arraycopy(c, 0, out, pos, c.length);
            pos += c.length;
        }
        return out;
    }
}

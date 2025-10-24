package com.example.unimatcher.util;

import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.LocalDate;
import java.util.*;
import java.util.regex.*;

@Component
public class AdmissionCalendarLoader {

    private final List<Map<String, Object>> events = new ArrayList<>();
    private final Map<Long, String> facultyNames = new HashMap<>();

    public List<Map<String, Object>> getEvents() {
        return events;
    }

    public String findFacultyNameById(Long id) {
        return facultyNames.get(id);
    }

    private static final Map<String, Integer> MONTHS = Map.ofEntries(
            Map.entry("ianuarie", 1), Map.entry("februarie", 2), Map.entry("martie", 3),
            Map.entry("aprilie", 4), Map.entry("mai", 5), Map.entry("iunie", 6),
            Map.entry("iulie", 7), Map.entry("august", 8), Map.entry("septembrie", 9),
            Map.entry("octombrie", 10), Map.entry("noiembrie", 11), Map.entry("decembrie", 12)
    );

    private static final Pattern DATE_RANGE = Pattern.compile(
            "(\\d{1,2})\\s*(?:–|-|până la)?\\s*(\\d{0,2})\\s*"
                    + "(ianuarie|februarie|martie|aprilie|mai|iunie|iulie|august|septembrie|octombrie|noiembrie|decembrie)"
                    + "(?:\\s*(\\d{4}))?"
                    + "(?:[^\\d]+(\\d{1,2})\\s+"
                    + "(ianuarie|februarie|martie|aprilie|mai|iunie|iulie|august|septembrie|octombrie|noiembrie|decembrie)"
                    + "(?:\\s*(\\d{4}))?)?",
            Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE
    );

    @PostConstruct
    public void load() {
        Path csv = Paths.get("dataset", "admission_periods.csv");
        if (!Files.exists(csv)) {
            System.err.println("⚠️  Nu găsesc fișierul " + csv.toAbsolutePath());
            return;
        }

        try (BufferedReader br = Files.newBufferedReader(csv, StandardCharsets.UTF_8)) {
            String header = br.readLine(); // prima linie
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = splitCsv(line);
                if (parts == null || parts.length < 3) continue;

                Long id = parseLong(unquote(parts[0]));
                String name = unquote(parts[1]);
                String sentence = unquote(parts[2]);

                facultyNames.put(id, name);

                Optional<DateRange> dr = parseSentence(sentence);
                if (dr.isEmpty()) {
                    addEvent(name + " — perioadă indisponibilă",
                            LocalDate.now(), LocalDate.now().plusDays(1), true);
                } else {
                    DateRange r = dr.get();
                    addEvent(name + " — Admitere", r.start(), r.end().plusDays(1), true);
                }
            }

            System.out.println("📅 Evenimente încărcate: " + events.size());
        } catch (Exception e) {
            System.err.println("❌ Eroare la citirea calendarului: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ✅ metodă pentru adăugarea manuală a unui eveniment
    public void addEvent(String title, LocalDate start, LocalDate endExclusive, boolean allDay) {
        Map<String, Object> e = new LinkedHashMap<>();
        e.put("title", title);
        e.put("start", start.toString());
        e.put("end", endExclusive.toString());
        e.put("allDay", allDay);
        events.add(e);
    }

    private static record DateRange(LocalDate start, LocalDate end) {}

    private Optional<DateRange> parseSentence(String text) {
        if (text == null || text.isBlank()) return Optional.empty();
        Matcher m = DATE_RANGE.matcher(text.toLowerCase(Locale.ROOT));
        if (m.find()) {
            Integer d1 = asInt(m.group(1));
            Integer d2 = asInt(m.group(2));
            String mon1 = m.group(3);
            Integer y1 = asInt(m.group(4));
            Integer d3 = asInt(m.group(5));
            String mon2 = m.group(6);
            Integer y2 = asInt(m.group(7));

            Integer month1 = MONTHS.get(mon1);
            Integer month2 = MONTHS.get(mon2);

            if (month1 == null) return Optional.empty();

            if (y1 == null && y2 != null) y1 = y2;
            if (y1 == null) y1 = findYear(text);
            if (y2 == null) y2 = y1;

            if (d3 != null && month2 != null) {
                LocalDate start = safeDate(y1, month1, d1 != null ? d1 : 1);
                LocalDate end = safeDate(y2, month2, d3);
                if (start != null && end != null && !end.isBefore(start))
                    return Optional.of(new DateRange(start, end));
            } else if (d2 != null) {
                LocalDate start = safeDate(y1, month1, d1);
                LocalDate end = safeDate(y1, month1, d2);
                if (start != null && end != null)
                    return Optional.of(new DateRange(start, end));
            }

            LocalDate single = safeDate(y1, month1, d1 != null ? d1 : 1);
            if (single != null)
                return Optional.of(new DateRange(single, single));
        }

        return Optional.empty();
    }

    private static Integer asInt(String s) {
        try {
            return s == null || s.isBlank() ? null : Integer.parseInt(s.trim());
        } catch (Exception e) {
            return null;
        }
    }

    private static Integer findYear(String t) {
        Matcher y = Pattern.compile("(20\\d{2})").matcher(t);
        return y.find() ? Integer.parseInt(y.group(1)) : LocalDate.now().getYear();
    }

    private static LocalDate safeDate(Integer y, Integer m, Integer d) {
        try {
            return LocalDate.of(y, m, d);
        } catch (Exception e) {
            return null;
        }
    }

    private static String unquote(String s) {
        if (s == null) return null;
        s = s.trim();
        if (s.startsWith("\"") && s.endsWith("\"") && s.length() >= 2) {
            return s.substring(1, s.length() - 1);
        }
        return s;
    }

    private static String[] splitCsv(String line) {
        List<String> parts = new ArrayList<>();
        StringBuilder cur = new StringBuilder();
        boolean inQuotes = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                inQuotes = !inQuotes;
                cur.append(c);
            } else if (c == ',' && !inQuotes) {
                parts.add(cur.toString());
                cur.setLength(0);
            } else {
                cur.append(c);
            }
        }
        parts.add(cur.toString());
        return parts.toArray(new String[0]);
    }

    private static Long parseLong(String s) {
        try {
            return Long.parseLong(s.trim());
        } catch (Exception e) {
            return null;
        }
    }
}

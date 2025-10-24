package com.example.unimatcher.service;

import com.example.unimatcher.model.Faculty;
import com.example.unimatcher.model.Student;
import com.example.unimatcher.repository.FacultyRepository;
import com.example.unimatcher.util.CsvIO;
import com.example.unimatcher.util.FacultyCSVReader;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.*;
import java.text.Normalizer;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class RecommendationService {

    private final FacultyRepository facultyRepository;
    private final Map<String, Map<String, Double>> deeplearningPredictions = new HashMap<>();

    private final List<Faculty> cachedFaculties;
    private final Map<Long, Double> cachedPopularity;

    public RecommendationService(FacultyRepository facultyRepository) {
        this.facultyRepository = facultyRepository;

        this.cachedFaculties = FacultyCSVReader.load("dataset/faculties.csv");
        this.cachedPopularity = computePopularity();

        try {
            Path p = Paths.get("dataset/deeplearning_predictions.csv");
            if (Files.exists(p)) loadDeepPredictions(p);
        } catch (Exception ignored) {
            System.err.println("⚠️ Nu pot încărca deeplearning_predictions.csv: " + ignored.getMessage());
        }
    }

    public List<Map<String, Object>> recommend(Student student, String algorithm) {
        List<Faculty> faculties = this.cachedFaculties;

        switch (algorithm.toLowerCase()) {
            default:
                return cosineRecommend(student, faculties);
        }
    }

    public List<Map<String, Object>> cosineRecommend(Student s, List<Faculty> faculties) {
        Map<String, Double> sv = buildStudentVector(s);

        return faculties.stream()
                .map(f -> {
                    Map<String, Double> fv = buildFacultyVector(f);
                    double cos = cosine(sv, fv);
                    double pen = calculateRequirementPenalty(s.getGrades(), f.getRequirements());
                    double score = cos * pen;

                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("facultyId", f.getId());
                    m.put("name", f.getName());
                    m.put("description", f.getDescription());
                    m.put("domains", String.join(" | ", f.getDomains()));
                    m.put("requirements", String.join(" | ", f.getRequirements()));
                    m.put("score", score);

                    return m;
                })
                .sorted(Comparator.comparingDouble(m -> -((Double) m.get("score"))))
                .collect(Collectors.toList());
    }

    private void loadDeepPredictions(Path p) throws IOException {
        try (BufferedReader br = Files.newBufferedReader(p)) {
            String line = br.readLine();
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length < 3) continue;
                String sid = parts[0].trim();
                String fname = parts[1].trim();

                try {
                    double score = Double.parseDouble(parts[2].trim());
                    deeplearningPredictions
                            .computeIfAbsent(sid, k -> new HashMap<>())
                            .put(fname, score);
                } catch (NumberFormatException e) {
                    System.err.println("⚠️ Eroare de parsare scor DL pentru " + sid + ", " + fname);
                }
            }
        }
    }

    private Map<Long, Double> computePopularity() {
        Map<Long, Double> avg = new HashMap<>();
        Map<Long, int[]> sumcount = new HashMap<>();

        try (BufferedReader br = CsvIO.open("dataset/interactions.csv")) {
            String line = br.readLine(); // header
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length < 3) continue;

                try {
                    long fid = Long.parseLong(parts[1].trim());
                    int rating = Integer.parseInt(parts[2].trim());

                    int[] sc = sumcount.getOrDefault(fid, new int[2]);
                    sc[0] += rating; sc[1] += 1;
                    sumcount.put(fid, sc);
                } catch (NumberFormatException e) {
                    System.err.println("⚠️ Eroare de parsare ID/Rating în interactions.csv: " + line);
                }
            }
        } catch (Exception e) {
            System.err.println("⚠️ Nu pot citi dataset/interactions.csv: " + e.getMessage());
            return avg;
        }

        for (Map.Entry<Long, int[]> e : sumcount.entrySet()) {
            avg.put(e.getKey(), e.getValue()[0] * 1.0 / e.getValue()[1]);
        }
        System.out.println("📊 Popularitate încărcată pentru " + avg.size() + " facultăți.");
        return avg;
    }

    private Map<String, Double> buildStudentVector(Student s) {
        Map<String, Double> v = new HashMap<>();

        if (s.getInterests() != null)
            s.getInterests().stream()
                    .filter(Objects::nonNull)
                    .map(this::normalize)
                    .forEach(it -> v.put(it, 1.0));

        if (s.getPreferences() != null)
            s.getPreferences().stream()
                    .filter(Objects::nonNull)
                    .map(this::normalize)
                    .forEach(p -> v.put(p, Math.max(v.getOrDefault(p, 0.0), 0.8)));

        if (s.getGrades() != null)
            s.getGrades().entrySet().stream()
                    .filter(e -> e.getKey() != null && e.getValue() != null)
                    .forEach(e -> v.put(normalize(e.getKey()), e.getValue() / 10.0));
        return v;
    }

    private Map<String, Double> buildFacultyVector(Faculty f) {
        Map<String, Double> v = new HashMap<>();

        if (f.getDomains() != null)
            f.getDomains().stream()
                    .filter(Objects::nonNull)
                    .map(this::normalize)
                    .forEach(d -> v.put(d, 1.0));

        if (f.getRequirements() != null)
            f.getRequirements().stream()
                    .map(this::extractSubject)
                    .filter(Objects::nonNull)
                    .map(this::normalize)
                    .forEach(subjNorm ->
                            v.put(subjNorm, Math.max(v.getOrDefault(subjNorm, 0.0), 0.6))
                    );
        return v;
    }

    private String normalize(String s) {
        if (s == null) return "";
        String noAccents = Normalizer.normalize(s, Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
        return noAccents
                .toLowerCase()
                .replaceAll("[^a-z0-9]", "")
                .trim();
    }

    private String extractSubject(String r) {
        if (r == null) return null;
        String cleaned = r.replace(":", " ").trim();
        String[] parts = cleaned.split("\\s+");
        return parts.length > 0 ? parts[0] : null;
    }

    private double cosine(Map<String, Double> a, Map<String, Double> b) {
        double dot = 0.0;
        for (Map.Entry<String, Double> e : a.entrySet()) {
            Double vb = b.get(e.getKey());
            if (vb != null) dot += e.getValue() * vb;
        }
        double na = Math.sqrt(a.values().stream().mapToDouble(v -> v * v).sum());
        double nb = Math.sqrt(b.values().stream().mapToDouble(v -> v * v).sum());
        if (na == 0 || nb == 0) return 0.0;
        return dot / (na * nb);
    }

    public double calculateRequirementPenalty(Map<String, Double> grades, Collection<String> requirements) {
        if (requirements == null || requirements.isEmpty()) return 1.0;

        Map<String, Double> normGrades = new HashMap<>();
        if (grades != null) {
            for (Map.Entry<String, Double> e : grades.entrySet()) {
                if (e.getKey() != null && e.getValue() != null) {
                    normGrades.put(normalize(e.getKey()), e.getValue());
                }
            }
        }

        double penalty = 1.0;
        for (String req : requirements) {
            if (req == null || req.isBlank()) continue;

            String cleaned = req.replace(":", " ").replaceAll("\\s+", " ").trim();
            String[] parts = cleaned.split(" ");

            String subject = normalize(parts[0]);

            Double required = null;
            for (int i = parts.length - 1; i >= 0; i--) {
                try {
                    required = Double.parseDouble(parts[i].replaceAll("[^0-9.]", ""));
                    break;
                } catch (NumberFormatException ignored) {}
            }

            Double studentGrade = normGrades.get(subject);

            if (required == null) {
                if (studentGrade == null) penalty *= 0.9;
            } else {
                if (studentGrade == null) {
                    penalty *= 0.6;
                } else if (studentGrade < required) {
                    double ratio = Math.max(0.6, studentGrade / required);
                    penalty *= ratio;
                }
            }
        }

        return Math.max(0.2, penalty);
    }
}
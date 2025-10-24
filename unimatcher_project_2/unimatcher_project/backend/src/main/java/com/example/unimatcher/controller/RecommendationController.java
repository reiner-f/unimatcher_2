// controller/RecommendationController.java
package com.example.unimatcher.controller;

import com.example.unimatcher.model.RecommendationForm;
import com.example.unimatcher.model.Student;
import com.example.unimatcher.repository.StudentRepository;
import com.example.unimatcher.service.RecommendationService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import org.springframework.security.core.Authentication;

import java.util.*;
import java.util.stream.Collectors;

@Controller
public class RecommendationController {

    private final RecommendationService recommendationService;
    private final StudentRepository studentRepository;

    public RecommendationController(RecommendationService recommendationService,
                                    StudentRepository studentRepository) {
        this.recommendationService = recommendationService;
        this.studentRepository = studentRepository;
    }

    @GetMapping("/recommendations")
    public String form(Model model, Authentication auth) {
        // poți arăta pagina goală sau eventual ultimele recomandări din DB
        model.addAttribute("form", new RecommendationForm());

        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            var dbStudent = studentRepository.findByEmail(auth.getName());
            if (dbStudent != null) {
                model.addAttribute("student", dbStudent);
            }
        }
        return "recommendations";
    }

    @PostMapping("/recommendations")
    public String recommend(@ModelAttribute RecommendationForm form, Model model, Authentication auth) {
        // 1) Verifică autentificarea
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return "redirect:/login";
        }

        // 2) Ia studentul din DB
        String email = auth.getName();
        Student dbStudent = studentRepository.findByEmail(email);
        if (dbStudent == null) {
            return "redirect:/login";
        }

        // 3) Construiește un "Student" temporar pentru vectorizare
        Student s = new Student();
        s.setName(dbStudent.getName());

        // a) pornește cu notele din DB
        if (dbStudent.getGrades() != null) {
            s.getGrades().putAll(dbStudent.getGrades());
        }
        // b) suprascrie/completează cu ce vine din formular (dacă a venit)
        Map<String, Double> cleanGrades = Optional.ofNullable(form.getGradesInput())
                .orElseGet(LinkedHashMap::new)
                .entrySet().stream()
                .filter(e -> e.getKey() != null && !e.getKey().isBlank())
                .filter(e -> e.getValue() != null && !e.getValue().isNaN() && !e.getValue().isInfinite())
                .collect(Collectors.toMap(
                        e -> e.getKey().trim(),
                        Map.Entry::getValue,
                        (a, b) -> b,
                        LinkedHashMap::new
                ));
        s.getGrades().putAll(cleanGrades);

        // c) interese: dacă vin în form le folosim; altfel folosim din DB (dacă ai așa ceva salvat)
        if (form.getInterestsInput() != null && !form.getInterestsInput().isBlank()) {
            Arrays.stream(form.getInterestsInput().split(","))
                    .map(String::trim)
                    .filter(t -> !t.isBlank())
                    .forEach(s.getInterests()::add);
        } else if (dbStudent.getInterests() != null) {
            s.getInterests().addAll(dbStudent.getInterests());
        }

        // 4) Rulează algoritmul (forțat "cosine")
        String algorithm = "cosine";
        List<Map<String, Object>> recs = recommendationService.recommend(s, algorithm);

        // 5) Filtrează scoruri neglijabile
        List<Map<String, Object>> filtered = recs.stream()
                .filter(f -> {
                    Object sc = f.get("score");
                    return (sc instanceof Number) && ((Number) sc).doubleValue() > 0.00001d;
                })
                .collect(Collectors.toList());

        // 6) Taie la TOP N (ex. 20)
        int topN = Math.min(20, filtered.size());
        List<Map<String, Object>> top = filtered.subList(0, topN);

        // 7) Adaugă rank pentru UI
        for (int i = 0; i < top.size(); i++) {
            top.get(i).put("rank", i + 1);
        }

        // 8) Trimite în UI
        model.addAttribute("recommendations", top);
        model.addAttribute("student", dbStudent);
        model.addAttribute("algorithm", algorithm);

        // 9) Persistă (sync) în student_recommendations
//        List<StudentRecommendationService.Item> items = top.stream()
//                .map(m -> new StudentRecommendationService.Item(
//                        ((Number) m.get("facultyId")).longValue(),
//                        ((Number) m.get("score")).doubleValue(),
//                        algorithm
//                ))
//                .toList();
//
//        studentRecommendationService.sync(dbStudent.getId(), items);

        return "recommendations";
    }
}

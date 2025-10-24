package com.example.unimatcher.controller;

import com.example.unimatcher.model.Student;
import com.example.unimatcher.repository.StudentRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Controller
@RequestMapping("/account")
public class AccountController {

    private final StudentRepository studentRepository;

    public AccountController(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    @GetMapping
    public String home(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            String email = auth.getName();
            Student student = studentRepository.findByEmail(email);
            if (student != null) {
                model.addAttribute("student", student);
                model.addAttribute("interests", String.join(", ", student.getInterests()));
            }
        }

        return "account";
    }

    @PostMapping("/grades")
    public String saveGrades(@RequestParam Map<String, String> grades,
                             @RequestParam(required = false) String interestsInput,
                             @RequestParam(required = false) String newInterest) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return "redirect:/login";
        }

        String email = auth.getName();
        Student student = studentRepository.findByEmail(email);
        if (student == null) return "redirect:/login";

        // ✅ NOTE
        grades.forEach((key, value) -> {
            if (key.startsWith("grades[")) {
                String subject = key.substring(7, key.length() - 1);
                try {
                    double grade = Double.parseDouble(value);
                    student.getGrades().put(subject, grade);
                } catch (NumberFormatException ignored) {}
            }
        });

        // ✅ INTERESE
        Set<String> interests = new HashSet<>();
        if (interestsInput != null && !interestsInput.isBlank()) {
            String[] tokens = interestsInput.split(",");
            for (String i : tokens) {
                String clean = i.trim();
                if (!clean.isEmpty()) interests.add(clean);
            }
        }

        if (newInterest != null && !newInterest.isBlank()) {
            interests.add(newInterest.trim());
        }

        if (!interests.isEmpty()) {
            student.setInterests(interests);
        }

        studentRepository.save(student);

        return "redirect:/account";
    }
}

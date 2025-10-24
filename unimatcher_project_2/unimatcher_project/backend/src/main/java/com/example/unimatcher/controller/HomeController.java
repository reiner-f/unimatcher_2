package com.example.unimatcher.controller;

import com.example.unimatcher.model.Student;
import com.example.unimatcher.repository.StudentRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Arrays;
import java.util.List;

@Controller
public class HomeController {

    private final StudentRepository studentRepository;

    public HomeController(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    @GetMapping("/")
    public String home(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            String email = auth.getName();
            Student student = studentRepository.findByEmail(email);
            if (student != null) {
                model.addAttribute("studentName", student.getName());
            }
        }

        return "index";
    }
}

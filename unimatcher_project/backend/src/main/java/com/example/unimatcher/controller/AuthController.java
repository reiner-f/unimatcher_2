// AuthController.java
package com.example.unimatcher.controller;

import com.example.unimatcher.request.RegisterRequest;
import com.example.unimatcher.model.Student;
import com.example.unimatcher.service.StudentService;
import jakarta.validation.Valid;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
public class AuthController {

    private final StudentService studentService;
    private final PasswordEncoder encoder;

    public AuthController(StudentService studentService, PasswordEncoder encoder) {
        this.studentService = studentService;
        this.encoder = encoder;
    }

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @GetMapping("/register")
    public String registerForm(Model model) {
        model.addAttribute("register", new RegisterRequest());
        return "register";
    }

    @PostMapping("/register")
    public String handleRegister(
            @ModelAttribute("register") @Valid RegisterRequest req,
            BindingResult br,
            Model model
    ) {
        if (br.hasErrors()) {
            return "register";
        }

        if (!req.getPassword().equals(req.getConfirmPassword())) {
            br.rejectValue("confirmPassword", "match", "Parola și confirmarea nu coincid");
            return "register";
        }

        if (studentService.existsByEmail(req.getEmail())) {
            br.rejectValue("email", "exists", "Există deja un cont cu acest email");
            return "register";
        }

        Student s = new Student();
        s.setName(req.getName());
        s.setEmail(req.getEmail().toLowerCase().trim());
        s.setPassword(encoder.encode(req.getPassword()));

        studentService.save(s);

        return "redirect:/login?registered";
    }
}

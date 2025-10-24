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
public class AboutController {

    @GetMapping("/about")
    public String about(Model model) {
        return "about";
    }
}

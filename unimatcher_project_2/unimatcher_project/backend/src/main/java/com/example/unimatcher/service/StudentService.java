package com.example.unimatcher.service;

import com.example.unimatcher.model.Student;
import com.example.unimatcher.repository.StudentRepository;
import org.springframework.stereotype.Service;

@Service
public class StudentService {

    private final StudentRepository repo;

    public StudentService(StudentRepository repo) {
        this.repo = repo;
    }

    public boolean existsByEmail(String email) {
        return repo.existsByEmail(email.toLowerCase().trim());
    }

    public void save(Student s) {
        s.setEmail(s.getEmail().toLowerCase().trim());
        repo.save(s);
    }

    public Student findByEmail(String email) {
        return repo.findByEmail(email.toLowerCase().trim());
    }
}

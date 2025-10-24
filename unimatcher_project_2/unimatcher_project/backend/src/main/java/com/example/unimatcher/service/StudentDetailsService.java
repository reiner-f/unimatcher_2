package com.example.unimatcher.service;

import com.example.unimatcher.model.Student;
import com.example.unimatcher.repository.StudentRepository;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Service
public class StudentDetailsService implements UserDetailsService {
    private final StudentRepository repo;

    public StudentDetailsService(StudentRepository repo) { this.repo = repo; }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Student s = repo.findByEmail(email.toLowerCase().trim());
        if (s == null) throw new UsernameNotFoundException("Student not found");
        return new org.springframework.security.core.userdetails.User(
                s.getEmail(),
                s.getPassword(),
                s.getRoles().stream().map(SimpleGrantedAuthority::new).collect(Collectors.toSet())
        );
    }
}

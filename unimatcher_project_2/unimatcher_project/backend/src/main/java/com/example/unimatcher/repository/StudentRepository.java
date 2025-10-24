package com.example.unimatcher.repository;

import com.example.unimatcher.model.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {
    Student findByEmail(String email);
    boolean existsByEmail(String email);
}


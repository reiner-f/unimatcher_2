package com.example.unimatcher.model;

import jakarta.persistence.*;
import java.util.*;

@Entity
@Table(name = "students")
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false, length = 60)
    private String password;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "student_roles", joinColumns = @JoinColumn(name = "student_id"))
    @Column(name = "role")
    private Set<String> roles = new HashSet<>(Set.of("ROLE_STUDENT"));

    @ElementCollection
    @CollectionTable(name = "student_grades", joinColumns = @JoinColumn(name = "student_id"))
    @MapKeyColumn(name = "subject")
    @Column(name = "grade")
    private Map<String, Double> grades = new HashMap<>();

    @ElementCollection
    @CollectionTable(name = "student_interests", joinColumns = @JoinColumn(name = "student_id"))
    @Column(name = "interest")
    private Set<String> interests = new HashSet<>();

    @ElementCollection
    @CollectionTable(name = "student_preferences", joinColumns = @JoinColumn(name = "student_id"))
    @Column(name = "preference")
    private Set<String> preferences = new HashSet<>();

    public Student() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public Set<String> getRoles() { return roles; }
    public void setRoles(Set<String> roles) { this.roles = roles; }

    public Map<String, Double> getGrades() { return grades; }
    public void setGrades(Map<String, Double> grades) { this.grades = grades; }

    public Set<String> getInterests() { return interests; }
    public void setInterests(Set<String> interests) { this.interests = interests; }

    public Set<String> getPreferences() { return preferences; }
    public void setPreferences(Set<String> preferences) { this.preferences = preferences; }
}

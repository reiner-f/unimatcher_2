package com.example.unimatcher.model;

import jakarta.persistence.*;
import java.util.*;

@Entity
@Table(name = "faculties")
public class Faculty {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=false, unique=true)
    private String name;

    @Column(length=2000)
    private String description;

    @ElementCollection
    @CollectionTable(name = "faculty_domains", joinColumns = @JoinColumn(name = "faculty_id"))
    @Column(name = "domain")
    private Set<String> domains = new HashSet<>();

    @ElementCollection
    @CollectionTable(name = "faculty_requirements", joinColumns = @JoinColumn(name = "faculty_id"))
    @Column(name = "requirement")
    private Set<String> requirements = new HashSet<>();

    public Long getId(){ return id; }
    public void setId(Long id){ this.id = id; }

    public String getName(){ return name; }
    public void setName(String name){ this.name = name; }

    public String getDescription(){ return description; }
    public void setDescription(String description){ this.description = description; }

    public Set<String> getDomains(){ return domains; }
    public void setDomains(Set<String> domains){ this.domains = domains; }

    public Set<String> getRequirements(){ return requirements; }
    public void setRequirements(Set<String> requirements){ this.requirements = requirements; } // ← Set

}

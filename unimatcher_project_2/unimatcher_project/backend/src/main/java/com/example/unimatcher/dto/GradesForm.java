package com.example.unimatcher.dto;

import java.util.Map;

public class GradesForm {
    private Map<String, Double> grades; // subject -> grade

    public Map<String, Double> getGrades() { return grades; }
    public void setGrades(Map<String, Double> grades) { this.grades = grades; }
}

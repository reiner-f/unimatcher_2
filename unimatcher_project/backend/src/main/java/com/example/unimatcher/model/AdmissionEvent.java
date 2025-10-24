package com.example.unimatcher.model;

import java.time.LocalDate;

public class AdmissionEvent {
    private String facultyName;
    private LocalDate startDate;
    private LocalDate endDate;
    private String description;

    public AdmissionEvent(String facultyName, LocalDate startDate, LocalDate endDate, String description) {
        this.facultyName = facultyName;
        this.startDate = startDate;
        this.endDate = endDate;
        this.description = description;
    }

    public String getFacultyName() { return facultyName; }
    public LocalDate getStartDate() { return startDate; }
    public LocalDate getEndDate() { return endDate; }
    public String getDescription() { return description; }
}

package com.example.unimatcher.model;

import java.util.LinkedHashMap;
import java.util.Map;

public class RecommendationForm {
    private String name;
    private String algorithm;
    private String interestsInput;
    private Map<String, Double> gradesInput = new LinkedHashMap<>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAlgorithm() {
        return algorithm;
    }

    public void setAlgorithm(String algorithm) {
        this.algorithm = algorithm;
    }

    public String getInterestsInput() {
        return interestsInput;
    }

    public void setInterestsInput(String interestsInput) {
        this.interestsInput = interestsInput;
    }

    public Map<String, Double> getGradesInput() {
        return gradesInput;
    }

    public void setGradesInput(Map<String, Double> gradesInput) {
        this.gradesInput = gradesInput;
    }
}

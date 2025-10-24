package com.example.unimatcher.util;

import org.springframework.core.io.ClassPathResource;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;

public final class CsvIO {

    private CsvIO() {}

    public static BufferedReader open(String relativePath) {
        try {
            ClassPathResource res = new ClassPathResource(relativePath);
            if (res.exists()) {
                return new BufferedReader(new InputStreamReader(res.getInputStream(), StandardCharsets.UTF_8));
            }
        } catch (Exception ignore) { }

        Path p = Paths.get(relativePath);
        if (Files.exists(p)) {
            try {
                return Files.newBufferedReader(p, StandardCharsets.UTF_8);
            } catch (Exception e) {
                throw new RuntimeException("Nu pot deschide " + p.toAbsolutePath() + ": " + e.getMessage(), e);
            }
        }

        Path up = Paths.get("../" + relativePath).normalize();
        if (Files.exists(up)) {
            try {
                return Files.newBufferedReader(up, StandardCharsets.UTF_8);
            } catch (Exception e) {
                throw new RuntimeException("Nu pot deschide " + up.toAbsolutePath() + ": " + e.getMessage(), e);
            }
        }

        throw new RuntimeException(
                "❌ Nu găsesc fișierul \"" + relativePath + "\" nici în classpath, nici în filesystem.\n" +
                        "📂 Working directory: " + Paths.get("").toAbsolutePath() + "\n" +
                        "💡 Pune fișierul în src/main/resources/" + relativePath + " sau în " + relativePath + " (lângă proiect)."
        );
    }
}

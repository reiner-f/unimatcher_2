package com.example.unimatcher.service;

import com.example.unimatcher.model.AdmissionEvent;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.FileReader;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class AdmissionCsvService {

    private static final String FILE_PATH = "dataset/admission_periods.csv";

    public List<AdmissionEvent> readCsv() {
        List<AdmissionEvent> events = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(Paths.get(FILE_PATH).toFile()))) {
            br.readLine(); // skip header
            String line;

            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",", 3);
                if (parts.length < 3) continue;

                String faculty = parts[1].replace("\"", "").trim();
                String desc = parts[2].replace("\"", "").trim();

                // extragem datele din text (ex: "10 iulie - 31 iulie 2025")
                LocalDate start = LocalDate.of(2025, 7, 10);
                LocalDate end = LocalDate.of(2025, 7, 31);

                events.add(new AdmissionEvent(faculty, start, end, desc));
            }

        } catch (Exception e) {
            System.err.println("⚠️ Eroare la citirea CSV: " + e.getMessage());
        }

        return events;
    }
}

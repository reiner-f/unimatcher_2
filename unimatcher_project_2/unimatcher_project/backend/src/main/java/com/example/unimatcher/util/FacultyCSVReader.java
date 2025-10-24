package com.example.unimatcher.util;

import com.example.unimatcher.model.Faculty;

import java.io.BufferedReader;
import java.util.*;

public class FacultyCSVReader {

    public static List<Faculty> load(String relativePath) {
        List<Faculty> list = new ArrayList<>();

        try (BufferedReader br = CsvIO.open(relativePath)) {
            String line = br.readLine();
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",", -1);
                if (parts.length < 2) continue;

                Faculty f = new Faculty();
                f.setId(Long.valueOf(parts[0].trim()));
                f.setName(parts[1].trim());
                f.setDescription(parts[2].trim());

                if (parts.length >= 4) f.setDomains(parseSet(parts[3]));
                if (parts.length >= 5) f.setRequirements(parseSet(parts[4]));

                list.add(f);
            }
        } catch (Exception e) {
            throw new RuntimeException("Eroare la citirea " + relativePath + ": " + e.getMessage(), e);
        }

        System.out.println("📚 Facultăți încărcate: " + list.size());
        return list;
    }

    private static Set<String> parseSet(String cell) {
        Set<String> out = new LinkedHashSet<>();
        if (cell == null || cell.isBlank()) return out;
        for (String t : cell.split("\\|")) {
            String v = t.trim();
            if (!v.isBlank()) out.add(v);
        }
        return out;
    }
}

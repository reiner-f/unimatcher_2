// src/main/java/com/example/unimatcher/controller/CalendarController.java
package com.example.unimatcher.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.time.LocalDate;
import java.util.*;

@Controller
public class CalendarController {

    @GetMapping("/calendar")
    public String calendarPage() {
        return "calendar";
    }

    @GetMapping("/calendar/events")
    @ResponseBody
    public List<Map<String, Object>> listEvents() {
        List<Map<String, Object>> events = new ArrayList<>();
        addEvent(events,
                "Admitere UAIC - Facultatea de Biologie",
                LocalDate.of(2025, 7, 10),
                LocalDate.of(2025, 7, 31),
                "#3B82F6");
        addEvent(events,
                "Admitere UBB - Facultatea de Informatică",
                LocalDate.of(2025, 7, 12),
                LocalDate.of(2025, 7, 28),
                "#10B981");
        addEvent(events,
                "Admitere UPB - Automatică și Calculatoare",
                LocalDate.of(2025, 7, 15),
                LocalDate.of(2025, 7, 30),
                "#F59E0B");
        addEvent(events,
                "Admitere ASE - Cibernetică",
                LocalDate.of(2025, 7, 16),
                LocalDate.of(2025, 7, 29),
                "#EF4444");
        addEvent(events,
                "Admitere UMF Carol Davila - Medicină",
                LocalDate.of(2025, 7, 20),
                LocalDate.of(2025, 7, 31),
                "#8B5CF6");
        return events;
    }

    private void addEvent(List<Map<String, Object>> events,
                          LocalDate start, LocalDate end, String title, String color) {
        Map<String, Object> e = new LinkedHashMap<>();
        e.put("title", title);
        e.put("start", start.toString());
        e.put("end", end.toString());
        e.put("allDay", true);
        e.put("color", color);
        events.add(e);
    }

    private void addEvent(List<Map<String, Object>> events,
                          String title, LocalDate start, LocalDate end, String color) {
        addEvent(events, start, end, title, color);
    }
}

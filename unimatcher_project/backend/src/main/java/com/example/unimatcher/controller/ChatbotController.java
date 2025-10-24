package com.example.unimatcher.controller;

import com.example.unimatcher.ai.ChatGPTClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
public class ChatbotController {

    @Autowired
    private ChatGPTClient chatGPTClient;

    @PostMapping("/chatbot")
    public Map<String, String> chat(@RequestBody Map<String, String> payload) {
        String facultyName = payload.get("facultyName");
        String question = payload.get("question");

        if (question == null || question.isBlank()) {
            Map<String, String> emptyResp = new HashMap<>();
            emptyResp.put("answer", "Întrebare goală!");
            return emptyResp;
        }

        if (facultyName == null || facultyName.isBlank()) {
            Map<String, String> emptyResp = new HashMap<>();
            emptyResp.put("answer", "Facultate necunoscută!");
            return emptyResp;
        }

        String prompt = String.format(
                "Sunt un asistent pentru studenți. Răspunde la întrebările despre facultatea '%s'.\nÎntrebare: %s",
                facultyName, question
        );

        String answer;
        try {
            answer = chatGPTClient.getAnswer(prompt);
        } catch (Exception e) {
            e.printStackTrace();
            answer = "Ne pare rău, a apărut o eroare la generarea răspunsului.";
        }

        Map<String, String> response = new HashMap<>();
        response.put("answer", answer);

        return response;
    }
}

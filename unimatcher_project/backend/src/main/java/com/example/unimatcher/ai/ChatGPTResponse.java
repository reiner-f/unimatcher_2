package com.example.unimatcher.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ChatGPTResponse {
    public static String extractText(String json) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(json);

            if (root.has("error")) {
                return "❌ Eroare API: " + root.path("error").path("message").asText();
            }

            JsonNode choices = root.path("choices");
            if (choices.isArray() && choices.size() > 0) {
                String text = choices.get(0)
                        .path("message")
                        .path("content")
                        .asText();
                return text.strip();
            } else {
                return "❌ Răspuns invalid: modelul nu a generat niciun text.";
            }

        } catch (Exception e) {
            return "Eroare la parsarea răspunsului: " + e.getMessage();
        }
    }
}

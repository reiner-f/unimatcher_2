package com.example.unimatcher.ai;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpRequest.BodyPublishers;
import java.nio.charset.StandardCharsets;

@Component
public class ChatGPTClient {

    private static final String API_KEY = "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";
    private static final String ENDPOINT = "https://api.openai.com/v1/chat/completions";

    private String sendPrompt(String prompt) throws Exception {
        JSONObject messageUser = new JSONObject();
        messageUser.put("role", "user");
        messageUser.put("content", prompt);

        JSONObject messageSystem = new JSONObject();
        messageSystem.put("role", "system");
        messageSystem.put("content", "You are a helpful assistant that provides precise and concise answers.");

        JSONArray messages = new JSONArray();
        messages.put(messageSystem);
        messages.put(messageUser);

        JSONObject payload = new JSONObject();
        payload.put("model", "gpt-4o-mini");
        payload.put("messages", messages);
        payload.put("temperature", 0.2);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(ENDPOINT))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + API_KEY)
                .POST(HttpRequest.BodyPublishers.ofString(payload.toString(), StandardCharsets.UTF_8))
                .build();

        HttpClient client = HttpClient.newHttpClient();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        return response.body();
    }

    public String getAnswer(String prompt) {
        try {
            String jsonResponse = sendPrompt(prompt);

            JSONObject obj = new JSONObject(jsonResponse);

            if (!obj.has("choices")) {
                System.err.println("❌ Răspuns OpenAI nu are 'choices': " + jsonResponse);
                return "Eroare la generarea răspunsului.";
            }

            JSONArray choices = obj.getJSONArray("choices");
            if (choices.isEmpty()) return "Eroare la generarea răspunsului.";

            JSONObject message = choices.getJSONObject(0).getJSONObject("message");
            String content = message.optString("content", "").trim();

            if (content.isBlank()) return "Eroare la generarea răspunsului.";

            return content;

        } catch (Exception e) {
            e.printStackTrace();
            return "Eroare la generarea răspunsului.";
        }
    }
}

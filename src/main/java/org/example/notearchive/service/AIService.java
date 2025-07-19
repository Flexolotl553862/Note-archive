package org.example.notearchive.service;

import com.google.genai.Client;
import org.springframework.stereotype.Service;

@Service
public class AIService {
    private final Client client;

    public AIService() {
        client = Client.builder().apiKey(System.getenv("GEMINI_API_KEY")).build();
    }

    public String generateMarkdown(String question) {
        StringBuilder sb = new StringBuilder();
        sb.append("Сгенерируй markdown на основании предоставленных мной данных, он должен быть красивым и красочным, в качестве ответа отдай только саму разметку, никаких пояснений писать не надо");
        String AI_MODEL = "gemini-2.5-flash";
        return client.models.generateContent(AI_MODEL, sb.append(question).toString(), null).text();
    }
}

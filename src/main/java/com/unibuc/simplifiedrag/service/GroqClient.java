package com.unibuc.simplifiedrag.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;

@Service
public class GroqClient {

    private static final String SYSTEM_PROMPT = """
            You are a helpful assistant that answers questions
            based strictly on the provided document context.
            If the answer is not in the context, say so clearly.
            Do not make up information.
            """;

    private final RestClient restClient;
    private final String model;

    public GroqClient(@Qualifier("groqRestClient") RestClient restClient,
                      @Value("${groq.model:llama-3.3-70b-versatile}") String model) {
        this.restClient = restClient;
        this.model = model;
    }

    public String complete(String userMessage) {
        var request = new ChatRequest(
                model,
                List.of(
                        new Message("system", SYSTEM_PROMPT),
                        new Message("user", userMessage)
                ),
                0.2
        );

        ChatCompletion response = restClient.post()
                .uri("/chat/completions")
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(ChatCompletion.class);

        return response.choices().get(0).message().content();
    }

    private record Message(String role, String content) {}

    private record ChatRequest(String model, List<Message> messages, double temperature) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record Choice(Message message) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record ChatCompletion(List<Choice> choices) {}
}

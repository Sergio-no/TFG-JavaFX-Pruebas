package org.example.tfgjavafxpruebas.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class AuthService {

    private static final String BASE_URL = "http://localhost:8080/api";
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();

    public String login(String email, String password) throws Exception {
        String body = mapper.writeValueAsString(
                java.util.Map.of("email", email, "password", password)
        );

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/auth/login"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        HttpResponse<String> response = httpClient.send(
                request, HttpResponse.BodyHandlers.ofString()
        );

        if (response.statusCode() != 200) {
            throw new RuntimeException("Login fallido: " + response.statusCode());
        }

        JsonNode json = mapper.readTree(response.body());
        return json.get("token").asText();
    }
}

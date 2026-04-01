package org.example.tfgjavafxpruebas.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class AuthService {

    // Pega aquí tu API Key de Firebase
    private static final String FIREBASE_API_KEY = "AIzaSyDiZH5LedWuNSuQU8jw3-TvTbUeZkeI28E";
    private static final String FIREBASE_URL =
            "https://identitytoolkit.googleapis.com/v1/accounts:signInWithPassword?key="
                    + FIREBASE_API_KEY;

    private final HttpClient client = HttpClient.newHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();

    public String login(String email, String password) throws Exception {
        String body = mapper.writeValueAsString(java.util.Map.of(
                "email", email,
                "password", password,
                "returnSecureToken", true
        ));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(FIREBASE_URL))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        HttpResponse<String> response = client.send(
                request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200)
            throw new RuntimeException("Credenciales incorrectas");

        JsonNode json = mapper.readTree(response.body());
        return json.get("idToken").asText();
    }

    public String getUid(String idToken) throws Exception {
        String body = mapper.writeValueAsString(
                java.util.Map.of("idToken", idToken));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(
                        "https://identitytoolkit.googleapis.com/v1/accounts:lookup?key="
                                + FIREBASE_API_KEY))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        HttpResponse<String> response = client.send(
                request, HttpResponse.BodyHandlers.ofString());

        JsonNode json = mapper.readTree(response.body());
        return json.path("users").get(0).get("localId").asText();
    }
}
package org.example.tfgjavafxpruebas.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.tfgjavafxpruebas.sesion.UserSesion;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

public class AuthService {

    // Pega aquí tu API Key de Firebase
    private static final String FIREBASE_API_KEY = "AIzaSyDiZH5LedWuNSuQU8jw3-TvTbUeZkeI28E";

    private static final String FIREBASE_SIGNIN_URL =
            "https://identitytoolkit.googleapis.com/v1/accounts:signInWithPassword?key="
                    + FIREBASE_API_KEY;
    private static final String FIREBASE_SIGNUP_URL =
            "https://identitytoolkit.googleapis.com/v1/accounts:signUp?key="
                    + FIREBASE_API_KEY;
    private static final String FIREBASE_LOOKUP_URL =
            "https://identitytoolkit.googleapis.com/v1/accounts:lookup?key="
                    + FIREBASE_API_KEY;

    private static final String BACKEND_BASE = "http://localhost:8080/api";

    private final HttpClient client = HttpClient.newHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();

    /** Login contra Firebase. Devuelve el idToken. */
    public String login(String email, String password) throws Exception {
        String body = mapper.writeValueAsString(Map.of(
                "email", email,
                "password", password,
                "returnSecureToken", true
        ));

        HttpResponse<String> response = client.send(
                HttpRequest.newBuilder()
                        .uri(URI.create(FIREBASE_SIGNIN_URL))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(body))
                        .build(),
                HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200)
            throw new RuntimeException("Credenciales incorrectas");

        return mapper.readTree(response.body()).get("idToken").asText();
    }

    /** Crea una cuenta nueva en Firebase. Devuelve el idToken. */
    public String signUp(String email, String password) throws Exception {
        String body = mapper.writeValueAsString(Map.of(
                "email", email,
                "password", password,
                "returnSecureToken", true
        ));

        HttpResponse<String> response = client.send(
                HttpRequest.newBuilder()
                        .uri(URI.create(FIREBASE_SIGNUP_URL))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(body))
                        .build(),
                HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            JsonNode err = mapper.readTree(response.body());
            String msg = err.path("error").path("message").asText("Error al registrar");
            if (msg.contains("EMAIL_EXISTS"))
                throw new RuntimeException("Ese email ya está registrado");
            if (msg.contains("WEAK_PASSWORD"))
                throw new RuntimeException("La contraseña debe tener al menos 6 caracteres");
            if (msg.contains("INVALID_EMAIL"))
                throw new RuntimeException("Email inválido");
            throw new RuntimeException("Error al registrar: " + msg);
        }

        return mapper.readTree(response.body()).get("idToken").asText();
    }

    public String getUid(String idToken) throws Exception {
        String body = mapper.writeValueAsString(Map.of("idToken", idToken));

        HttpResponse<String> response = client.send(
                HttpRequest.newBuilder()
                        .uri(URI.create(FIREBASE_LOOKUP_URL))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(body))
                        .build(),
                HttpResponse.BodyHandlers.ofString());

        return mapper.readTree(response.body())
                .path("users").get(0).get("localId").asText();
    }

    /** Registra el usuario en el backend (tras crearlo en Firebase). */
    public void registerInBackend(String uid, String nombre, String apellidos,
                                  String email, String telefono, String rol) throws Exception {
        String body = mapper.writeValueAsString(Map.of(
                "firebaseUid", uid,
                "nombre",      nombre,
                "apellidos",   apellidos,
                "email",       email,
                "telefono",    telefono == null ? "" : telefono,
                "rol",         rol
        ));

        HttpResponse<String> response = client.send(
                HttpRequest.newBuilder()
                        .uri(URI.create(BACKEND_BASE + "/auth/register"))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(body))
                        .build(),
                HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200)
            throw new RuntimeException("Error al registrar en backend: " + response.body());
    }

    /** Pide al backend el perfil del usuario actual y devuelve su rol. */
    public String getRolFromBackend(String idToken) throws Exception {
        HttpResponse<String> response = client.send(
                HttpRequest.newBuilder()
                        .uri(URI.create(BACKEND_BASE + "/auth/me"))
                        .header("Authorization", "Bearer " + idToken)
                        .GET().build(),
                HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200)
            throw new RuntimeException("No se pudo obtener el rol: " + response.body());

        return mapper.readTree(response.body()).path("rol").asText("CLIENTE");
    }
}
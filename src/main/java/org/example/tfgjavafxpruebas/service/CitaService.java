package org.example.tfgjavafxpruebas.service;

import org.example.tfgjavafxpruebas.model.Cita;
import org.example.tfgjavafxpruebas.sesion.UserSesion;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CitaService {

    private static final String BASE = "http://localhost:8080/api";
    private final HttpClient client  = HttpClient.newHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();

    private String token() {
        return "Bearer " + UserSesion.getInstance().getToken();
    }

    public List<Cita> getAll() throws Exception {
        HttpResponse<String> r = client.send(
                HttpRequest.newBuilder().uri(URI.create(BASE + "/citas"))
                        .header("Authorization", token()).GET().build(),
                HttpResponse.BodyHandlers.ofString());
        List<Cita> list = new ArrayList<>();
        for (JsonNode n : mapper.readTree(r.body()))
            list.add(fromJson(n));
        return list;
    }

    /**
     * Obtiene las horas disponibles para un día concreto.
     */
    public List<String> getHorasDisponibles(LocalDate fecha) throws Exception {
        HttpResponse<String> r = client.send(
                HttpRequest.newBuilder()
                        .uri(URI.create(BASE + "/citas/horas-disponibles?fecha=" + fecha.toString()))
                        .header("Authorization", token())
                        .GET().build(),
                HttpResponse.BodyHandlers.ofString());

        List<String> horas = new ArrayList<>();
        for (JsonNode n : mapper.readTree(r.body())) {
            horas.add(n.asText());
        }
        return horas;
    }

    public void confirmar(Long id) throws Exception {
        client.send(HttpRequest.newBuilder()
                        .uri(URI.create(BASE + "/citas/" + id + "/confirmar"))
                        .header("Authorization", token())
                        .PUT(HttpRequest.BodyPublishers.noBody()).build(),
                HttpResponse.BodyHandlers.ofString());
    }

    public void cancelar(Long id) throws Exception {
        client.send(HttpRequest.newBuilder()
                        .uri(URI.create(BASE + "/citas/" + id + "/cancelar"))
                        .header("Authorization", token())
                        .PUT(HttpRequest.BodyPublishers.noBody()).build(),
                HttpResponse.BodyHandlers.ofString());
    }

    public void crear(Map<String, Object> body) throws Exception {
        HttpResponse<String> r = client.send(HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/citas"))
                .header("Authorization", token())
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(body)))
                .build(), HttpResponse.BodyHandlers.ofString());
        if (r.statusCode() >= 300)
            throw new RuntimeException("Error al crear cita: " + r.body());
    }

    private Cita fromJson(JsonNode n) {
        return new Cita(
                n.get("id").asLong(),
                n.path("clienteNombre").asText("—"),
                n.path("vehiculo").asText("—"),
                n.path("fecha").asText(""),
                n.path("hora").asText(""),
                n.path("tipo").asText(""),
                n.path("estado").asText(""),
                n.path("descripcion").asText("")
        );
    }
}

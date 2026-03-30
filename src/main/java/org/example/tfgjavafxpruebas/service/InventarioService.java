package org.example.tfgjavafxpruebas.service;

import org.example.tfgjavafxpruebas.model.Pieza;
import org.example.tfgjavafxpruebas.sesion.UserSesion;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.*;
import java.util.*;

public class InventarioService {

    private static final String BASE = "http://localhost:8080/api";
    private final HttpClient client  = HttpClient.newHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();
    private String token() { return "Bearer " + UserSesion.getInstance().getToken(); }

    public List<Pieza> getAll() throws Exception {
        HttpResponse<String> r = client.send(
                HttpRequest.newBuilder().uri(URI.create(BASE + "/piezas"))
                        .header("Authorization", token()).GET().build(),
                HttpResponse.BodyHandlers.ofString());
        List<Pieza> list = new ArrayList<>();
        for (JsonNode n : mapper.readTree(r.body()))
            list.add(fromJson(n));
        return list;
    }

    public void crear(Map<String, Object> body) throws Exception {
        client.send(HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/piezas"))
                .header("Authorization", token())
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(body)))
                .build(), HttpResponse.BodyHandlers.ofString());
    }

    public void ajustarStock(Long id, int cantidad) throws Exception {
        client.send(HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/piezas/" + id + "/stock"))
                .header("Authorization", token())
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(
                        mapper.writeValueAsString(Map.of("cantidad", cantidad))))
                .build(), HttpResponse.BodyHandlers.ofString());
    }

    private Pieza fromJson(JsonNode n) {
        return new Pieza(
                n.get("id").asLong(),
                n.path("nombre").asText(),
                n.path("descripcion").asText(),
                n.path("precioUnitario").asDouble(),
                n.path("stockActual").asInt(),
                n.path("stockMinimo").asInt()
        );
    }
}

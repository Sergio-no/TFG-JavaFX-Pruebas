package org.example.tfgjavafxpruebas.service;

import org.example.tfgjavafxpruebas.model.Reparacion;
import org.example.tfgjavafxpruebas.sesion.UserSesion;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.*;
import java.util.*;

public class ReparacionesService {

    private static final String BASE = "http://localhost:8080/api";
    private final HttpClient client   = HttpClient.newHttpClient();
    private final ObjectMapper mapper  = new ObjectMapper();
    private String token() { return "Bearer " + UserSesion.getInstance().getToken(); }

    public List<Reparacion> getAll() throws Exception {
        HttpResponse<String> r = client.send(
                HttpRequest.newBuilder().uri(URI.create(BASE + "/reparaciones"))
                        .header("Authorization", token()).GET().build(),
                HttpResponse.BodyHandlers.ofString());
        List<Reparacion> list = new ArrayList<>();
        for (JsonNode n : mapper.readTree(r.body()))
            list.add(fromJson(n));
        return list;
    }

    public void cambiarEstado(Long id, String nuevoEstado) throws Exception {
        HttpResponse<String> r = client.send(HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/reparaciones/" + id + "/estado"))
                .header("Authorization", token())
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(
                        mapper.writeValueAsString(Map.of("estado", nuevoEstado))))
                .build(), HttpResponse.BodyHandlers.ofString());
        if (r.statusCode() >= 300)
            throw new RuntimeException("Error al cambiar estado: " + r.body());
    }

    public void crear(Map<String, Object> body) throws Exception {
        HttpResponse<String> r = client.send(HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/reparaciones"))
                .header("Authorization", token())
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(body)))
                .build(), HttpResponse.BodyHandlers.ofString());
        if (r.statusCode() >= 300)
            throw new RuntimeException("Error al crear reparación: " + r.body());
    }

    /**
     * NUEVO: Crea una reparación y devuelve su ID.
     */
    public Long crearYObtenerId(Map<String, Object> body) throws Exception {
        HttpResponse<String> r = client.send(HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/reparaciones"))
                .header("Authorization", token())
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(body)))
                .build(), HttpResponse.BodyHandlers.ofString());
        if (r.statusCode() >= 300)
            throw new RuntimeException("Error al crear reparación: " + r.body());

        JsonNode node = mapper.readTree(r.body());
        return node.get("id").asLong();
    }

    /**
     * NUEVO: Añade una pieza a una reparación.
     */
    public void addPieza(Long reparacionId, Long piezaId, int cantidad)
            throws Exception {
        Map<String, Object> body = Map.of(
                "piezaId", piezaId,
                "cantidadUsada", cantidad
        );
        HttpResponse<String> r = client.send(HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/reparaciones/" + reparacionId + "/piezas"))
                .header("Authorization", token())
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(body)))
                .build(), HttpResponse.BodyHandlers.ofString());
        if (r.statusCode() >= 300)
            throw new RuntimeException("Error al añadir pieza: " + r.body());
    }

    private Reparacion fromJson(JsonNode n) {
        return new Reparacion(
                n.get("id").asLong(),
                n.path("vehiculo").asText("—"),
                n.path("mecanico").asText("—"),
                n.path("fechaInicio").asText(""),
                n.path("fechaFin").asText(""),
                n.path("estado").asText(""),
                n.path("costeTotal").asText("0") + "€"
        );
    }
}

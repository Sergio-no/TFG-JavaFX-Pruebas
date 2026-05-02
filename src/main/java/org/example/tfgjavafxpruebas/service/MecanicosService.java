package org.example.tfgjavafxpruebas.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.tfgjavafxpruebas.sesion.UserSesion;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MecanicosService {

    private static final String BASE = "http://localhost:8080/api";
    private final HttpClient client  = HttpClient.newHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();
    private String token() { return "Bearer " + UserSesion.getInstance().getToken(); }

    /** Devuelve mecánicos activos. */
    public List<MecanicoItem> getActivos() throws Exception {
        HttpResponse<String> r = client.send(
                HttpRequest.newBuilder()
                        .uri(URI.create(BASE + "/mecanicos/activos"))
                        .header("Authorization", token())
                        .GET().build(),
                HttpResponse.BodyHandlers.ofString());

        List<MecanicoItem> list = new ArrayList<>();
        for (JsonNode n : mapper.readTree(r.body())) {
            list.add(fromJson(n));
        }
        return list;
    }

    /** Devuelve TODOS los mecánicos (activos e inactivos). */
    public List<MecanicoItem> getAll() throws Exception {
        HttpResponse<String> r = client.send(
                HttpRequest.newBuilder()
                        .uri(URI.create(BASE + "/mecanicos"))
                        .header("Authorization", token())
                        .GET().build(),
                HttpResponse.BodyHandlers.ofString());

        List<MecanicoItem> list = new ArrayList<>();
        for (JsonNode n : mapper.readTree(r.body())) {
            list.add(fromJson(n));
        }
        return list;
    }

    /** Crea un nuevo mecánico de taller. */
    public void crear(Map<String, Object> body) throws Exception {
        HttpResponse<String> r = client.send(HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/mecanicos"))
                .header("Authorization", token())
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(body)))
                .build(), HttpResponse.BodyHandlers.ofString());
        if (r.statusCode() >= 300)
            throw new RuntimeException("Error al crear mecánico: " + r.body());
    }

    /** Activa o desactiva un mecánico. */
    public void toggleActivo(Long id) throws Exception {
        HttpResponse<String> r = client.send(HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/mecanicos/" + id + "/toggle"))
                .header("Authorization", token())
                .PUT(HttpRequest.BodyPublishers.noBody())
                .build(), HttpResponse.BodyHandlers.ofString());
        if (r.statusCode() >= 300)
            throw new RuntimeException("Error al cambiar estado del mecánico: " + r.body());
    }

    private MecanicoItem fromJson(JsonNode n) {
        return new MecanicoItem(
                n.get("id").asLong(),
                n.path("nombre").asText() + " " + n.path("apellidos").asText(),
                n.path("especialidad").asText("—"),
                n.path("telefono").asText("—"),
                n.path("activoTaller").asBoolean(true)
        );
    }

    public static class MecanicoItem {
        private final Long id;
        private final String nombre;
        private final String especialidad;
        private final String telefono;
        private final boolean activo;

        public MecanicoItem(Long id, String nombre, String especialidad,
                            String telefono, boolean activo) {
            this.id = id;
            this.nombre = nombre;
            this.especialidad = especialidad;
            this.telefono = telefono;
            this.activo = activo;
        }

        public Long getId() { return id; }
        public String getNombre() { return nombre; }
        public String getEspecialidad() { return especialidad; }
        public String getTelefono() { return telefono; }
        public boolean isActivo() { return activo; }
        @Override public String toString() { return nombre; }
    }
}

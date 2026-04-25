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

public class EmpleadosService {

    private static final String BASE = "http://localhost:8080/api";
    private final HttpClient client  = HttpClient.newHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();
    private String token() { return "Bearer " + UserSesion.getInstance().getToken(); }

    public List<EmpleadoItem> getAll() throws Exception {
        HttpResponse<String> r = client.send(
                HttpRequest.newBuilder()
                        .uri(URI.create(BASE + "/empleados"))
                        .header("Authorization", token())
                        .GET().build(),
                HttpResponse.BodyHandlers.ofString());

        List<EmpleadoItem> list = new ArrayList<>();
        for (JsonNode n : mapper.readTree(r.body())) {
            list.add(new EmpleadoItem(
                    n.get("id").asLong(),
                    n.path("nombre").asText() + " " + n.path("apellidos").asText(),
                    n.path("email").asText(),
                    n.path("telefono").asText("—"),
                    n.path("rol").asText(),
                    n.path("activo").asBoolean(true)
            ));
        }
        return list;
    }

    public void desactivar(Long id) throws Exception {
        HttpResponse<String> r = client.send(HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/empleados/" + id + "/desactivar"))
                .header("Authorization", token())
                .PUT(HttpRequest.BodyPublishers.noBody())
                .build(), HttpResponse.BodyHandlers.ofString());
        if (r.statusCode() >= 300)
            throw new RuntimeException("Error al desactivar empleado: " + r.body());
    }

    public void activar(Long id) throws Exception {
        HttpResponse<String> r = client.send(HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/empleados/" + id + "/activar"))
                .header("Authorization", token())
                .PUT(HttpRequest.BodyPublishers.noBody())
                .build(), HttpResponse.BodyHandlers.ofString());
        if (r.statusCode() >= 300)
            throw new RuntimeException("Error al activar empleado: " + r.body());
    }

    public static class EmpleadoItem {
        private final Long id;
        private final String nombre;
        private final String email;
        private final String telefono;
        private final String rol;
        private final boolean activo;

        public EmpleadoItem(Long id, String nombre, String email,
                            String telefono, String rol, boolean activo) {
            this.id = id;
            this.nombre = nombre;
            this.email = email;
            this.telefono = telefono;
            this.rol = rol;
            this.activo = activo;
        }

        public Long getId() { return id; }
        public String getNombre() { return nombre; }
        public String getEmail() { return email; }
        public String getTelefono() { return telefono; }
        public String getRol() { return rol; }
        public boolean isActivo() { return activo; }
    }
}


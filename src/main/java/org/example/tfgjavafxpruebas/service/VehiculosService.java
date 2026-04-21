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

public class VehiculosService {

    private static final String BASE = "http://localhost:8080/api";
    private final HttpClient client  = HttpClient.newHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();
    private String token() { return "Bearer " + UserSesion.getInstance().getToken(); }

    /**
     * Busca un vehículo por su matrícula.
     * Devuelve un VehiculoItem con id, matrícula, descripción y clienteId.
     */
    public VehiculoItem getByMatricula(String matricula) throws Exception {
        HttpResponse<String> r = client.send(
                HttpRequest.newBuilder()
                        .uri(URI.create(BASE + "/vehiculos/matricula/"
                                + matricula.trim().toUpperCase()))
                        .header("Authorization", token())
                        .GET().build(),
                HttpResponse.BodyHandlers.ofString());

        if (r.statusCode() == 404)
            throw new RuntimeException("No se encontró vehículo con matrícula: " + matricula);
        if (r.statusCode() != 200)
            throw new RuntimeException("Error al buscar vehículo: " + r.body());

        JsonNode n = mapper.readTree(r.body());
        return fromJson(n);
    }

    /**
     * Devuelve los vehículos de un cliente.
     */
    public List<VehiculoItem> getByCliente(Long clienteId) throws Exception {
        HttpResponse<String> r = client.send(
                HttpRequest.newBuilder()
                        .uri(URI.create(BASE + "/vehiculos/cliente/" + clienteId))
                        .header("Authorization", token())
                        .GET().build(),
                HttpResponse.BodyHandlers.ofString());

        List<VehiculoItem> list = new ArrayList<>();
        for (JsonNode n : mapper.readTree(r.body()))
            list.add(fromJson(n));
        return list;
    }

    private VehiculoItem fromJson(JsonNode n) {
        return new VehiculoItem(
                n.get("id").asLong(),
                n.path("matricula").asText(),
                n.path("marca").asText() + " " + n.path("modelo").asText(),
                n.path("clienteNombre").asText("—"),
                n.path("clienteId").asLong()
        );
    }

    public static class VehiculoItem {
        private final Long id;
        private final String matricula;
        private final String descripcion;
        private final String clienteNombre;
        private final Long clienteId;

        public VehiculoItem(Long id, String matricula, String descripcion,
                            String clienteNombre, Long clienteId) {
            this.id = id;
            this.matricula = matricula;
            this.descripcion = descripcion;
            this.clienteNombre = clienteNombre;
            this.clienteId = clienteId;
        }

        public Long getId() { return id; }
        public String getMatricula() { return matricula; }
        public String getDescripcion() { return descripcion; }
        public String getClienteNombre() { return clienteNombre; }
        public Long getClienteId() { return clienteId; }

        @Override
        public String toString() {
            return matricula + " — " + descripcion + " (" + clienteNombre + ")";
        }
    }
}


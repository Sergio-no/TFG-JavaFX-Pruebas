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

public class MecanicosService {

    private static final String BASE = "http://localhost:8080/api";
    private final HttpClient client  = HttpClient.newHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();
    private String token() { return "Bearer " + UserSesion.getInstance().getToken(); }

    /** Devuelve mecánicos activos como pares {id, "Nombre Apellidos"}. */
    public List<MecanicoItem> getActivos() throws Exception {
        HttpResponse<String> r = client.send(
                HttpRequest.newBuilder()
                        .uri(URI.create(BASE + "/mecanicos/activos"))
                        .header("Authorization", token())
                        .GET().build(),
                HttpResponse.BodyHandlers.ofString());

        List<MecanicoItem> list = new ArrayList<>();
        for (JsonNode n : mapper.readTree(r.body())) {
            list.add(new MecanicoItem(
                    n.get("id").asLong(),
                    n.path("nombre").asText() + " " + n.path("apellidos").asText()
            ));
        }
        return list;
    }

    public static class MecanicoItem {
        private final Long id;
        private final String nombre;
        public MecanicoItem(Long id, String nombre) { this.id = id; this.nombre = nombre; }
        public Long getId() { return id; }
        public String getNombre() { return nombre; }
        @Override public String toString() { return nombre; }
    }
}

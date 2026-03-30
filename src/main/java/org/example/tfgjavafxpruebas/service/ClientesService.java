package org.example.tfgjavafxpruebas.service;

import org.example.tfgjavafxpruebas.model.Cliente;
import org.example.tfgjavafxpruebas.sesion.UserSesion;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.*;
import java.util.*;

public class ClientesService {

    private static final String BASE = "http://localhost:8080/api";
    private final HttpClient client  = HttpClient.newHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();
    private String token() { return "Bearer " + UserSesion.getInstance().getToken(); }

    public List<Cliente> getAll() throws Exception {
        HttpResponse<String> r = client.send(
                HttpRequest.newBuilder().uri(URI.create(BASE + "/clientes"))
                        .header("Authorization", token()).GET().build(),
                HttpResponse.BodyHandlers.ofString());
        List<Cliente> list = new ArrayList<>();
        for (JsonNode n : mapper.readTree(r.body()))
            list.add(fromJson(n));
        return list;
    }

    private Cliente fromJson(JsonNode n) {
        return new Cliente(
                n.get("id").asLong(),
                n.path("nombre").asText() + " " + n.path("apellidos").asText(),
                n.path("email").asText(),
                n.path("telefono").asText("—"),
                n.path("puntosAcumulados").asInt(0),
                n.path("totalGastado").asText("0") + "€",
                n.path("fechaRegistro").asText("")
        );
    }
}

package org.example.tfgjavafxpruebas.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.tfgjavafxpruebas.model.MecanicoStats;
import org.example.tfgjavafxpruebas.model.Valoracion;
import org.example.tfgjavafxpruebas.sesion.UserSesion;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

public class EstadisticasService {

    private static final String BASE = "http://localhost:8080/api";
    private final HttpClient client  = HttpClient.newHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();
    private String token() { return "Bearer " + UserSesion.getInstance().getToken(); }

    public JsonNode getRaw() throws Exception {
        HttpResponse<String> r = client.send(
                HttpRequest.newBuilder()
                        .uri(URI.create(BASE + "/estadisticas"))
                        .header("Authorization", token())
                        .GET().build(),
                HttpResponse.BodyHandlers.ofString());
        return mapper.readTree(r.body());
    }

    public List<MecanicoStats> getMecanicoStats() throws Exception {
        JsonNode data = getRaw();
        List<MecanicoStats> list = new ArrayList<>();
        for (JsonNode n : data.path("mecanicoStats")) {
            list.add(new MecanicoStats(
                    n.path("nombre").asText("—"),
                    n.path("reparaciones").asInt(0),
                    n.path("ingresos").asText("0€")
            ));
        }
        return list;
    }

    public List<Valoracion> getUltimasValoraciones() throws Exception {
        JsonNode data = getRaw();
        List<Valoracion> list = new ArrayList<>();
        for (JsonNode n : data.path("ultimasValoraciones")) {
            list.add(new Valoracion(
                    n.path("clienteNombre").asText("—"),
                    n.path("puntuacion").asInt(0),
                    n.path("comentario").asText("")
            ));
        }
        return list;
    }
}

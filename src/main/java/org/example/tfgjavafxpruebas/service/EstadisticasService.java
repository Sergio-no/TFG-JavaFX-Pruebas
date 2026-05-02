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

    /**
     * Una sola llamada al backend que devuelve todo.
     */
    public JsonNode getRaw() throws Exception {
        HttpResponse<String> r = client.send(
                HttpRequest.newBuilder()
                        .uri(URI.create(BASE + "/estadisticas"))
                        .header("Authorization", token())
                        .GET().build(),
                HttpResponse.BodyHandlers.ofString());
        if (r.statusCode() != 200) {
            throw new RuntimeException("Error HTTP " + r.statusCode() + ": " + r.body());
        }
        return mapper.readTree(r.body());
    }

    /**
     * Extrae las stats de mecánicos de un JsonNode ya cargado.
     */
    public List<MecanicoStats> getMecanicoStatsFromNode(JsonNode data) {
        List<MecanicoStats> list = new ArrayList<>();
        JsonNode statsNode = data.path("mecanicoStats");
        if (statsNode.isMissingNode() || !statsNode.isArray()) return list;
        for (JsonNode n : statsNode) {
            list.add(new MecanicoStats(
                    n.path("nombre").asText("—"),
                    n.path("reparaciones").asInt(0),
                    n.path("ingresos").asText("0€")
            ));
        }
        return list;
    }

    /**
     * Extrae las últimas valoraciones de un JsonNode ya cargado.
     */
    public List<Valoracion> getUltimasValoracionesFromNode(JsonNode data) {
        List<Valoracion> list = new ArrayList<>();
        JsonNode valNode = data.path("ultimasValoraciones");
        if (valNode.isMissingNode() || !valNode.isArray()) return list;
        for (JsonNode n : valNode) {
            list.add(new Valoracion(
                    n.path("clienteNombre").asText("—"),
                    n.path("puntuacion").asInt(0),
                    n.path("comentario").asText("")
            ));
        }
        return list;
    }

    // ── Métodos legacy por compatibilidad (hacen su propia llamada) ──

    public List<MecanicoStats> getMecanicoStats() throws Exception {
        return getMecanicoStatsFromNode(getRaw());
    }

    public List<Valoracion> getUltimasValoraciones() throws Exception {
        return getUltimasValoracionesFromNode(getRaw());
    }
}
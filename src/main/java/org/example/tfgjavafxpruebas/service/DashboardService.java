package org.example.tfgjavafxpruebas.service;

import org.example.tfgjavafxpruebas.model.AlertaStock;
import org.example.tfgjavafxpruebas.model.CitaResumen;
import org.example.tfgjavafxpruebas.model.ReparacionResumen;
import org.example.tfgjavafxpruebas.sesion.UserSesion;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

public class DashboardService {

    private static final String BASE_URL = "http://localhost:8080/api";
    private final HttpClient client = HttpClient.newHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();

    private JsonNode getDashboardData() throws Exception {
        HttpResponse<String> response = client.send(
                HttpRequest.newBuilder()
                        .uri(URI.create(BASE_URL + "/dashboard"))
                        .header("Authorization", "Bearer " + UserSesion.getInstance().getToken())
                        .GET().build(),
                HttpResponse.BodyHandlers.ofString());
        return mapper.readTree(response.body());
    }

    public List<CitaResumen> getCitasHoy() throws Exception {
        JsonNode data = getDashboardData();
        List<CitaResumen> result = new ArrayList<>();
        for (JsonNode n : data.path("proximasCitas")) {
            result.add(new CitaResumen(
                    n.path("clienteNombre").asText(),
                    n.path("hora").asText(),
                    n.path("tipo").asText("—"),
                    n.path("estado").asText()
            ));
        }
        return result;
    }

    public List<ReparacionResumen> getReparacionesActivas() throws Exception {
        JsonNode data = getDashboardData();
        List<ReparacionResumen> result = new ArrayList<>();
        for (JsonNode n : data.path("reparacionesEnCurso")) {
            result.add(new ReparacionResumen(
                    n.path("vehiculo").asText("—"),
                    n.path("mecanico").asText("—"),
                    n.path("fechaInicio").asText(""),
                    n.path("estado").asText(""),
                    n.path("costeTotal").asText("0") + "€"
            ));
        }
        return result;
    }

    public List<AlertaStock> getAlertasStock() throws Exception {
        JsonNode data = getDashboardData();
        List<AlertaStock> result = new ArrayList<>();
        for (JsonNode n : data.path("piezasStockBajo")) {
            result.add(new AlertaStock(
                    n.path("nombre").asText(),
                    n.path("stockActual").asInt(),
                    n.path("stockMinimo").asInt()
            ));
        }
        return result;
    }
}
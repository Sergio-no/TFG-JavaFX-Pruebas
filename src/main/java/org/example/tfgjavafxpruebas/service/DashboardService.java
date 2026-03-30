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

    private HttpRequest.Builder authedRequest(String path) {
        return HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + path))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + UserSesion.getInstance().getToken());
    }

    public List<CitaResumen> getCitasHoy() throws Exception {
        HttpResponse<String> response = client.send(
                authedRequest("/dashboard/citas-hoy").GET().build(),
                HttpResponse.BodyHandlers.ofString()
        );
        List<CitaResumen> result = new ArrayList<>();
        JsonNode arr = mapper.readTree(response.body());
        for (JsonNode node : arr) {
            result.add(new CitaResumen(
                    node.get("clienteNombre").asText(),
                    node.get("hora").asText(),
                    node.get("tipo").asText(),
                    node.get("estado").asText()
            ));
        }
        return result;
    }

    public List<AlertaStock> getAlertasStock() throws Exception {
        HttpResponse<String> response = client.send(
                authedRequest("/dashboard/alertas-stock").GET().build(),
                HttpResponse.BodyHandlers.ofString()
        );
        List<AlertaStock> result = new ArrayList<>();
        JsonNode arr = mapper.readTree(response.body());
        for (JsonNode node : arr) {
            result.add(new AlertaStock(
                    node.get("nombre").asText(),
                    node.get("stockActual").asInt(),
                    node.get("stockMinimo").asInt()
            ));
        }
        return result;
    }

    public List<ReparacionResumen> getReparacionesActivas() throws Exception {
        HttpResponse<String> response = client.send(
                authedRequest("/dashboard/reparaciones-activas").GET().build(),
                HttpResponse.BodyHandlers.ofString()
        );
        List<ReparacionResumen> result = new ArrayList<>();
        JsonNode arr = mapper.readTree(response.body());
        for (JsonNode node : arr) {
            result.add(new ReparacionResumen(
                    node.get("vehiculo").asText(),
                    node.get("mecanico").asText(),
                    node.get("fechaInicio").asText(),
                    node.get("estado").asText(),
                    node.get("costeTotal").asText() + "€"
            ));
        }
        return result;
    }

    public int getCitasHoyCount()       throws Exception { return getCitasHoy().size(); }
    public int getReparacionesCount()   throws Exception { return getReparacionesActivas().size(); }
    public int getAlertasStockCount()   throws Exception { return getAlertasStock().size(); }
}

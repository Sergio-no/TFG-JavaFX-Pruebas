package org.example.tfgjavafxpruebas.service;

import org.example.tfgjavafxpruebas.model.Factura;
import org.example.tfgjavafxpruebas.sesion.UserSesion;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.*;
import java.util.*;

public class FacturasService {

    private static final String BASE = "http://localhost:8080/api";
    private final HttpClient client  = HttpClient.newHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();
    private String token() { return "Bearer " + UserSesion.getInstance().getToken(); }

    public List<Factura> getAll() throws Exception {
        HttpResponse<String> r = client.send(
                HttpRequest.newBuilder().uri(URI.create(BASE + "/facturas"))
                        .header("Authorization", token()).GET().build(),
                HttpResponse.BodyHandlers.ofString());
        List<Factura> list = new ArrayList<>();
        for (JsonNode n : mapper.readTree(r.body()))
            list.add(fromJson(n));
        return list;
    }

    public void marcarPagada(Long id, String metodo) throws Exception {
        client.send(HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/facturas/" + id + "/pagar"))
                .header("Authorization", token())
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(
                        mapper.writeValueAsString(Map.of("metodoPago", metodo))))
                .build(), HttpResponse.BodyHandlers.ofString());
    }

    /**
     * Descarga la factura como PDF (bytes).
     */
    public byte[] descargarPdf(Long id) throws Exception {
        HttpResponse<byte[]> r = client.send(
                HttpRequest.newBuilder()
                        .uri(URI.create(BASE + "/facturas/" + id + "/pdf"))
                        .header("Authorization", token())
                        .GET().build(),
                HttpResponse.BodyHandlers.ofByteArray());

        if (r.statusCode() != 200)
            throw new RuntimeException("Error al descargar PDF (código " + r.statusCode() + ")");

        return r.body();
    }

    private Factura fromJson(JsonNode n) {
        return new Factura(
                n.get("id").asLong(),
                n.path("numeroFactura").asText(),
                n.path("clienteNombre").asText("—"),
                n.path("fecha").asText(""),
                n.path("total").asText("0") + "€",
                n.path("metodoPago").asText(null),
                n.path("pagada").asBoolean(false)
        );
    }
}

package ar.edu.utn.frba.dds.domain.geolocalizacion;

import ar.edu.utn.frba.dds.domain.info.PuntoGeografico;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

/**
 * Implementación asíncrona de GeoApi que utiliza el servicio Nominatim de OpenStreetMap.
 * Incluye un LimitadorRequests para respetar la política de uso de 1 petición por segundo.
 */
public class GeoOpenStreetMaps implements GeoApi {
  private static final String API_BASE_URL = "https://nominatim.openstreetmap.org/";
  private final String userAgent;
  private final LimitadorRequests limitador;

  /**
   * Constructor que requiere un User-Agent.
   * @param userAgent Identificador de la aplicación cliente (ej: "MiAppDeHechos/1.0").
   * Es un requisito de la política de uso de OpenStreetMap.
   */
  public GeoOpenStreetMaps(String userAgent) {
    if (userAgent == null || userAgent.isBlank()) {
      throw new IllegalArgumentException("El User-Agent no puede estar vacío.");
    }
    this.userAgent = userAgent;
    // Creamos un limitador para asegurar un intervalo de al menos 1000ms entre peticiones
    this.limitador = new LimitadorRequests(1000);
  }

  @Override
  public CompletableFuture<String> obtenerProvincia(double lat, double lon) {
    // Entregamos la lógica de la petición al limitador para que la ejecute cuando corresponda
    return limitador.submit(() -> CompletableFuture.supplyAsync(() -> {
      String urlString = API_BASE_URL + "reverse?format=json&lat=" + lat + "&lon=" + lon;
      try {
        JsonNode jsonNode = this.consultarApi(urlString);
        // En OpenStreetMap, la provincia suele estar en el campo 'state'
        return jsonNode.path("address").path("state").asText(null);
      } catch (IOException e) {
        throw new CompletionException(e);
      }
    }));
  }

  @Override
  public CompletableFuture<PuntoGeografico> obtenerUbicacion(String nombreProvincia) {
    return limitador.submit(() -> CompletableFuture.supplyAsync(() -> {
      try {
        String provinciaCodificada = URLEncoder.encode(nombreProvincia, StandardCharsets.UTF_8);
        String urlString = API_BASE_URL + "search?q=" + provinciaCodificada + "&format=json&countrycodes=ar";
        JsonNode jsonNode = this.consultarApi(urlString);
        if (jsonNode.isArray() && !jsonNode.isEmpty()) {
          JsonNode primerResultado = jsonNode.get(0);
          double lat = primerResultado.path("lat").asDouble();
          double lon = primerResultado.path("lon").asDouble();
          return new PuntoGeografico(lat, lon);
        }
        return null;
      } catch (IOException e) {
        throw new CompletionException(e);
      }
    }));
  }

  private JsonNode consultarApi(String urlString) throws IOException {
    URL url = new URL(urlString);
    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
    conn.setRequestMethod("GET");
    // Es mandatorio incluir el User-Agent en la cabecera
    conn.setRequestProperty("User-Agent", this.userAgent);
    conn.connect();

    if (conn.getResponseCode() != 200) {
      throw new IOException("Error en la API de OpenStreetMap: " + conn.getResponseCode() + " " + conn.getResponseMessage());
    }

    StringBuilder response = new StringBuilder();
    try (Scanner sc = new Scanner(conn.getInputStream())) {
      while (sc.hasNext()) {
        response.append(sc.nextLine());
      }
    }

    ObjectMapper mapper = new ObjectMapper();
    return mapper.readTree(response.toString());
  }
}


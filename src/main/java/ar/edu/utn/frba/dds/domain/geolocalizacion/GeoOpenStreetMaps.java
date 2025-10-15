package ar.edu.utn.frba.dds.domain.geolocalizacion;

import ar.edu.utn.frba.dds.domain.info.PuntoGeografico;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

public class GeoOpenStreetMaps implements GeoApi {
  private final String apiBaseUrl;
  private final String userAgent;
  private final LimitadorRequests limitador;

  /**
   * Constructor para producción. Limpio y simple.
   * @param userAgent Identificador de la aplicación cliente (ej: "MiAppDeHechos/1.0").
   */
  public GeoOpenStreetMaps(String userAgent) {
    // Llama al constructor de testing/interno con los valores por defecto.
    this("https://nominatim.openstreetmap.org/", userAgent, 1000);
  }

  /**
   * Constructor que permite especificar un intervalo para el limitador de peticiones.
   * @param minIntervalMillis Intervalo mínimo en milisegundos entre peticiones.
   */
  public GeoOpenStreetMaps(String userAgent, long minIntervalMillis) {
    this("https://nominatim.openstreetmap.org/",userAgent, minIntervalMillis);
  }

  /**
   * Constructor con visibilidad de paquete para testing y flexibilidad.
   * Permite inyectar dependencias como la URL base y la configuración del limitador.
   * NO es visible fuera de este paquete.
   * @param apiBaseUrl La URL base del servicio Nominatim.
   * @param userAgent Identificador de la aplicación cliente.
   * @param limitadorIntervalo Intervalo en ms para el LimitadorRequests.
   */
  public GeoOpenStreetMaps(String apiBaseUrl, String userAgent, long limitadorIntervalo) {
    if (userAgent == null || userAgent.isBlank()) {
      throw new IllegalArgumentException("El User-Agent no puede estar vacío.");
    }
    this.apiBaseUrl = apiBaseUrl;
    this.userAgent = userAgent;
    this.limitador = new LimitadorRequests(limitadorIntervalo);
  }

  @Override
  public CompletableFuture<String> obtenerProvincia(double lat, double lon) {
    return limitador.submit(() -> CompletableFuture.supplyAsync(() -> {
      String urlString = apiBaseUrl + "reverse?format=json&lat=" + lat + "&lon=" + lon;
      try {
        JsonNode jsonNode = this.consultarApi(urlString);
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
        String provinciaCodificada = URLEncoder.encode(nombreProvincia, StandardCharsets.UTF_8.toString());
        String urlString = apiBaseUrl + "search?q=" + provinciaCodificada + "&format=json&countrycodes=ar";
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


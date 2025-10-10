package ar.edu.utn.frba.dds.domain.geolocalizacion;

import ar.edu.utn.frba.dds.domain.info.PuntoGeografico;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

/**
 * Implementación asíncrona de GeoApi que utiliza el servicio oficial GeoRef de Argentina.
 * Incluye un LimitadorRequests para evitar un número excesivo de peticiones simultáneas.
 */
public class GeoGeoref implements GeoApi {
  private static final String API_BASE_URL = "https://apis.datos.gob.ar/georef/api/";
  private final LimitadorRequests limitador;

  /**
   * Constructor por defecto. Utiliza un intervalo de 50ms entre peticiones.
   */
  public GeoGeoref() {
    this.limitador = new LimitadorRequests(50);
  }

  /**
   * Constructor que permite especificar un intervalo para el limitador de peticiones.
   * @param minIntervalMillis Intervalo mínimo en milisegundos entre peticiones.
   */
  public GeoGeoref(long minIntervalMillis) {
    this.limitador = new LimitadorRequests(minIntervalMillis);
  }

  @Override
  public CompletableFuture<String> obtenerProvincia(double lat, double lon) {
    return limitador.submit(() -> CompletableFuture.supplyAsync(() -> {
      String urlString = API_BASE_URL + "ubicacion?lat=" + lat + "&lon=" + lon + "&campos=provincia.nombre";
      try {
        JsonNode jsonNode = this.consultarApiGet(urlString);
        return jsonNode.path("ubicacion").path("provincia").path("nombre").asText(null);
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
        String urlString = API_BASE_URL + "provincias?nombre=" + provinciaCodificada;
        JsonNode jsonNode = this.consultarApiGet(urlString);
        JsonNode provincias = jsonNode.path("provincias");
        if (provincias.isArray() && !provincias.isEmpty()) {
          JsonNode primeraProvincia = provincias.get(0);
          double latitud = primeraProvincia.path("centroide").path("lat").asDouble();
          double longitud = primeraProvincia.path("centroide").path("lon").asDouble();
          return new PuntoGeografico(latitud, longitud);
        }
        return null;
      } catch (IOException e) {
        throw new CompletionException(e);
      }
    }));
  }

  private JsonNode consultarApiGet(String urlString) throws IOException {
    URL url = new URL(urlString);
    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
    conn.setRequestMethod("GET");
    conn.connect();
    return procesarRespuesta(conn);
  }

  private JsonNode procesarRespuesta(HttpURLConnection conn) throws IOException {
    if (conn.getResponseCode() != 200) {
      throw new IOException("Error en la API de GeoRef: " + conn.getResponseCode());
    }

    StringBuilder response = new StringBuilder();
    try (Scanner sc = new Scanner(conn.getInputStream(), StandardCharsets.UTF_8)) {
      while (sc.hasNext()) {
        response.append(sc.nextLine());
      }
    }

    ObjectMapper mapper = new ObjectMapper();
    return mapper.readTree(response.toString());
  }
}


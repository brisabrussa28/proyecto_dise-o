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

public class ServicioGeoref {

  private static final String API_BASE_URL = "https://apis.datos.gob.ar/georef/api/";

  public String obtenerProvincia(double lat, double lon) {
    String urlString = API_BASE_URL + "ubicacion?lat=" + lat + "&lon=" + lon + "&campos=provincia.nombre";

    try {
      JsonNode jsonNode = this.consultarApi(urlString);
      return jsonNode.path("ubicacion")
                     .path("provincia")
                     .path("nombre")
                     .asText(null); // Devuelve null si no se encuentra
    } catch (IOException e) {
      throw new RuntimeException("Error consultando la API de georef para obtener provincia", e);
    }
  }

  /**
   * Obtiene la ubicación (latitud y longitud del centroide) de una provincia a partir de su nombre.
   *
   * @param nombreProvincia El nombre de la provincia a buscar.
   * @return Un objeto PuntoGeografico con las coordenadas, o null si no se encuentra o hay un error.
   */
  public PuntoGeografico obtenerUbicacion(String nombreProvincia) {
    try {
      String provinciaCodificada = URLEncoder.encode(nombreProvincia, StandardCharsets.UTF_8);
      String urlString = API_BASE_URL + "provincias?nombre=" + provinciaCodificada;

      JsonNode jsonNode = this.consultarApi(urlString);

      JsonNode provincias = jsonNode.path("provincias");
      if (provincias.isArray() && !provincias.isEmpty()) {
        JsonNode primeraProvincia = provincias.get(0);
        double lat = primeraProvincia.path("centroide").path("lat").asDouble();
        double lon = primeraProvincia.path("centroide").path("lon").asDouble();
        return new PuntoGeografico(lat, lon);
      }

      return null; // Si no se encontraron provincias
    } catch (IOException e) {
      // deberiamos lanzar una excepcion personalizada TODO
      System.err.println("Error consultando la API de georef para obtener ubicación: " + e.getMessage());
      return null;
    }
  }

  /**
   * Metodo privado y reutilizable para realizar la llamada a la API y parsear el JSON.
   *
   * @param urlString La URL completa a la que se va a consultar.
   * @return El nodo raíz del JSON de la respuesta.
   * @throws IOException Si ocurre un error de conexión o de lectura.
   */
  private JsonNode consultarApi(String urlString) throws IOException {
    URL url = new URL(urlString);
    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
    conn.setRequestMethod("GET");
    conn.connect();

    if (conn.getResponseCode() != 200) {
      throw new IOException("Error en la API de GeoRef: " + conn.getResponseCode());
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

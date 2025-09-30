package ar.edu.utn.frba.dds.domain.geolocalizacion;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

public class ServicioGeoref {

  private static final String API_URL = "https://apis.datos.gob.ar/georef/api/ubicacion?";

  public String obtenerProvincia(double lat, double lon) {
    String urlString = API_URL + "lat=" + lat + "&lon=" + lon + "&campos=provincia.nombre";

    try {
      URL url = new URL(urlString);
      HttpURLConnection conn = (HttpURLConnection) url.openConnection();
      conn.setRequestMethod("GET");

      if (conn.getResponseCode() != 200) {
        throw new RuntimeException("Error en la API de GeoLocalizacion: " + conn.getResponseCode());
      }

      try (Scanner sc = new Scanner(conn.getInputStream())) {
        StringBuilder response = new StringBuilder();
        while (sc.hasNext()) {
          response.append(sc.nextLine());
        }

        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonNode = mapper.readTree(response.toString());

        return jsonNode.path("ubicacion")
            .path("provincia")
            .path("nombre")
            .asText(null); // null si no existe
      }
    } catch (IOException e) {
      throw new RuntimeException("Error consultando la API de georef", e);
    }
  }
}


package ar.edu.utn.frba.dds.domain.fuentes;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.stream.Collectors;

public class Conexion {
  private final ObjectMapper objectMapper = new ObjectMapper();

  public Map<String, Object> siguienteHecho(URL url, LocalDateTime fechaUltimaConsulta) {
    try {
      String fechaParam = fechaUltimaConsulta != null ? fechaUltimaConsulta.toString() : "";
      String urlConParams = url.toString() + "?fechaUltimaConsulta=" + fechaParam;

      HttpURLConnection conn = (HttpURLConnection) new URL(urlConParams).openConnection();
      conn.setRequestMethod("GET");

      if (conn.getResponseCode() == 204) {
        return null; // No hay hechos
      }

      if (conn.getResponseCode() != 200) {
        throw new RuntimeException("Error en la consulta: " + conn.getResponseCode());
      }

      BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
      String json = in.lines().collect(Collectors.joining());
      in.close();

      return objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {});
    } catch (Exception e) {
      throw new RuntimeException("Fallo la conexión: " + e.getMessage(), e);
    }
  }
}

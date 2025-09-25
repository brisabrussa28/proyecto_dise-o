package ar.edu.utn.frba.dds.domain.fuentes.apis.conexion;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.stream.Collectors;

public class Conexion {

  private final ObjectMapper objectMapper = new ObjectMapper();


  protected HttpURLConnection createConnection(URL url) throws IOException {
    return (HttpURLConnection) url.openConnection();
  }

  public Map<String, Object> siguienteHecho(URL url, LocalDateTime fechaUltimaConsulta) {
    try {
      String fechaParam = fechaUltimaConsulta != null ? fechaUltimaConsulta.toString() : "";
      URL urlConParams = new URL(url.toString() + "?fechaUltimaConsulta=" + fechaParam);

      HttpURLConnection conn = createConnection(urlConParams);
      conn.setRequestMethod("GET");

      if (conn.getResponseCode() == 204) {
        return null; // No hay hechos
      }

      if (conn.getResponseCode() != 200) {
        throw new RuntimeException("Error en la consulta: " + conn.getResponseCode());
      }

      BufferedReader in = new BufferedReader(
          new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8)
      );
      String json = in.lines().collect(Collectors.joining());
      in.close();

      return objectMapper.readValue(json, new TypeReference<>() {
      });
    } catch (Exception e) {
      throw new RuntimeException("Fallo la conexión: " + e.getMessage(), e);
    }
  }
}
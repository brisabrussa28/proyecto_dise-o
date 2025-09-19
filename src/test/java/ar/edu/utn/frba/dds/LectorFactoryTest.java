package ar.edu.utn.frba.dds;

import ar.edu.utn.frba.dds.domain.hecho.Hecho;
import ar.edu.utn.frba.dds.domain.lector.Lector;
import ar.edu.utn.frba.dds.domain.lector.LectorFactory;
import ar.edu.utn.frba.dds.domain.lector.csv.LectorCSV;
import ar.edu.utn.frba.dds.domain.lector.json.LectorJson;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;


public class LectorFactoryTest {

  private LectorFactory factory;
  private ObjectMapper objectMapper;

  @BeforeEach
  void setUp() {
    factory = new LectorFactory();
    objectMapper = new ObjectMapper();
  }

  private JsonNode getJsonNodeFromString(String json) throws IOException {
    return objectMapper.readTree(json);
  }

  @Test
  @DisplayName("Crea un LectorJson para la clase Hecho")
  void testCrearLectorJsonParaHecho() throws IOException {
    String jsonConfig = "{\"formato\": \"JSON\"}";
    JsonNode configNode = getJsonNodeFromString(jsonConfig);
    Lector<Hecho> lector = factory.create(configNode, Hecho.class);
    assertNotNull(lector);
    assertInstanceOf(LectorJson.class, lector);
  }

  @Test
  @DisplayName("Crea un LectorCSV para la clase Hecho")
  void testCrearLectorCsvParaHecho() throws IOException {
    // Se corrigen las claves para que coincidan con el enum CampoHecho.java
    String jsonConfig = "{ " +
        "\"formato\": \"CSV\", " +
        "\"separador\": \";\", " +
        "\"formatoFecha\": \"dd/MM/yyyy\", " +
        "\"mapeoColumnas\": { " +
        "  \"TITULO\": \"titulo_del_hecho\", " +
        "  \"FECHA_SUCESO\": [\"fecha_medicion\", \"hora_medicion\"], " +
        "  \"CATEGORIA\": \"categoria\" " +
        "}" +
        "}";
    JsonNode configNode = getJsonNodeFromString(jsonConfig);
    Lector<Hecho> lector = factory.create(configNode, Hecho.class);
    assertNotNull(lector);
    assertInstanceOf(LectorCSV.class, lector);
  }


  @Test
  @DisplayName("Lanza excepción para formato no soportado")
  void testFormatoNoSoportado() throws IOException {
    String jsonConfig = "{\"formato\": \"XML\"}";
    JsonNode configNode = getJsonNodeFromString(jsonConfig);
    Exception exception = assertThrows(IllegalArgumentException.class, () -> {
      factory.create(configNode, Hecho.class);
    });
    assertEquals("Formato de importador no soportado: XML", exception.getMessage());
  }

  @Test
  @DisplayName("Lanza excepción para clase no soportada en JSON")
  void testClaseNoSoportadaJson() throws IOException {
    String jsonConfig = "{\"formato\": \"JSON\"}";
    JsonNode configNode = getJsonNodeFromString(jsonConfig);
    Exception exception = assertThrows(IllegalArgumentException.class, () -> {
      factory.create(configNode, String.class);
    });
    assertEquals("No hay un TypeReference definido para la clase: java.lang.String", exception.getMessage());
  }

  @Test
  @DisplayName("Lanza excepción para clase no soportada en CSV")
  void testClaseNoSoportadaCsv() throws IOException {
    String jsonConfig = "{\"formato\": \"CSV\"}";
    JsonNode configNode = getJsonNodeFromString(jsonConfig);
    Exception exception = assertThrows(IllegalArgumentException.class, () -> {
      factory.create(configNode, String.class);
    });
    assertEquals("No hay un FilaConverter definido para la clase: java.lang.String", exception.getMessage());
  }

  @Test
  @DisplayName("Lanza excepción si falta el mapeo de columnas para CSV")
  void testFaltaMapeoColumnasCsv() throws IOException {
    String jsonConfig = "{\"formato\": \"CSV\", \"separador\": \";\"}";
    JsonNode configNode = getJsonNodeFromString(jsonConfig);
    Exception exception = assertThrows(IllegalArgumentException.class, () -> {
      factory.create(configNode, Hecho.class);
    });
    assertEquals("La configuración 'mapeoColumnas' es requerida para el importador CSV de Hecho.", exception.getMessage());
  }
}


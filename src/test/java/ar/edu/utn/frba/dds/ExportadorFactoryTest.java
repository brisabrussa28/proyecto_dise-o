package ar.edu.utn.frba.dds;

import ar.edu.utn.frba.dds.domain.exportador.Exportador;
import ar.edu.utn.frba.dds.domain.exportador.ExportadorFactory;
import ar.edu.utn.frba.dds.domain.exportador.csv.ExportadorCSV;
import ar.edu.utn.frba.dds.domain.exportador.json.ExportadorJson;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

public class ExportadorFactoryTest {

  private ExportadorFactory factory;
  private ObjectMapper objectMapper;

  @BeforeEach
  void setUp() {
    factory = new ExportadorFactory();
    objectMapper = new ObjectMapper();
  }

  private JsonNode getJsonNodeFromString(String json) throws IOException {
    return objectMapper.readTree(json);
  }

  @Test
  @DisplayName("Crea un ExportadorJson correctamente")
  void testCrearExportadorJson() throws IOException {
    String jsonConfig = "{\"formato\": \"JSON\"}";
    JsonNode configNode = getJsonNodeFromString(jsonConfig);
    Exportador<?> exportador = factory.create(configNode);
    assertNotNull(exportador);
    assertInstanceOf(ExportadorJson.class, exportador);
  }

  @Test
  @DisplayName("Crea un ExportadorCSV con modo Sobrescribir por defecto")
  void testCrearExportadorCsvDefault() throws IOException {
    String jsonConfig = "{\"formato\": \"CSV\"}";
    JsonNode configNode = getJsonNodeFromString(jsonConfig);
    Exportador<?> exportador = factory.create(configNode);
    assertNotNull(exportador);
    assertInstanceOf(ExportadorCSV.class, exportador);

    ExportadorCSV<?> csvExportador = (ExportadorCSV<?>) exportador;
    JsonNode config = objectMapper.readTree(csvExportador.getConfiguracionJson());
    assertEquals("SOBRESCRIBIR", config.get("modo").asText());
  }

  @Test
  @DisplayName("Crea un ExportadorCSV con modo Anexar")
  void testCrearExportadorCsvModoAnexar() throws IOException {
    String jsonConfig = "{\"formato\": \"CSV\", \"modo\": \"ANEXAR\"}";
    JsonNode configNode = getJsonNodeFromString(jsonConfig);
    Exportador<?> exportador = factory.create(configNode);
    assertInstanceOf(ExportadorCSV.class, exportador);

    ExportadorCSV<?> csvExportador = (ExportadorCSV<?>) exportador;
    JsonNode config = objectMapper.readTree(csvExportador.getConfiguracionJson());
    assertEquals("ANEXAR", config.get("modo").asText());
  }

  @Test
  @DisplayName("Crea un ExportadorCSV con modo Numerar")
  void testCrearExportadorCsvModoNumerar() throws IOException {
    String jsonConfig = "{\"formato\": \"CSV\", \"modo\": \"NUMERAR\"}";
    JsonNode configNode = getJsonNodeFromString(jsonConfig);
    Exportador<?> exportador = factory.create(configNode);
    assertInstanceOf(ExportadorCSV.class, exportador);

    ExportadorCSV<?> csvExportador = (ExportadorCSV<?>) exportador;
    JsonNode config = objectMapper.readTree(csvExportador.getConfiguracionJson());
    assertEquals("NUMERAR", config.get("modo").asText());
  }

  @Test
  @DisplayName("Crea un ExportadorCSV con modo Timestamp")
  void testCrearExportadorCsvModoTimestamp() throws IOException {
    String jsonConfig = "{\"formato\": \"CSV\", \"modo\": \"TIMESTAMP\"}";
    JsonNode configNode = getJsonNodeFromString(jsonConfig);
    Exportador<?> exportador = factory.create(configNode);
    assertInstanceOf(ExportadorCSV.class, exportador);

    ExportadorCSV<?> csvExportador = (ExportadorCSV<?>) exportador;
    JsonNode config = objectMapper.readTree(csvExportador.getConfiguracionJson());
    assertEquals("TIMESTAMP", config.get("modo").asText());
  }

  @Test
  @DisplayName("Lanza excepción para formato no soportado")
  void testFormatoNoSoportado() throws IOException {
    String jsonConfig = "{\"formato\": \"XML\"}";
    JsonNode configNode = getJsonNodeFromString(jsonConfig);
    Exception exception = assertThrows(IllegalArgumentException.class, () -> {
      factory.create(configNode);
    });
    assertEquals("Formato de exportador no soportado: XML", exception.getMessage());
  }

  @Test
  @DisplayName("Lanza excepción para modo de exportación no válido")
  void testModoExportacionNoValido() throws IOException {
    String jsonConfig = "{\"formato\": \"CSV\", \"modo\": \"INVALIDO\"}";
    JsonNode configNode = getJsonNodeFromString(jsonConfig);
    Exception exception = assertThrows(IllegalArgumentException.class, () -> {
      factory.create(configNode);
    });
    assertEquals("Modo de exportación no válido: INVALIDO", exception.getMessage());
  }
}


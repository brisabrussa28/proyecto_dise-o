package ar.edu.utn.frba.dds.domain.serializadores.Lector.json;

import ar.edu.utn.frba.dds.domain.serializadores.Lector.Lector;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Lee un archivo JSON y lo deserializa en una lista de objetos genéricos.
 *
 * @param <T> El tipo de objeto a deserializar.
 */
public class LectorJson<T> implements Lector<T> {

  private static final Logger LOGGER = Logger.getLogger(LectorJson.class.getName());
  private final ObjectMapper objectMapper;
  private final TypeReference<List<T>> typeReference;

  /**
   * Constructor que requiere un TypeReference para manejar la deserialización de listas genéricas.
   *
   * @param typeReference El TypeReference que describe el tipo de la lista, por ejemplo, new TypeReference<List<Hecho>>() {}.
   */
  public LectorJson(TypeReference<List<T>> typeReference) {
    this.objectMapper = new ObjectMapper();
    this.objectMapper.registerModule(new JavaTimeModule());
    this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    this.typeReference = typeReference;
  }

  /**
   * Carga una lista de objetos desde un archivo JSON.
   *
   * @param jsonFilePath La ruta al archivo JSON.
   * @return Una lista de objetos de tipo T. Retorna una lista vacía si el archivo no existe o está vacío.
   */
  @Override
  public List<T> importar(String jsonFilePath) {
    File jsonFile = new File(jsonFilePath);

    if (!jsonFile.exists() || jsonFile.length() == 0) {
      LOGGER.warning("El archivo JSON en " + jsonFilePath + " no existe o está vacío. Se retorna una lista vacía.");
      return new ArrayList<>();
    }

    try {
      // El mét0do readValue utiliza el typeReference del objeto para deserializar correctamente
      // la lista con el tipo genérico T de la clase.
      List<T> objetosLeidos = objectMapper.readValue(jsonFile, typeReference);
      LOGGER.info("Copia local de objetos cargada desde: " + jsonFilePath);
      return objetosLeidos != null ? objetosLeidos : new ArrayList<>();
    } catch (MismatchedInputException e) {
      LOGGER.log(Level.WARNING, "Error de sintaxis JSON o de tipo al cargar la copia " + jsonFilePath, e);
      return new ArrayList<>();
    } catch (IOException e) {
      LOGGER.log(Level.SEVERE, "Error de I/O al cargar la copia JSON " + jsonFilePath, e);
      // En un caso real, se podría relanzar una excepción personalizada.
      return new ArrayList<>();
    }
  }

  /**
   * Devuelve la configuración del lector en formato JSON.
   * @return Un string con la configuración en JSON.
   */
  @Override
  public String getConfiguracionJson() {
    ObjectNode configNode = objectMapper.createObjectNode();
    configNode.put("formato", "JSON");
    try {
      return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(configNode);
    } catch (JsonProcessingException e) {
      LOGGER.log(Level.SEVERE, "Error al generar la configuración JSON para LectorJson", e);
      return "{\"error\":\"No se pudo generar la configuración\"}";
    }
  }
}

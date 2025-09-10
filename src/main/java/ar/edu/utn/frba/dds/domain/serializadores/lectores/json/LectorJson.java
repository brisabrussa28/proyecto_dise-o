package ar.edu.utn.frba.dds.domain.serializadores.lectores.json;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LectorJson {

  private static final Logger LOGGER = Logger.getLogger(LectorJson.class.getName());
  private final ObjectMapper objectMapper;

  public LectorJson() {
    this.objectMapper = new ObjectMapper();
    this.objectMapper.registerModule(new JavaTimeModule());
    this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
  }

  public <T> List<T> cargarCopiaLocalJson(String jsonFilePath, TypeReference<List<T>> typeReference) {
    File jsonFile = new File(jsonFilePath);

    if (!jsonFile.exists() || jsonFile.length() == 0) {
      LOGGER.warning("El archivo JSON en " + jsonFilePath + " no existe o está vacío. Se retorna una lista vacía.");
      return new ArrayList<>();
    }

    try {
      List<T> objetosLeidos = objectMapper.readValue(jsonFile, typeReference);
      LOGGER.info("Copia local de objetos cargada desde: " + jsonFilePath);
      return objetosLeidos != null ? objetosLeidos : new ArrayList<>();
    } catch (MismatchedInputException e) {
      LOGGER.log(Level.WARNING, "Error de sintaxis JSON o tipo al cargar la copia " + jsonFilePath, e);
      return new ArrayList<>();
    } catch (IOException e) {
      LOGGER.log(Level.SEVERE, "Error de I/O al cargar la copia JSON " + jsonFilePath, e);
      return new ArrayList<>();
    }
  }
}

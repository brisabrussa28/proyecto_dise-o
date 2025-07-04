package ar.edu.utn.frba.dds.domain.serviciodecopiaslocales;

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

public class ServicioDeCopiasLocales {

  private static final Logger LOGGER = Logger.getLogger(ServicioDeCopiasLocales.class.getName());
  private final String jsonFilePath;
  private final ObjectMapper objectMapper;

  public ServicioDeCopiasLocales(String jsonFilePath) {
    this.jsonFilePath = jsonFilePath;
    this.objectMapper = new ObjectMapper();
    this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
    this.objectMapper.registerModule(new JavaTimeModule());
    this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
  }

  public <T> void guardarCopiaLocalJson(List<T> objetos) {
    try {
      objectMapper.writeValue(new File(jsonFilePath), objetos);
      LOGGER.info("Copia local de objetos guardada en " + jsonFilePath);
    } catch (IOException e) {
      LOGGER.log(Level.SEVERE, "Error al escribir la copia JSON local en " + jsonFilePath, e);
    }
  }

  public <T> List<T> cargarCopiaLocalJson(TypeReference<List<T>> typeReference) {
    File jsonFile = new File(jsonFilePath);

    // El constructor ya asegura que el archivo existe, pero podría estar vacío.
    if (jsonFile.length() == 0) {
      LOGGER.warning("El archivo JSON en " + jsonFilePath + " está vacío. Se retorna una lista vacía.");
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

  public String getJsonFilePath() {
    return jsonFilePath;
  }
}
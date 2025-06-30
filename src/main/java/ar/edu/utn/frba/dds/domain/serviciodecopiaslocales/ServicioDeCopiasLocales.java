package ar.edu.utn.frba.dds.domain.serviciodecopiaslocales;

import ar.edu.utn.frba.dds.domain.hecho.Hecho;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Provides utilities to save and load lists of objects of any type to a local JSON file.
 * This class does not keep a copy of the objects in memory; it always operates directly
 * with the provided JSON file.
 */
public class ServicioDeCopiasLocales {

  private final String jsonFilePath;
  private final ObjectMapper objectMapper;

  /**
   * Constructor.
   *
   * @param jsonFilePath Path to the file where JSON copies will be saved and read.
   */
  public ServicioDeCopiasLocales(String jsonFilePath) {
    this.jsonFilePath = jsonFilePath;
    this.objectMapper = new ObjectMapper();
    this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
    this.objectMapper.registerModule(new JavaTimeModule());
    this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
  }

  /**
   * Saves a local copy of the list of objects of any type in JSON format.
   *
   * @param <T>     The type of objects in the list.
   * @param objetos The list of objects to save.
   */
  public <T> void guardarCopiaLocalJson(List<T> objetos) {
    try {
      objectMapper.writeValue(new File(jsonFilePath), objetos);
      System.out.println("ServicioDeCopiasLocales: Copia local de objetos guardada en: " + jsonFilePath);
    } catch (IOException e) {
      System.err.println("ServicioDeCopiasLocales: Error al escribir la copia JSON local en " + jsonFilePath + ": " + e.getMessage());
    }
  }

  /**
   * Reads the local JSON file and converts its content into a list of objects of any type.
   *
   * @param <T>          The type of objects in the list.
   * @param typeReference A TypeReference specifying the full type of the list to deserialize.
   * @return A list of objects read from the JSON file, or an empty list if the file does not exist,
   *         is empty, or if a read/syntax error occurs.
   */
  public <T> List<T> cargarCopiaLocalJson(TypeReference<List<T>> typeReference) {
    File jsonFile = new File(jsonFilePath);
    if (!jsonFile.exists() || jsonFile.length() == 0) {
      System.out.println("Archivo JSON no encontrado o vacío en " + jsonFilePath);
      return new ArrayList<>();
    }
    try {
      List<T> objetosLeidos = objectMapper.readValue(jsonFile, typeReference);
      System.out.println("Copia local de objetos cargada desde: " + jsonFilePath);
      return objetosLeidos != null ? objetosLeidos : new ArrayList<>();
    } catch (MismatchedInputException e) {
      System.err.println("Error de sintaxis JSON o tipo al cargar la copia " + jsonFilePath);
      return new ArrayList<>();
    } catch (IOException e) {
      System.err.println("Error de I/O al cargar la copia JSON " + jsonFilePath);
      return new ArrayList<>();
    }
  }

  /**
   * Saves a local copy of a list of Hecho objects in JSON format.
   *
   * @param hechos The list of Hecho objects to save.
   */
  public void guardarCopiaHechos(List<Hecho> hechos) {
    guardarCopiaLocalJson(hechos);
  }

  /**
   * Reads the local JSON file and converts its content into a list of Hecho objects.
   *
   * @return A list of Hecho objects read from the JSON file, or an empty list if the file does not exist,
   *         is empty, or if a read/syntax error occurs.
   */
  public List<Hecho> cargarCopiaHechos() {
    return cargarCopiaLocalJson(new TypeReference<List<Hecho>>() {});
  }

  /**
   * Gets the path of the JSON file used by this service.
   *
   * @return The path of the JSON file.
   */
  public String getJsonFilePath() {
    return jsonFilePath;
  }
}
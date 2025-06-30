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
 * Provee utilidades para guardar y cargar listas de objetos de cualquier tipo en un archivo JSON local.
 * Esta clase no mantiene una copia de los objetos en memoria; siempre opera directamente
 * con el archivo JSON provisto.
 */
public class ServicioDeCopiasLocales {

  private final String jsonFilePath;
  private final ObjectMapper objectMapper;

  /**
   * Constructor.
   *
   * @param jsonFilePath Ruta al archivo donde se guardarán y leerán las copias JSON.
   */
  public ServicioDeCopiasLocales(String jsonFilePath) {
    this.jsonFilePath = jsonFilePath;
    this.objectMapper = new ObjectMapper();
    this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
    this.objectMapper.registerModule(new JavaTimeModule());
    this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
  }

  /**
   * Guarda una copia local de la lista de objetos de cualquier tipo en formato JSON.
   *
   * @param <T>     Tipo de los objetos en la lista.
   * @param objetos Lista de objetos a guardar.
   */
  public <T> void guardarCopiaLocalJson(List<T> objetos) {
    try {
      objectMapper.writeValue(new File(jsonFilePath), objetos);
      System.out.println("Copia local de objetos guardada en " + jsonFilePath);
    } catch (IOException e) {
      System.err.println("Error al escribir la copia JSON local en " + jsonFilePath);
    }
  }

  /**
   * Lee el archivo JSON local y convierte su contenido en una lista de objetos de cualquier tipo.
   *
   * @param <T>           Tipo de los objetos en la lista.
   * @param typeReference Referencia de tipo que especifica el tipo completo de la lista a deserializar.
   * @return Una lista de objetos leídos del archivo JSON, o una lista vacía si el archivo no existe,
   *         está vacío o si ocurre un error de lectura/sintaxis.
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
   * Guarda una copia local de una lista de objetos Hecho en formato JSON.
   *
   * @param hechos Lista de objetos Hecho a guardar.
   */
  public void guardarCopiaHechos(List<Hecho> hechos) {
    guardarCopiaLocalJson(hechos);
  }

  /**
   * Lee el archivo JSON local y convierte su contenido en una lista de objetos Hecho.
   *
   * @return Una lista de objetos Hecho leídos del archivo JSON, o una lista vacía si el archivo no existe,
   *         está vacío o si ocurre un error de lectura/sintaxis.
   */
  public List<Hecho> cargarCopiaHechos() {
    return cargarCopiaLocalJson(new TypeReference<List<Hecho>>() {});
  }

  /**
   * Obtiene la ruta del archivo JSON utilizado por este servicio.
   *
   * @return La ruta del archivo JSON.
   */
  public String getJsonFilePath() {
    return jsonFilePath;
  }
}
package ar.edu.utn.frba.dds.domain.serviciodecopiaslocales;

import ar.edu.utn.frba.dds.domain.hecho.Hecho; // Ahora necesario porque los nuevos métodos son específicos de Hecho

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.core.type.TypeReference; // Necesario para deserializar Listas de tipos genéricos
import com.fasterxml.jackson.databind.exc.MismatchedInputException; // Para manejar errores de sintaxis JSON
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule; // Importar el módulo para java.time

import java.io.File; // Usado para operaciones de archivo
import java.io.IOException;
import java.io.FileNotFoundException; // Para manejar errores de archivo no encontrado
import java.util.ArrayList;
import java.util.List;

/**
 * ServicioDeCopiasLocales proporciona utilidades para guardar y cargar
 * listas de objetos de cualquier tipo en un archivo JSON local.
 * Esta clase no mantiene una copia de los objetos en memoria; siempre opera
 * directamente con el archivo JSON proporcionado.
 */
public class ServicioDeCopiasLocales {

  private final String jsonFilePath;
  private final ObjectMapper objectMapper; // ObjectMapper de Jackson

  /**
   * Constructor para ServicioDeCopiasLocales.
   *
   * @param jsonFilePath Ruta del archivo donde se guardarán y leerán las copias JSON.
   */
  public ServicioDeCopiasLocales(String jsonFilePath) {
    this.jsonFilePath = jsonFilePath;
    this.objectMapper = new ObjectMapper();
    // Habilitar el "pretty printing" para que el archivo JSON sea legible
    this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
    // Configurar ObjectMapper para manejar LocalDateTime y otras clases de java.time
    this.objectMapper.registerModule(new JavaTimeModule());
    // Deshabilitar la escritura de fechas como timestamps (útil para LocalDateTime)
    this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
  }

  /**
   * Guarda una copia local de la lista de objetos de cualquier tipo en formato JSON.
   *
   * @param <T> El tipo de objetos que contendrá la lista.
   * @param objetos La lista de objetos de tipo T a guardar.
   */
  public <T> void guardarCopiaLocalJson(List<T> objetos) { // Método genérico (por si en un futuro se quiere guardar otro tipo de objetos)
    try {
      // Escribir la lista de objetos en el archivo JSON especificado
      objectMapper.writeValue(new File(jsonFilePath), objetos);
      System.out.println("ServicioDeCopiasLocales: Copia local de objetos guardada en: " + jsonFilePath);
    } catch (IOException e) {
      System.err.println("ServicioDeCopiasLocales: Error al escribir la copia JSON local en " + jsonFilePath + ": " + e.getMessage());
      // En un entorno de producción, considera lanzar una excepción personalizada
      // o un mecanismo de registro de errores más sofisticado.
    }
  }

  /**
   * Lee el archivo JSON local y convierte su contenido en una lista de objetos de cualquier tipo.
   *
   * @param <T> El tipo de objetos que contendrá la lista.
   * @param typeReference Un TypeReference que especifica el tipo completo de la lista a deserializar
   * (e.g., new TypeReference<List<Hecho>>() {}). Esto es necesario para
   * manejar correctamente los tipos genéricos en tiempo de ejecución.
   * @return Una lista de objetos de tipo T leídos del archivo JSON, o una lista vacía si
   * el archivo no existe, está vacío o si ocurre un error de lectura/sintaxis.
   */
  public <T> List<T> cargarCopiaLocalJson(TypeReference<List<T>> typeReference) {
    File jsonFile = new File(jsonFilePath);
    // Verificar si el archivo existe antes de intentar leer
    if (!jsonFile.exists() || jsonFile.length() == 0) {
      System.out.println("ServicioDeCopiasLocales: Archivo JSON no encontrado o vacío en " + jsonFilePath + ". Devolviendo una lista vacía.");
      return new ArrayList<>(); // Archivo no encontrado o vacío, devolver una lista vacía
    }

    try {
      // Leer y convertir el contenido JSON a una lista de tipo T usando TypeReference
      List<T> objetosLeidos = objectMapper.readValue(jsonFile, typeReference);
      System.out.println("ServicioDeCopiasLocales: Copia local de objetos cargada desde: " + jsonFilePath);
      // Devolver la lista leída; si el archivo estaba mal formado pero no vacío, podría ser null
      return objetosLeidos != null ? objetosLeidos : new ArrayList<>(); // Asegurarse de devolver siempre una lista no nula
    } catch (FileNotFoundException e) {
      // Este caso debería ser capturado por la comprobación inicial de file.exists(), pero se mantiene por robustez
      System.err.println("ServicioDeCopiasLocales: El archivo JSON no se encontró en " + jsonFilePath + ". Devolviendo una lista vacía.");
      return new ArrayList<>();
    } catch (MismatchedInputException e) {
      System.err.println("ServicioDeCopiasLocales: Error de sintaxis JSON o de tipo al cargar la copia desde " + jsonFilePath + ": " + e.getMessage());
      return new ArrayList<>(); // El contenido del archivo no es un JSON válido o no coincide con la estructura de T
    } catch (IOException e) {
      System.err.println("ServicioDeCopiasLocales: Error de I/O al cargar la copia JSON desde " + jsonFilePath + ": " + e.getMessage());
      return new ArrayList<>(); // Error general de I/O
    }
  }

  /**
   * Guarda una copia local de una lista de objetos Hecho en formato JSON.
   * Este es un método de conveniencia para guardar específicamente Hechos.
   *
   * @param hechos La lista de objetos Hecho a guardar.
   */
  public void guardarCopiaHechos(List<Hecho> hechos) {
    // Reutiliza el método genérico para guardar la lista de Hechos
    // No se necesita casting aquí porque el método genérico acepta List<T> y List<Hecho> es compatible
    guardarCopiaLocalJson(hechos);
  }

  /**
   * Lee el archivo JSON local y convierte su contenido en una lista de objetos Hecho.
   * Este es un método de conveniencia para cargar específicamente Hechos.
   *
   * @return Una lista de objetos Hecho leídos del archivo JSON, o una lista vacía si
   * el archivo no existe, está vacío o si ocurre un error de lectura/sintaxis.
   */
  public List<Hecho> cargarCopiaHechos() {
    // Reutiliza el método genérico para cargar la lista de Hechos
    // Se necesita un TypeReference específico para List<Hecho>
    // No se necesita casting aquí porque el método genérico devuelve List<T> y se especifica List<Hecho>
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

package ar.edu.utn.frba.dds.domain.filtro.condiciones;

import ar.edu.utn.frba.dds.domain.filtro.condiciones.condicion.Condicion;
import ar.edu.utn.frba.dds.domain.filtro.condiciones.condicion.CondicionAnd;
import ar.edu.utn.frba.dds.domain.filtro.condiciones.condicion.CondicionCompuesta;
import ar.edu.utn.frba.dds.domain.filtro.condiciones.condicion.CondicionGenerica;
import ar.edu.utn.frba.dds.domain.filtro.condiciones.condicion.CondicionNot;
import ar.edu.utn.frba.dds.domain.filtro.condiciones.condicion.CondicionOr;
import ar.edu.utn.frba.dds.domain.hecho.Estado;
import ar.edu.utn.frba.dds.domain.info.PuntoGeografico;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * Factory para crear objetos Condicion a partir de un String JSON.
 */
public class CondicionFactory {

  private final Gson gson;

  public CondicionFactory() {
    this.gson = new GsonBuilder()
        .registerTypeAdapter(
            LocalDateTime.class,
            (JsonDeserializer<LocalDateTime>) (json, type, jsonDeserializationContext) ->
                LocalDateTime.parse(
                    json.getAsJsonPrimitive()
                        .getAsString(), DateTimeFormatter.ISO_LOCAL_DATE_TIME
                )
        )
        .create();
  }

  /**
   * Recibe un String JSON y lo convierte en una Condicion.
   *
   * @param jsonString El JSON del tipo de condicion.
   * @return Una instancia de Condicion.
   */
  public Condicion crearCondicionDesdeJson(String jsonString) {
    // 1. Transforma al JSON en un map.
    Map<String, Object> metadata = gson.fromJson(jsonString, Map.class);
    // 2. Procesa el map para construir el objeto Condicion.
    return crearCondicion(metadata);
  }

  /**
   * Mét0do recursivo que interpreta los metadatos y construye los flujos segun corresponda.
   *
   * @param metadata Un mapa que representa a la condición.
   * @return Una instancia de Condicion.
   */
  private Condicion crearCondicion(Map<String, Object> metadata) {

    if (metadata == null || metadata.isEmpty()) {
      return crearCondicionTrue();
    } else if (metadata.containsKey("compuesta")) { //and or
      return crearCondicionCompuesta(metadata);
    } else if (metadata.containsKey("logica")) { // solo not de momento
      return crearCondicionLogica(metadata);
    } else if (metadata.containsKey("campo")) {
      return crearCondicionSimple(metadata); // condicion normal igual mayor menor etc
    } else {
      throw new IllegalArgumentException(
          "La metadata del filtro es inválida. Debe contener 'logica' o 'campo'.");
    }
  }

  private Condicion crearCondicionTrue() {
    return new Condicion();
  }


  private CondicionCompuesta crearCondicionCompuesta(Map<String, Object> metadata) {
    String logica = (String) metadata.get("compuesta");
    List<Map<String, Object>> subCondicionesMeta = (List<Map<String, Object>>) metadata.get(
        "condiciones");

    CondicionCompuesta compuesta;
    switch (logica.toUpperCase()) {
      case "AND":
        compuesta = new CondicionAnd();
        break;
      case "OR":
        compuesta = new CondicionOr();
        break;

      default:
        throw new IllegalArgumentException("Lógica no soportada: " + logica);
    }

    // Recursivamente crea y agrega cada sub-condición.
    for (Map<String, Object> subMeta : subCondicionesMeta) {
      compuesta.agregarCondicion(crearCondicion(subMeta));
    }
    return compuesta;
  }

  private CondicionNot crearCondicionLogica(Map<String, Object> metadata) {
    String logica = (String) metadata.get("logica");
    if (!"NOT".equalsIgnoreCase(logica)) {
      throw new IllegalArgumentException("Lógica no soportada para condición lógica: " + logica);
    }

    Map<String, Object> subMeta = (Map<String, Object>) metadata.get("condicion");
    Condicion subCondicion = crearCondicion(subMeta);

    CondicionNot condicionNot = new CondicionNot();
    condicionNot.setCondicion(subCondicion);
    return condicionNot;
  }

  private CondicionGenerica crearCondicionSimple(Map<String, Object> metadata) {
    String campo = (String) metadata.get("campo");
    String operador = (String) metadata.get("operador");
    Object valor = parsearValor(campo, metadata.get("valor"));

    return new CondicionGenerica(campo, operador, valor);
  }

  /**
   * Convierte el valor del JSON al tipo de dato correcto según el campo del Hecho.
   * Esto es crucial para que la comparación de tipos (ej: fechas) funcione correctamente.
   */
  private Object parsearValor(String campo, Object valorJson) {
    if (valorJson == null) {
      return null;
    }

    String valorStr = valorJson.toString();

    return switch (campo.toLowerCase()) {
      case "estado" -> Estado.valueOf(valorStr.toUpperCase());
      case "origen" -> Estado.valueOf(valorStr.toUpperCase());
      case "fechacarga" -> LocalDateTime.parse(valorStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
      case "fechasuceso" -> LocalDateTime.parse(valorStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
      case "ubicacion" -> {
        if (valorJson instanceof Map) {
          Map<String, Object> ubicacionMap = (Map<String, Object>) valorJson;
          double latitud = Double.parseDouble(ubicacionMap.get("latitud")
                                                          .toString());
          double longitud = Double.parseDouble(ubicacionMap.get("longitud")
                                                           .toString());
          yield new PuntoGeografico(latitud, longitud);
        }
        throw new IllegalArgumentException(
            "El valor de 'ubicacion' debe ser un objeto con 'latitud' y 'longitud'");
      }
      case "etiquetas" -> throw new IllegalArgumentException("no implementado todavia jijo");
      default ->
          // Si el campo no tiene un parseo especial, se trata como String.
          valorStr;
    };
  }
}


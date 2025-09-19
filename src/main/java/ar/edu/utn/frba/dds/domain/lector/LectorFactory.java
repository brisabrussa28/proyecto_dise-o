package ar.edu.utn.frba.dds.domain.lector;

import ar.edu.utn.frba.dds.domain.hecho.CampoHecho;
import ar.edu.utn.frba.dds.domain.hecho.Hecho;
import ar.edu.utn.frba.dds.domain.lector.csv.LectorCSV;
import ar.edu.utn.frba.dds.domain.lector.csv.filaconverter.FilaConverter;
import ar.edu.utn.frba.dds.domain.lector.csv.filaconverter.HechoFilaConverter;
import ar.edu.utn.frba.dds.domain.lector.json.LectorJson;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Factory dedicada a la creación de instancias de Lector (Importador)
 * a partir de una configuración JSON.
 */
public class LectorFactory {

  /**
   * Crea un Lector genérico a partir de su nodo de configuración JSON.
   *
   * @param importadorNode El nodo JSON con la configuración del importador.
   * @param clazz          La clase del objeto a leer.
   * @return Una instancia de Lector configurada.
   */
  public <T> Lector<T> create(JsonNode importadorNode, Class<T> clazz) {
    String formato = importadorNode.path("formato")
                                   .asText()
                                   .toUpperCase();
    switch (formato) {
      case "JSON":
        if (clazz.equals(Hecho.class)) {
          // El cast es seguro porque está dentro de la validación de la clase
          @SuppressWarnings("unchecked")
          Lector<T> lectorJson = (Lector<T>) new LectorJson<>(new TypeReference<List<Hecho>>() {
          });
          return lectorJson;
        }
        throw new IllegalArgumentException("No hay un TypeReference definido para la clase: " + clazz.getName());

      case "CSV":
        char separador = importadorNode.path("separador")
                                       .asText(",")
                                       .charAt(0);
        FilaConverter<T> converter = createFilaConverter(importadorNode, clazz);
        return new LectorCSV<>(separador, converter);

      default:
        throw new IllegalArgumentException("Formato de importador no soportado: " + formato);
    }
  }

  /**
   * Factory interna para crear el FilaConverter apropiado para la clase dada.
   */
  private <T> FilaConverter<T> createFilaConverter(JsonNode importadorNode, Class<T> clazz) {
    if (clazz.equals(Hecho.class)) {
      String dateFormat = importadorNode.path("formatoFecha")
                                        .asText("yyyy-MM-dd HH:mm:ss");
      JsonNode mapeoNode = importadorNode.path("mapeoColumnas");

      if (mapeoNode.isMissingNode() || !mapeoNode.isObject()) {
        throw new IllegalArgumentException(
            "La configuración 'mapeoColumnas' es requerida para el importador CSV de Hecho.");
      }

      Map<CampoHecho, List<String>> mapeoColumnas = parseMapeoColumnasHecho(mapeoNode);

      // El cast es seguro porque está dentro de la validación de la clase
      @SuppressWarnings("unchecked")
      FilaConverter<T> converter = (FilaConverter<T>) new HechoFilaConverter(
          dateFormat,
          mapeoColumnas
      );
      return converter;
    }
    throw new IllegalArgumentException("No hay un FilaConverter definido para la clase: " + clazz.getName());
  }

  /**
   * Parsea el nodo JSON 'mapeoColumnas' para un objeto Hecho.
   */
  private Map<CampoHecho, List<String>> parseMapeoColumnasHecho(JsonNode mapeoNode) {
    Map<CampoHecho, List<String>> mapeo = new HashMap<>();
    Iterator<Map.Entry<String, JsonNode>> fields = mapeoNode.fields();

    while (fields.hasNext()) {
      Map.Entry<String, JsonNode> field = fields.next();
      try {
        CampoHecho campo = CampoHecho.valueOf(field.getKey()
                                                   .toUpperCase());
        List<String> columnas = new ArrayList<>();
        if (field.getValue()
                 .isArray()) {
          ArrayNode arrayNode = (ArrayNode) field.getValue();
          arrayNode.forEach(node -> columnas.add(node.asText()));
        } else {
          columnas.add(field.getValue()
                            .asText());
        }
        mapeo.put(campo, columnas);
      } catch (IllegalArgumentException e) {
        throw new IllegalArgumentException(
            "La clave '" + field.getKey() + "' en 'mapeoColumnas' no corresponde a un CampoHecho válido.", e);
      }
    }
    return mapeo;
  }
}

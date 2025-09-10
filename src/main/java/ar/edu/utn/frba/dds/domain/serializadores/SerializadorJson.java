package ar.edu.utn.frba.dds.domain.serializadores;

import ar.edu.utn.frba.dds.domain.serializadores.json.Exportador.ExportadorJson;
import ar.edu.utn.frba.dds.domain.serializadores.json.Lector.LectorJson;

import java.util.List;

/**
 * Implementación de la interfaz Serializador para el formato JSON.
 * Utiliza LectorJson para cargar y ExportadorJson para exportar.
 *
 * @param <T> El tipo de objeto a serializar.
 */
public class SerializadorJson<T> implements Serializador<T> {

  private final LectorJson<T> lector;
  private final ExportadorJson exportador;

  /**
   * Constructor que recibe las dependencias para leer y escribir JSON.
   * @param lector La instancia de LectorJson ya configurada para el tipo T.
   * @param exportador La instancia del ExportadorJson.
   */
  public SerializadorJson(LectorJson<T> lector, ExportadorJson exportador) {
    this.lector = lector;
    this.exportador = exportador;
  }

  @Override
  public List<T> importar(String path) {
    // La responsabilidad de conocer el tipo de la lista ahora recae en el LectorJson
    return this.lector.cargarCopiaLocalJson(path);
  }

  @Override
  public void exportar(List<T> objetos, String path) {
    this.exportador.guardarCopiaLocalJson(path, objetos);
  }
}

package ar.edu.utn.frba.dds.domain.serializadores;

import java.util.List;

/**
 * Interfaz genérica para serializar y deserializar listas de objetos.
 * Define un contrato para importar y exportar datos desde/hacia un origen de datos.
 *
 * @param <T> El tipo de objeto a serializar.
 */
public interface Serializador<T> {

  /**
   * Importa (deserializa) una lista de objetos desde una ruta de archivo.
   *
   * @param path La ruta del archivo a cargar.
   * @return Una lista de objetos de tipo T.
   */
  List<T> importar(String path);

  /**
   * Exporta (serializa) una lista de objetos a una ruta de archivo.
   *
   * @param objetos La lista de objetos a exportar.
   * @param path    La ruta del archivo donde se guardarán los objetos.
   */
  void exportar(List<T> objetos, String path);
}

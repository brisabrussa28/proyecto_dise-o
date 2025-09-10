package ar.edu.utn.frba.dds.domain.serializadores;

import ar.edu.utn.frba.dds.domain.serializadores.csv.Exportador.ExportadorCSV;
import ar.edu.utn.frba.dds.domain.serializadores.csv.Lector.LectorCSV;
import java.util.List;

/**
 * Implementación de la interfaz Serializador para el formato CSV.
 * Utiliza LectorCSV para cargar y ExportadorCSV para exportar.
 *
 * @param <T> El tipo de objeto a serializar.
 */
public class SerializadorCSV<T> implements Serializador<T> {

  private final LectorCSV<T> lector;
  private final ExportadorCSV<T> exportador;

  public SerializadorCSV(LectorCSV<T> lector, ExportadorCSV<T> exportador) {
    this.lector = lector;
    this.exportador = exportador;
  }

  @Override
  public List<T> importar(String path) {
    return this.lector.importar(path);
  }

  @Override
  public void exportar(List<T> objetos, String path) {
    this.exportador.exportar(objetos, path);
  }
}

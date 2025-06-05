package ar.edu.utn.frba.dds.domain.fuentes;

import ar.edu.utn.frba.dds.domain.csv.LectorCSV;
import ar.edu.utn.frba.dds.domain.hecho.CampoHecho;
import ar.edu.utn.frba.dds.domain.hecho.Hecho;
import java.util.List;
import java.util.Map;
import jdk.jshell.execution.LocalExecutionControl;

/**
 * Fuente de datos estática basada en archivo CSV.
 */
public class FuenteEstatica extends Fuente {

  private final String rutaCsv;
  private LectorCSV lectorCSV;

  /**
   * Constructor de la fuente estática.
   *
   * @param nombre       Nombre de la fuente estática.
   * @param rutaCsv      Ruta del archivo CSV que contiene los datos.
   * @param lectorCSV    Carácter separador utilizado en el CSV.
   */
  public FuenteEstatica(
      String nombre,
      String rutaCsv,
      LectorCSV lectorCSV
  ) {
    super(nombre); // no carga los hechos en el constructor
    this.rutaCsv = rutaCsv;
    this.lectorCSV = lectorCSV;
  }

  /**
   * Obtiene los hechos de la fuente estática.
   *
   * @return Lista de hechos importados desde el archivo CSV.
   */
  @Override
  public List<Hecho> obtenerHechos() {
    return lectorCSV.importar(rutaCsv);
  }
}

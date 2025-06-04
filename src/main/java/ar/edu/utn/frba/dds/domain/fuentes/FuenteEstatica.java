package ar.edu.utn.frba.dds.domain.fuentes;

import ar.edu.utn.frba.dds.domain.csv.LectorCSV;
import ar.edu.utn.frba.dds.domain.hecho.CampoHecho;
import ar.edu.utn.frba.dds.domain.hecho.Hecho;
import java.util.List;
import java.util.Map;

/**
 * Fuente de datos estática basada en archivo CSV.
 */
public class FuenteEstatica extends Fuente {

  private final String rutaCsv;
  private final char separador;
  private final String formatoFecha;
  private final Map<CampoHecho, List<String>> mapeo;

  /**
   * Constructor de la fuente estática.
   *
   * @param nombre       Nombre de la fuente estática.
   * @param rutaCsv      Ruta del archivo CSV que contiene los datos.
   * @param separador    Carácter separador utilizado en el CSV.
   * @param formatoFecha Formato de fecha para los campos de fecha en el CSV.
   * @param mapeo        Mapeo de campos del hecho a columnas del CSV.
   */
  public FuenteEstatica(
      String nombre,
      String rutaCsv,
      char separador,
      String formatoFecha,
      Map<CampoHecho, List<String>> mapeo
  ) {
    super(nombre); // no carga los hechos en el constructor
    this.rutaCsv = rutaCsv;
    this.separador = separador;
    this.formatoFecha = formatoFecha;
    this.mapeo = mapeo;
  }

  /**
   * Obtiene los hechos de la fuente estática.
   *
   * @return Lista de hechos importados desde el archivo CSV.
   */
  @Override
  public List<Hecho> obtenerHechos() {
    return new LectorCSV().importar(rutaCsv, separador, formatoFecha, mapeo);
  }
}

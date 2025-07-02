package ar.edu.utn.frba.dds.domain.fuentes;

import ar.edu.utn.frba.dds.domain.csv.LectorCSV;
import ar.edu.utn.frba.dds.domain.hecho.Hecho;
import java.util.List;

/**
 * Fuente de datos estática basada en archivo CSV.
 */
public class FuenteEstatica implements Fuente {

  private final String rutaCsv;
  private final LectorCSV lectorCSV;
  private final String nombre;

  /**
   * Constructor de la fuente estática.
   *
   * @param nombre    Nombre de la fuente estática.
   * @param rutaCsv   Ruta del archivo CSV que contiene los datos.
   * @param lectorCSV Carácter separador utilizado en el CSV.
   */
  public FuenteEstatica(
      String nombre,
      String rutaCsv,
      LectorCSV lectorCSV
  ) {
    this.validarFuente(nombre);
    this.nombre = nombre;// no carga los hechos en el constructor
    if (rutaCsv == null || lectorCSV == null) {
      throw new IllegalArgumentException("Ruta y lector deben estar definidos");
    }
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

  public String getNombre() { return nombre;}
}

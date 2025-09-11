package ar.edu.utn.frba.dds.domain.fuentes;

import ar.edu.utn.frba.dds.domain.hecho.Hecho;
import ar.edu.utn.frba.dds.domain.serializadores.Serializador;
import java.util.List;

/**
 * Fuente de datos estática que lee desde un archivo utilizando un Serializador.
 */
public class FuenteEstatica implements Fuente {

  private final String rutaArchivo;
  private final Serializador<Hecho> serializador;
  private final String nombre;

  /**
   * Constructor de la fuente estática.
   *
   * @param nombre      Nombre de la fuente.
   * @param rutaArchivo Ruta del archivo que contiene los datos (e.g., "datos.csv").
   * @param serializador Implementación de Serializador para leer los datos.
   */
  public FuenteEstatica(String nombre, String rutaArchivo, Serializador<Hecho> serializador) {
    this.validarFuente(nombre);
    this.nombre = nombre;
    if (rutaArchivo == null || serializador == null) {
      throw new IllegalArgumentException("La ruta del archivo y el serializador deben estar definidos.");
    }
    this.rutaArchivo = rutaArchivo;
    this.serializador = serializador;
  }

  /**
   * Obtiene los hechos de la fuente estática utilizando el serializador.
   *
   * @return Lista de hechos importados desde el archivo.
   */
  @Override
  public List<Hecho> obtenerHechos() {
    return serializador.importar(rutaArchivo);
  }

  public String getNombre() {
    return nombre;
  }
}
package ar.edu.utn.frba.dds.domain.fuentes;

import ar.edu.utn.frba.dds.domain.fuentes.apis.FuenteAdapter;
import ar.edu.utn.frba.dds.domain.hecho.Hecho;
import ar.edu.utn.frba.dds.domain.serializadores.Serializador;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Clase para fuentes de datos externas que consultan a través de una API.
 * Delega la responsabilidad de la consulta a un 'FuenteAdapter' inyectado
 * y utiliza un 'Serializador' para manejar la caché local.
 */
public class FuenteExternaAPI extends FuenteDeCopiaLocal {

  private static final Logger logger = Logger.getLogger(FuenteExternaAPI.class.getName());
  private final FuenteAdapter adaptador;

  /**
   * Constructor de la fuente externa.
   *
   * @param nombre       Nombre de la fuente.
   * @param adaptador    Adaptador específico para la API que se va a consumir.
   * @param rutaCopia    Ruta del archivo para la copia local (caché).
   * @param serializador Serializador para cargar y guardar la caché.
   */
  public FuenteExternaAPI(String nombre, FuenteAdapter adaptador, String rutaCopia, Serializador<Hecho> serializador) {
    super(nombre, rutaCopia, serializador);
    if (adaptador == null) {
      throw new IllegalArgumentException("El adaptador no puede ser nulo.");
    }
    this.adaptador = adaptador;
  }

  /**
   * Consulta los nuevos hechos a través del adaptador de la API.
   * Si la consulta falla, devuelve una lista vacía para evitar
   * corromper la caché con datos antiguos.
   *
   * @return Una lista de nuevos Hechos o una lista vacía en caso de error.
   */
  @Override
  protected List<Hecho> consultarNuevosHechos() {
    try {
      return adaptador.consultarHechos();
    } catch (Exception e) {
      logger.log(Level.SEVERE, "Error al consultar la fuente externa '" + this.getNombre() + "'", e);
      return Collections.emptyList(); // Devolver vacío para limpiar caché en caso de error
    }
  }
}
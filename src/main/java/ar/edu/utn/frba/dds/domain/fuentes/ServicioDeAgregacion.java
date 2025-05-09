package ar.edu.utn.frba.dds.domain.fuentes;

import ar.edu.utn.frba.dds.domain.hecho.Hecho;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;


/**
 * Servicio de Agregacion.
 */
public class ServicioDeAgregacion extends Fuente {
  private final List<Fuente> fuentesCargadas;

  /**
   * Constructor Servicio de Agregacion.
   */
  public ServicioDeAgregacion(String nombre) {
    super(nombre, null); // no usamos una lista fija local
    this.fuentesCargadas = new ArrayList<>();
  }

  /**
   * Agrega fuentes a la lista de fuentes cargadas.
   */
  public void agregarFuente(Fuente fuente) {
    this.fuentesCargadas.add(fuente);
  }

  /**
   * Obtiene los hechos de una fuente.
   */
  @Override
  public List<Hecho> obtenerHechos() {
    return fuentesCargadas.stream()
        .flatMap(fuente -> fuente.obtenerHechos().stream())
        .collect(Collectors.toList());
  }

  /**
   * Elimina un hecho de una fuente.
   */
  @Override
  public void eliminarHecho(UUID hecho) {
    for (Fuente fuente : fuentesCargadas) {
      try {
        fuente.eliminarHecho(hecho);
        return; // eliminado con éxito, salimos
      } catch (IllegalStateException e) {
        // ignoramos y seguimos buscando en otras fuentes
      }
    }
    throw new IllegalStateException("El hecho no se encontró: " + hecho);
  }

}

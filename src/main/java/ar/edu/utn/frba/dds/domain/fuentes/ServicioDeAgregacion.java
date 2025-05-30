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
    super(nombre);
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
   * muestra las fuentes cargadas.
   */
  public List<Fuente> getFuentesCargadas() {
    return new ArrayList<>(fuentesCargadas);
  }

}

package ar.edu.utn.frba.dds.domain.fuentes;

import ar.edu.utn.frba.dds.domain.hecho.Hecho;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


/**
 * Servicio de Agregación.
 * Permite agregar múltiples fuentes y obtener los hechos combinados de todas ellas.
 */
public class FuenteDeAgregacion implements Fuente {
  private final List<Fuente> fuentesCargadas;
  private final String nombre;

  /**
   * Constructor del Servicio de Agregación.
   *
   * @param nombre Nombre del servicio de agregación
   */
  public FuenteDeAgregacion(String nombre) {
    this.validarFuente(nombre);
    this.nombre = nombre;
    this.fuentesCargadas = new ArrayList<>();
  }

  /**
   * agrega una fuente al servicio de agregación.
   *
   * @param fuente Fuente a agregar
   */
  public void agregarFuente(Fuente fuente) {
    this.fuentesCargadas.add(fuente);
  }

  /**
   * obtiene los hechos de todas las fuentes cargadas.
   *
   * @return Lista de hechos combinados de todas las fuentes
   */
  @Override
  public List<Hecho> obtenerHechos() {
    return fuentesCargadas.stream()
                          .flatMap(fuente -> fuente.obtenerHechos()
                                                   .stream())
                          .collect(Collectors.toList());
  }

  /**
   * obtiene las fuentes cargadas en el servicio de agregación.
   *
   * @return Lista de fuentes cargadas
   */
  public List<Fuente> getFuentesCargadas() {
    return new ArrayList<>(fuentesCargadas);
  }

  public String getNombre() { return nombre;}

}

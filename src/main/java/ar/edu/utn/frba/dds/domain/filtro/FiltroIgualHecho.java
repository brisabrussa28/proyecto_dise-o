package ar.edu.utn.frba.dds.domain.filtro;

import ar.edu.utn.frba.dds.domain.hecho.Hecho;

/**
 * Filtra segun un Hecho concreto.
 */
public class FiltroIgualHecho extends Filtro {
  /**
   * Constructor de FiltroIgualHecho.
   *
   * @param hecho Hecho con el que se compararán los demás hechos.
   */

  public FiltroIgualHecho(Hecho hecho) {
    super(hechos -> hechos.stream()
                          .filter(h -> {
                            if (hecho.getTitulo() != null && !hecho.getTitulo()
                                                                   .equalsIgnoreCase(h.getTitulo())) {
                              return false;
                            }
                            if (hecho.getCategoria() != null && !hecho.getCategoria()
                                                                      .equalsIgnoreCase(h.getCategoria())) {
                              return false;
                            }
                            if (hecho.getDireccion() != null && !hecho.getDireccion()
                                                                      .equalsIgnoreCase(h.getDireccion())) {
                              return false;
                            }
                            if (hecho.getFechaSuceso() != null && !hecho.getFechaSuceso()
                                                                        .equals(h.getFechaSuceso())) {
                              return false;
                            }
                            if (hecho.getFechaCarga() != null && !hecho.getFechaCarga()
                                                                       .equals(h.getFechaCarga())) {
                              return false;
                            }
                            return hecho.getUbicacion() == null || hecho.getUbicacion()
                                                                        .equals(h.getUbicacion());
                          })
                          .toList());
  }
}
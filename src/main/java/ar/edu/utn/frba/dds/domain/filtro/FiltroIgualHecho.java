package ar.edu.utn.frba.dds.domain.filtro;

import ar.edu.utn.frba.dds.domain.hecho.Hecho;
import java.util.List;

/**
 * Filtra segun un Hecho concreto.
 */
public class FiltroIgualHecho implements Filtro {
  private final Hecho hecho;

  public FiltroIgualHecho(Hecho hecho) {
    this.hecho = hecho;
  }

  @Override
  public List<Hecho> filtrar(List<Hecho> hechos) {
    return hechos.stream()
                 .filter(h -> {
                   if (hecho.getTitulo() != null && !hecho.getTitulo().equalsIgnoreCase(h.getTitulo())) {
                     return false;
                   }
                   if (hecho.getCategoria() != null && !hecho.getCategoria().equalsIgnoreCase(h.getCategoria())) {
                     return false;
                   }
                   if (hecho.getDireccion() != null && !hecho.getDireccion().equalsIgnoreCase(h.getDireccion())) {
                     return false;
                   }
                   if (hecho.getFechaSuceso() != null && !hecho.getFechaSuceso().equals(h.getFechaSuceso())) {
                     return false;
                   }
                   if (hecho.getFechaCarga() != null && !hecho.getFechaCarga().equals(h.getFechaCarga())) {
                     return false;
                   }
                   return hecho.getUbicacion() == null || hecho.getUbicacion().equals(h.getUbicacion());
                 })
                 .toList();
  }
}

package ar.edu.utn.frba.dds.domain.filtro;

import ar.edu.utn.frba.dds.domain.hecho.Hecho;
import java.util.List;

/**
 * Clase base para filtros de hechos.
 */
public interface Filtro {
  List<Hecho> filtrar(List<Hecho> hechos);

}

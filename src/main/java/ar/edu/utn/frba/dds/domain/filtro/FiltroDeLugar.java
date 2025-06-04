package ar.edu.utn.frba.dds.domain.filtro;

import ar.edu.utn.frba.dds.domain.hecho.Hecho;
import ar.edu.utn.frba.dds.domain.info.PuntoGeografico;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Clase de filtro de lugar.
 */
public class FiltroDeLugar extends Filtro {
  /**
   * Constructor de FiltroDeLugar.
   *
   * @param lugar PuntoGeografico a filtrar en los hechos.
   */
  public FiltroDeLugar(PuntoGeografico lugar) {
    super(hechos -> hechos.stream()
        .filter(h -> h.getUbicacion().equals(lugar))
        .toList());
  }
}
package ar.edu.utn.frba.dds.domain.detectorspam;

import ar.edu.utn.frba.dds.domain.reportes.Solicitud;
import java.util.HashSet;
import java.util.Set;

/**
 * Interfaz de nuestro componente para detectar si una solicitud es spam.
 */
public interface DetectorSpam {
  final Set<Solicitud> solicitudesSpam = new HashSet<>();

  public default int cantidadDetectada(){
    return solicitudesSpam.size();
  }
  /**
   * Mét0do para detectar si un texto es spam.
   *
   * @param texto El texto a evaluar.
   * @return true si el texto es considerado spam, false en caso contrario.
   */
  boolean esSpam(String texto);
}
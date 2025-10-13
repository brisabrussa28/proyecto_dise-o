package ar.edu.utn.frba.dds.model.reportes.detectorspam;

/**
 * Interfaz de nuestro componente para detectar si una solicitud es spam.
 */
public interface DetectorSpam {
  /**
   * Mét0do para detectar si un texto es spam.
   *
   * @param texto El texto a evaluar.
   * @return true si el texto es considerado spam, false en caso contrario.
   */
  boolean esSpam(String texto);
}
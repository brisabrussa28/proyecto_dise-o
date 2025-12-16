package ar.edu.utn.frba.dds.model.lector.csv.filaconverter.model.exceptions;

/**
 * Excepción solicitud inexistente.
 */
public class SolicitudInexistenteException extends RuntimeException {
  /**
   * Constructor.
   */
  public SolicitudInexistenteException(String mensaje) {
    super(mensaje);
  }
}


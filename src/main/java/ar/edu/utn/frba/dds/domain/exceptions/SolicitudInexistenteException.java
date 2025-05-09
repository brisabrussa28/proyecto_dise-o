package ar.edu.utn.frba.dds.domain.exceptions;

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


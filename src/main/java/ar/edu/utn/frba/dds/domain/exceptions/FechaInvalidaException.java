package ar.edu.utn.frba.dds.domain.exceptions;

/**
 * Excepción fecha invalida.
 */
public class FechaInvalidaException extends RuntimeException {

  /**
   * Constructor 1.
   */
  public FechaInvalidaException(String mensaje) {
    super(mensaje);
  }

  /**
   * Constructor 2.
   */
  public FechaInvalidaException(String mensaje, Throwable causa) {
    super(mensaje, causa);
  }
}

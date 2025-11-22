package ar.edu.utn.frba.dds.model.exceptions;

/**
 * Excepción razón inválida.
 */
public class RazonInvalidaException extends RuntimeException {
  /**
   * Constructor.
   */
  public RazonInvalidaException(String mensaje) {
    super(mensaje);
  }
}
package ar.edu.utn.frba.dds.domain.exceptions;

/**
 * Excepción etiqueta invalida.
 */
public class EtiquetaInvalidaException extends RuntimeException {
  /**
   * Constructor.
   */
  public EtiquetaInvalidaException(String mensaje) {
    super(mensaje);
  }
}
package ar.edu.utn.frba.dds.domain.exceptions;

/**
 * Excepción archivo vacio.
 */
public class ArchivoVacioException extends RuntimeException {
  /**
   * Constructor.
   */
  public ArchivoVacioException(String mensaje) {
    super(mensaje);
  }
}

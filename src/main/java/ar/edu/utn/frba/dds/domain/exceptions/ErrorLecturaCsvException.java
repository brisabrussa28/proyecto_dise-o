package ar.edu.utn.frba.dds.domain.exceptions;

/**
 * Excepción de error de lectura del archivo.csv.
 */
public class ErrorLecturaCsvException extends RuntimeException {
  /**
   * Constructor 1.
   */
  public ErrorLecturaCsvException(String mensaje) {
    super(mensaje);
  }

  /**
   * Constructor 2.
   */
  public ErrorLecturaCsvException(String mensaje, Throwable causa) {
    super(mensaje, causa);
  }
}
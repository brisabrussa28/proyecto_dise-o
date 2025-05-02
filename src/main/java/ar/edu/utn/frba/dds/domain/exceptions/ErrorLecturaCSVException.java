package ar.edu.utn.frba.dds.domain.exceptions;

public class ErrorLecturaCSVException extends RuntimeException {
  public ErrorLecturaCSVException(String mensaje) {
    super(mensaje);
  }

  public ErrorLecturaCSVException(String mensaje, Throwable causa) {
    super(mensaje, causa);
  }
}
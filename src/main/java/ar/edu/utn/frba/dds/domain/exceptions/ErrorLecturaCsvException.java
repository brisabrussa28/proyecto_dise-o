package ar.edu.utn.frba.dds.domain.exceptions;

public class ErrorLecturaCsvException extends RuntimeException {
  public ErrorLecturaCsvException(String mensaje) {
    super(mensaje);
  }

  public ErrorLecturaCsvException(String mensaje, Throwable causa) {
    super(mensaje, causa);
  }
}
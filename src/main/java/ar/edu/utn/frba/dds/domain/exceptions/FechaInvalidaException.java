package ar.edu.utn.frba.dds.domain.exceptions;

public class FechaInvalidaException extends RuntimeException {

  public FechaInvalidaException(String mensaje) {
    super(mensaje);
  }

  public FechaInvalidaException(String mensaje, Throwable causa) {
    super(mensaje, causa);
  }
}

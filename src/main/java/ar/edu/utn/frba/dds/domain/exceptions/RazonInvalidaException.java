package ar.edu.utn.frba.dds.domain.exceptions;

public class RazonInvalidaException extends RuntimeException {
  public RazonInvalidaException(String mensaje) {
    super(mensaje);
  }
}
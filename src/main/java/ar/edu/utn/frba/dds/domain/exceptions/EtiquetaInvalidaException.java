package ar.edu.utn.frba.dds.domain.exceptions;

public class EtiquetaInvalidaException extends RuntimeException {
  public EtiquetaInvalidaException(String mensaje) {
    super(mensaje);
  }
}
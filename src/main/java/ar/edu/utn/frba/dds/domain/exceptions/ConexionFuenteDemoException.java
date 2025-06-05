package ar.edu.utn.frba.dds.domain.exceptions;

public class ConexionFuenteDemoException extends RuntimeException {
  public ConexionFuenteDemoException(String mensaje, Throwable causa) {
    super(mensaje, causa);
  }
}

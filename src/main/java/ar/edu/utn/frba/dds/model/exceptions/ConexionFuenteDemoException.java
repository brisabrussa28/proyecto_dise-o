package ar.edu.utn.frba.dds.model.exceptions;

public class ConexionFuenteDemoException extends RuntimeException {
  public ConexionFuenteDemoException(String mensaje, Throwable causa) {
    super(mensaje, causa);
  }
}

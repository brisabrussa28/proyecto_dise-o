package ar.edu.utn.frba.dds.domain.exceptions;

public class ArchivoVacioException extends RuntimeException {
  public ArchivoVacioException(String mensaje) {
    super(mensaje);
  }
}

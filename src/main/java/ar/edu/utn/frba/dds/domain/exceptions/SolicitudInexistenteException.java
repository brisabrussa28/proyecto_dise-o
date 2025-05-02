package ar.edu.utn.frba.dds.domain.exceptions;

public class SolicitudInexistenteException extends RuntimeException {
  public SolicitudInexistenteException(String mensaje) {
    super(mensaje);
  }
}


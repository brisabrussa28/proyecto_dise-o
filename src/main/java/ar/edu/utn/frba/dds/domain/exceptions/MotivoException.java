package ar.edu.utn.frba.dds.domain.exceptions;

public class MotivoException extends RuntimeException {
  String motivo;

  public MotivoException(String motivo) {
    super(motivo);
  }
}
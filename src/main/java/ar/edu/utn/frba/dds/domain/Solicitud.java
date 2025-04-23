package ar.edu.utn.frba.dds.domain;

public class Solicitud {
  private Hecho hechoSolicitado;
  private String razonEliminacion;
  public Solicitud(Hecho hechoSolicitado, String motivo) {
    this.validarMotivo(motivo);
    this.hechoSolicitado = hechoSolicitado;
    this.razonEliminacion = motivo;
  }

  public Hecho getHechoSolicitado() {
    return hechoSolicitado;
  }

  public String getRazonEliminacion() {
    return razonEliminacion;
  }

  void validarMotivo(String motivo) {
    if (motivo == null || motivo.length() < 500) {
      throw new RazonInvalidaException("La razón de eliminación debe tener al menos 500 caracteres.");
    }
  }

}

class RazonInvalidaException extends RuntimeException {
  public RazonInvalidaException(String mensaje) {
    super(mensaje);
  }
}

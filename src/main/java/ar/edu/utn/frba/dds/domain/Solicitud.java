package ar.edu.utn.frba.dds.domain;

import ar.edu.utn.frba.dds.domain.exceptions.RazonInvalidaException;
import ar.edu.utn.frba.dds.main.Contribuyente;

public class Solicitud {
  private final Contribuyente solicitante;
  private final Hecho hechoSolicitado;
  private final String razonEliminacion;
  public Solicitud(Contribuyente solicitante, Hecho hechoSolicitado, String motivo) {
    this.validarMotivo(motivo);

    this.solicitante = solicitante;
    this.hechoSolicitado = hechoSolicitado;
    this.razonEliminacion = motivo;
  }

  public Contribuyente getSolicitante() {
    return solicitante;
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
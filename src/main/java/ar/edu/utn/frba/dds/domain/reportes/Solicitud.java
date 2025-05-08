package ar.edu.utn.frba.dds.domain.reportes;

import ar.edu.utn.frba.dds.domain.exceptions.RazonInvalidaException;
import ar.edu.utn.frba.dds.domain.fuentes.Fuente;
import ar.edu.utn.frba.dds.domain.hecho.Hecho;
import ar.edu.utn.frba.dds.main.Contribuyente;

public class Solicitud {
  private final Contribuyente solicitante;
  private final Hecho hechoSolicitado;
  private final String razonEliminacion;
  private final Fuente fuente;

  public Solicitud(Contribuyente solicitante, Hecho hechoSolicitado, Fuente fuente, String motivo) {
    this.validarMotivo(motivo);
    this.fuente = fuente;
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

  public Fuente getFuente() {
    return fuente;
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
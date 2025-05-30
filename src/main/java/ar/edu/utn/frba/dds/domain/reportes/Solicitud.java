package ar.edu.utn.frba.dds.domain.reportes;

import ar.edu.utn.frba.dds.domain.exceptions.RazonInvalidaException;
import ar.edu.utn.frba.dds.domain.fuentes.Fuente;
import ar.edu.utn.frba.dds.domain.hecho.Hecho;
import ar.edu.utn.frba.dds.main.Contribuyente;
import java.util.UUID;

/**
 * Solicitud.
 */
public class Solicitud {
  Contribuyente solicitante;
  Hecho hechoSolicitado;
  String razonEliminacion;


  /**
   * Solicitud.
   *
   * @param solicitante     Contribuyente
   * @param hechoSolicitado Hecho
   * @param motivo          String
   */
  public Solicitud(Contribuyente solicitante,
                   Hecho hechoSolicitado,
                   String motivo) {
    this.validarMotivo(motivo);
    this.solicitante = solicitante;
    this.hechoSolicitado = hechoSolicitado;
    this.razonEliminacion = motivo;
  }

  public Contribuyente getSolicitante() {
    return solicitante;
  }

  public Hecho getHechoSolicitado() {
    return this.hechoSolicitado;
  }

  public String getRazonEliminacion() {
    return razonEliminacion;
  }

  void validarMotivo(String motivo) {
    if (motivo == null || motivo.length() < 500) {
      throw new RazonInvalidaException("Tiene menos 500 caracteres.");
    }
  }

}
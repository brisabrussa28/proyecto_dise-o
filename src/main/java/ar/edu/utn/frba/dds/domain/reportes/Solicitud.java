package ar.edu.utn.frba.dds.domain.reportes;

import ar.edu.utn.frba.dds.domain.exceptions.RazonInvalidaException;
import ar.edu.utn.frba.dds.domain.hecho.Hecho;
import ar.edu.utn.frba.dds.main.Contribuyente;

/**
 * Solicitud.
 */
public class Solicitud {

  Contribuyente solicitante;
  Hecho hechoSolicitado;
  String razonEliminacion;

  /**
   * Constructor de solicitud.
   *
   * @param solicitante     Contribuyente que solicita la eliminación
   * @param hechoSolicitado Hecho solicitado para eliminar
   * @param motivo          Razón de la eliminación
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
    return hechoSolicitado;
  }

  public String getRazonEliminacion() {
    return razonEliminacion;
  }

  /**
   * Valida que el motivo tenga al menos 500 caracteres.
   *
   * @param motivo razón escrita
   */
  void validarMotivo(String motivo) {
    if (motivo == null || motivo.length() < 500) {
      throw new RazonInvalidaException("Tiene menos de 500 caracteres.");
    }
  }
}

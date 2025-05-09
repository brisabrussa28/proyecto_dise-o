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
  UUID idhechoSolicitado;
  String razonEliminacion;
  Fuente fuente;

  /**
   * Solicitud.
   *
   * @param solicitante     Contribuyente
   * @param idhechoSolicitado UUID
   * @param fuente          Fuente
   * @param motivo          String
   */
  public Solicitud(Contribuyente solicitante,
                   UUID idhechoSolicitado,
                   Fuente fuente,
                   String motivo) {
    this.validarMotivo(motivo);
    this.fuente = fuente;
    this.solicitante = solicitante;
    this.idhechoSolicitado = idhechoSolicitado;
    this.razonEliminacion = motivo;
  }

  public Contribuyente getSolicitante() {
    return solicitante;
  }

  public UUID getHechoSolicitado() {
    return this.idhechoSolicitado;
  }

  public Fuente getFuente() {
    return fuente;
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
package ar.edu.utn.frba.dds.domain.reportes;

import ar.edu.utn.frba.dds.domain.exceptions.RazonInvalidaException;
import ar.edu.utn.frba.dds.domain.hecho.Hecho;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.Objects;
import java.util.UUID;

/**
 * Solicitud.
 */
public class Solicitud {

  UUID solicitante;
  public Hecho hechoSolicitado;
  String razonEliminacion;

  /**
   * Constructor de solicitud.
   *
   * @param solicitante     Usuario que solicita la eliminación
   * @param hechoSolicitado Hecho solicitado para eliminar
   * @param motivo          Razón de la eliminación
   */
  @SuppressFBWarnings(value = "EI_EXPOSE_REP", justification = "El objeto Hecho debe ser mutable y compartido intencionalmente")
  public Solicitud(UUID solicitante, Hecho hechoSolicitado, String motivo) {
    /*
      todo: Deberia o ser un observer o consultarlo via bdd/hibernate, guardarlo asi esta mal
      pero de momento no hay otra
    */

    if (solicitante == null) {
      throw new NullPointerException("El solicitante no puede ser null");
    }
    if (hechoSolicitado == null) {
      throw new NullPointerException("El hecho solicitado no puede ser null");
    }
    if (motivo == null) {
      throw new RazonInvalidaException("El motivo no puede ser null");
    }
    this.validarMotivo(motivo);
    this.solicitante = solicitante;

    this.hechoSolicitado = hechoSolicitado;
    this.razonEliminacion = motivo;
  }

  public Hecho getHechoSolicitado() {
    return this.hechoSolicitado;
  }

  public String getRazonEliminacion() {
    return razonEliminacion;
  }

  public UUID getSolicitante() {
    return solicitante;
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


  /**
   * Si no modifico equals y hassCode no puede comparar solicitudes.
   *
   * @param o Object.
   * @return si son iguales.
   */
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    Solicitud solicitud = (Solicitud) o;
    return Objects.equals(solicitante, solicitud.solicitante)
        && Objects.equals(hechoSolicitado, solicitud.hechoSolicitado)
        && Objects.equals(razonEliminacion, solicitud.razonEliminacion);
  }

  @Override
  public int hashCode() {
    return Objects.hash(solicitante, hechoSolicitado, razonEliminacion);
  }

}
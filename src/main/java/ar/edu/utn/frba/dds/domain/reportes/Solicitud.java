package ar.edu.utn.frba.dds.domain.reportes;

import ar.edu.utn.frba.dds.domain.exceptions.RazonInvalidaException;
import ar.edu.utn.frba.dds.domain.hecho.Hecho;
import ar.edu.utn.frba.dds.usuario.Usuario;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * Solicitud.
 */
public class Solicitud {

  private final Usuario solicitante;
  private final Hecho hechoSolicitado;
  private final String razonEliminacion;

  /**
   * Constructor de solicitud.
   *
   * @param solicitante     Usuario que solicita la eliminación
   * @param hechoSolicitado Hecho solicitado para eliminar (original, no copia)
   * @param motivo          Razón de la eliminación
   */
  @SuppressFBWarnings(
      value = "EI_EXPOSE_REP2",
      justification = "Exposing hechoSolicitado is intentional and required for deletion by ID."
  )
  public Solicitud(Usuario solicitante, Hecho hechoSolicitado, String motivo) {
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
    this.hechoSolicitado = hechoSolicitado; // Intentionally not defensive copy
    this.razonEliminacion = motivo;
  }

  /**
   * Devuelve el hecho original solicitado para eliminar.
   * Exposing internal representation is intentional and required for deletion.
   */
  @SuppressFBWarnings(
      value = {"EI_EXPOSE_REP", "EI_EXPOSE_REP2"},
      justification = "Exponer hechoSolicitado es intencional."
  )
  public Hecho getHechoSolicitado() {

    return hechoSolicitado;
  }

  public String getRazonEliminacion() {
    return razonEliminacion;
  }

  public Usuario getSolicitante() {
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
}
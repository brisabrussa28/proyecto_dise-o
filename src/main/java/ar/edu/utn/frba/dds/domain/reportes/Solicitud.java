package ar.edu.utn.frba.dds.domain.reportes;

import ar.edu.utn.frba.dds.domain.exceptions.RazonInvalidaException;
import ar.edu.utn.frba.dds.domain.hecho.Hecho;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.Objects;

public class Solicitud {

  private Hecho hechoSolicitado;
  private String razonEliminacion;
  private EstadoSolicitud estado;

  @SuppressFBWarnings(value = "EI_EXPOSE_REP", justification = "El objeto Hecho debe ser mutable y compartido intencionalmente")
  public Solicitud(Hecho hechoSolicitado, String motivo) {
    if (hechoSolicitado == null) {
      throw new NullPointerException("El hecho solicitado no puede ser null");
    }
    if (motivo == null) {
      throw new RazonInvalidaException("El motivo no puede ser null");
    }
    this.validarMotivo(motivo);

    this.hechoSolicitado = hechoSolicitado;
    this.razonEliminacion = motivo;
    this.estado = EstadoSolicitud.PENDIENTE;
  }

  public Hecho getHechoSolicitado() {
    return this.hechoSolicitado;
  }

  public String getRazonEliminacion() {
    return this.razonEliminacion;
  }

  public EstadoSolicitud getEstado() {
    return this.estado;
  }

  public void marcarComoSpam() {
    this.estado = EstadoSolicitud.SPAM;
  }

  public void aceptar() {
    this.estado = EstadoSolicitud.ACEPTADA;
  }

  public void rechazar() {
    this.estado = EstadoSolicitud.RECHAZADA;
  }

  void validarMotivo(String motivo) {
    if (motivo.length() < 500) {
      throw new RazonInvalidaException("Tiene menos de 500 caracteres.");
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Solicitud solicitud = (Solicitud) o;
    return Objects.equals(hechoSolicitado, solicitud.hechoSolicitado)
        && Objects.equals(razonEliminacion, solicitud.razonEliminacion);
  }

  @Override
  public int hashCode() {
    // CORRECCIÓN: El estado (que es mutable) se excluye del hash code.
    return Objects.hash(hechoSolicitado, razonEliminacion);
  }
}

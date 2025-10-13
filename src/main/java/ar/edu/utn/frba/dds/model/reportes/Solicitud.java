package ar.edu.utn.frba.dds.model.reportes;

import ar.edu.utn.frba.dds.model.exceptions.RazonInvalidaException;
import ar.edu.utn.frba.dds.model.hecho.Estado;
import ar.edu.utn.frba.dds.model.hecho.Hecho;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.Indexed;

/**
 * Solicitud.
 */

@Entity
@Indexed
public class Solicitud {
  @Id
  @SequenceGenerator(name = "solicitud_seq", sequenceName = "solicitud_sequence", allocationSize = 1)
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "solicitud_seq")
  @Column(name = "solicitud_id")
  public Long id;

  @ManyToOne
  @JoinColumn(name = "hecho_id")
  private Hecho hechoSolicitado;

  @Column(length = 1024)
  private String razonEliminacion;

  @Enumerated(EnumType.STRING)
  @Column(name = "estado", nullable = false)
  private EstadoSolicitud estado;

  public void setHechoSolicitado(Hecho hechoSolicitado) {
    this.hechoSolicitado = hechoSolicitado;
  }

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

  public Solicitud() {

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
    this.hechoSolicitado.setEstado(Estado.ELIMINADO);
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

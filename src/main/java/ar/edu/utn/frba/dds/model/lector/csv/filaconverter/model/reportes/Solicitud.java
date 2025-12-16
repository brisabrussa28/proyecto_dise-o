package ar.edu.utn.frba.dds.model.lector.csv.filaconverter.model.reportes;

import ar.edu.utn.frba.dds.model.exceptions.RazonInvalidaException;
import ar.edu.utn.frba.dds.model.hecho.Estado;
import ar.edu.utn.frba.dds.model.hecho.Hecho;
import ar.edu.utn.frba.dds.model.reportes.EstadoSolicitud;
import ar.edu.utn.frba.dds.model.usuario.Usuario;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.time.LocalDateTime;
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
import org.hibernate.search.annotations.Indexed;

/**
 * Solicitud.
 */

@Entity
@Indexed
public class Solicitud {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "solicitud_id")
  public Long id;

  @ManyToOne
  @JoinColumn(name = "usuario_id")
  private Usuario usuario;

  @ManyToOne
  @JoinColumn(name = "hecho_id")
  private Hecho hechoSolicitado;

  @Column(length = 1024, name = "solicitud_razon_eliminacion")
  private String razonEliminacion;

  @Enumerated(EnumType.STRING)
  @Column(name = "solicitud_estado", nullable = false)
  private EstadoSolicitud estado;

  @Column(name = "solicitud_fecha")
  private LocalDateTime fechaReporte;

  /**
   * Constructor vacío para JPA.
   */
  public Solicitud() {
  }

  /**
   * Constructor completo.
   */
  @SuppressFBWarnings(value = "EI_EXPOSE_REP",
      justification = "El objeto Hecho debe ser mutable y compartido intencionalmente")
  public Solicitud(Hecho hechoSolicitado, String motivo, Usuario usuario) {
    if (hechoSolicitado == null) {
      throw new NullPointerException("El hecho no puede ser null");
    }
    if (motivo == null) {
      throw new RazonInvalidaException("El motivo no puede ser null");
    }
    if (usuario == null) {
      throw new IllegalArgumentException("El usuario no puede ser null");
    }

    this.validarMotivo(motivo);

    this.hechoSolicitado = hechoSolicitado;
    this.razonEliminacion = motivo;
    this.usuario = usuario;
    this.estado = EstadoSolicitud.PENDIENTE;
    this.fechaReporte = LocalDateTime.now();
  }

  public Long getId() {
    return this.id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Usuario getUsuario() {
    return this.usuario;
  }

  public void setUsuario(Usuario usuario) {
    this.usuario = usuario;
  }

  public Hecho getHechoSolicitado() {
    return this.hechoSolicitado;
  }

  public void setHechoSolicitado(Hecho hechoSolicitado) {
    this.hechoSolicitado = hechoSolicitado;
  }

  public String getRazonEliminacion() {
    return this.razonEliminacion;
  }

  public EstadoSolicitud getEstado() {
    return this.estado;
  }

  public LocalDateTime getFechaReporte() {
    return this.fechaReporte;
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
    if (motivo.length() < 500 || motivo.length() > 1024) {
      throw new RazonInvalidaException(
          "La longitud del mensaje se encuentra fuera del rango (500 < caracteres < 1024"
      );
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
    return Objects.hash(hechoSolicitado, razonEliminacion);
  }
}

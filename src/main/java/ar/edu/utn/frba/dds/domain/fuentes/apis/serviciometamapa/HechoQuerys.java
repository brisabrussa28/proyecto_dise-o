package ar.edu.utn.frba.dds.domain.fuentes.apis.serviciometamapa;

import ar.edu.utn.frba.dds.domain.info.PuntoGeografico;
import java.time.LocalDateTime;
import javax.persistence.Embeddable;

@Embeddable
public class HechoQuerys {
  private String categoria;
  private String provincia;
  private PuntoGeografico ubicacion;
  private LocalDateTime fechaReporteDesde;
  private LocalDateTime fechaReporteHasta;
  private LocalDateTime fechaAcontecimientoDesde;
  private LocalDateTime fechaAcontecimientoHasta;

  public HechoQuerys() {
  }

  public HechoQuerys(
      String categoria,
      String provincia,
      PuntoGeografico ubicacion,
      LocalDateTime fechaAcontecimientoHasta,
      LocalDateTime fechaReporteHasta,
      LocalDateTime fechaAcontecimientoDesde,
      LocalDateTime fechaReporteDesde
  ) {
    this.categoria = categoria;
    this.provincia = provincia;
    this.ubicacion = ubicacion;
    this.fechaAcontecimientoHasta = fechaAcontecimientoHasta;
    this.fechaReporteHasta = fechaReporteHasta;
    this.fechaAcontecimientoDesde = fechaAcontecimientoDesde;
    this.fechaReporteDesde = fechaReporteDesde;
  }

  public String getCategoria() { return categoria; }
  public String getProvincia() { return provincia; }
  public PuntoGeografico getUbicacion() { return ubicacion; }
  public LocalDateTime getFechaReporteDesde() { return fechaReporteDesde; }
  public LocalDateTime getFechaReporteHasta() { return fechaReporteHasta; }
  public LocalDateTime getFechaAcontecimientoDesde() { return fechaAcontecimientoDesde; }
  public LocalDateTime getFechaAcontecimientoHasta() { return fechaAcontecimientoHasta; }

  public void setCategoria(String categoria) { this.categoria = categoria; }
  public void setProvincia(String provincia) { this.provincia = provincia; }
  public void setUbicacion(PuntoGeografico ubicacion) { this.ubicacion = ubicacion; }
  public void setFechaReporteDesde(LocalDateTime fechaReporteDesde) { this.fechaReporteDesde = fechaReporteDesde; }
  public void setFechaReporteHasta(LocalDateTime fechaReporteHasta) { this.fechaReporteHasta = fechaReporteHasta; }
  public void setFechaAcontecimientoDesde(LocalDateTime fechaAcontecimientoDesde) { this.fechaAcontecimientoDesde = fechaAcontecimientoDesde; }
  public void setFechaAcontecimientoHasta(LocalDateTime fechaAcontecimientoHasta) { this.fechaAcontecimientoHasta = fechaAcontecimientoHasta; }
}
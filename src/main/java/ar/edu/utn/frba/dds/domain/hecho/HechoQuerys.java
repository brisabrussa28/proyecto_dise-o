package ar.edu.utn.frba.dds.domain.hecho;

import ar.edu.utn.frba.dds.domain.info.PuntoGeografico;
import java.util.Date;

public class HechoQuerys {    // Value Object for filters
  private String categoria;
  private PuntoGeografico ubicacion;
  private Date fechaReporteDesde;
  private Date fechaReporteHasta;
  private Date fechaAcontecimientoDesde;
  private Date fechaAcontecimientoHasta;

  public HechoQuerys(
      String categoria,
      PuntoGeografico ubicacion,
      Date fechaAcontecimientoHasta,
      Date fechaReporteHasta,
      Date fechaAcontecimientoDesde,
      Date fechaReporteDesde
  ) {
    this.categoria = categoria;
    this.ubicacion = ubicacion;
    this.fechaAcontecimientoHasta = fechaAcontecimientoHasta != null
                                    ? new Date(fechaAcontecimientoHasta.getTime())
                                    : null;
    this.fechaAcontecimientoDesde = fechaAcontecimientoDesde != null
                                    ? new Date(fechaAcontecimientoDesde.getTime())
                                    : null;
    this.fechaReporteHasta = fechaReporteHasta != null
                             ? new Date(fechaReporteHasta.getTime())
                             : null;
    this.fechaReporteDesde = fechaReporteDesde != null
                             ? new Date(fechaReporteDesde.getTime())
                             : null;
  }

  // Getters with defensive copies
  public String getCategoria() {
    return categoria;
  }

  public PuntoGeografico getUbicacion() {
    return ubicacion;
  }

  public Date getFechaReporteDesde() {
    return fechaReporteDesde != null
           ? new Date(fechaReporteDesde.getTime())
           : null;
  }

  public Date getFechaReporteHasta() {
    return fechaReporteHasta != null
           ? new Date(fechaReporteHasta.getTime())
           : null;
  }

  public Date getFechaAcontecimientoDesde() {
    return fechaAcontecimientoDesde != null
           ? new Date(fechaAcontecimientoDesde.getTime())
           : null;
  }

  public Date getFechaAcontecimientoHasta() {
    return fechaAcontecimientoHasta != null
           ? new Date(fechaAcontecimientoHasta.getTime())
           : null;
  }
}
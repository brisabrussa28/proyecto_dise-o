package ar.edu.utn.frba.dds.domain.fuentes.apis.serviciometamapa;

import ar.edu.utn.frba.dds.domain.info.PuntoGeografico;
import java.time.LocalDateTime;

public class HechoQuerys {    // Value Object for filters
  private final String categoria;
  private final String provincia;
  private final PuntoGeografico ubicacion;
  private final LocalDateTime fechaReporteDesde;
  private final LocalDateTime fechaReporteHasta;
  private final LocalDateTime fechaAcontecimientoDesde;
  private final LocalDateTime fechaAcontecimientoHasta;

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
    this.validarFecha(fechaAcontecimientoHasta);
    this.validarFecha(fechaReporteHasta);
    this.validarFecha(fechaAcontecimientoDesde);
    this.validarFecha(fechaReporteDesde);
    this.fechaAcontecimientoHasta = fechaAcontecimientoHasta;
    this.fechaAcontecimientoDesde = fechaAcontecimientoDesde;
    this.fechaReporteHasta = fechaReporteHasta;
    this.fechaReporteDesde = fechaReporteDesde;
  }

  private void validarFecha(LocalDateTime fecha) {
    if (fecha == null) {
      throw new RuntimeException("Fecha no puede ser nulo");
    }
  }

  public String getCategoria() {
    return categoria;
  }

  public String getProvincia() {
    return provincia;
  }

  public PuntoGeografico getUbicacion() {
    return ubicacion;
  }

  public LocalDateTime getFechaReporteDesde() {
    return fechaReporteDesde;
  }

  public LocalDateTime getFechaReporteHasta() {
    return fechaReporteHasta;
  }

  public LocalDateTime getFechaAcontecimientoDesde() {
    return fechaAcontecimientoDesde;
  }

  public LocalDateTime getFechaAcontecimientoHasta() {
    return fechaAcontecimientoHasta;
  }
}
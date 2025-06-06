package ar.edu.utn.frba.dds.domain.entities;

import ar.edu.utn.frba.dds.domain.info.PuntoGeografico;
import java.util.Date;

public class HechoQuerys {    //Value Object para los filtros
  public String categoria;
  public PuntoGeografico ubicacion;
  public Date fecha_reporte_desde;
  public Date fecha_reporte_hasta;
  public Date fecha_acontecimiento_desde;
  public Date fecha_acontecimiento_hasta;

  public HechoQuerys(
      String categoria,
      PuntoGeografico ubicacion,
      Date fecha_acontecimiento_hasta,
      Date fecha_reporte_hasta,
      Date fecha_acontecimiento_desde,
      Date fecha_reporte_desde
  ) {
    this.categoria = categoria;
    this.ubicacion = ubicacion;
    this.fecha_acontecimiento_hasta = fecha_acontecimiento_hasta;
    this.fecha_acontecimiento_desde = fecha_acontecimiento_desde;
    this.fecha_reporte_hasta = fecha_reporte_hasta;
    this.fecha_reporte_desde = fecha_reporte_desde;
  }
}

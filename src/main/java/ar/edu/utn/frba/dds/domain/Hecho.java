package ar.edu.utn.frba.dds.domain;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class Hecho {
  private String titulo;
  private String descripcion;
  private String categoria;
  private String Direcccion;
  private LocalDateTime fechaSuceso;
  private LocalDateTime fechaCarga;
  private Fuente origen;
  private List<Etiqueta> etiquetas;
  private PuntoGeografico ubicacion;
  private UUID id;
  boolean vigencia; // NOTE: Nos va a servir para eliminar hechos :D

  public Hecho(
      String titulo,
      String descripcion,
      String categoria,
      String Direcccion,
      PuntoGeografico ubicacion,
      LocalDateTime fechaSuceso,
      LocalDateTime fechaCarga,
      Fuente origen,
      List<Etiqueta> etiquetas
  ) {
    this.titulo = titulo;
    this.descripcion = descripcion;
    this.categoria = categoria;
    this.ubicacion = ubicacion;
    this.Direcccion = Direcccion;
    this.fechaSuceso = fechaSuceso;
    this.fechaCarga = fechaCarga;
    this.origen = origen;
    this.etiquetas = etiquetas;
    this.id = UUID.randomUUID();
    this.vigencia = true;
  }

  public UUID getId() {
    return id;
  }

  public String getTitulo() {
    return titulo;
  }

  public String getDescripcion() {
    return descripcion;
  }

  public String getCategoria() {
    return categoria;
  }

  public String getDirecccion() {
    return Direcccion;
  }

  public PuntoGeografico getUbicacion() {
    return ubicacion;
  }

  public LocalDateTime getFechaSuceso() {
    return fechaSuceso;
  }

  public LocalDateTime getFechaCarga() {
    return fechaCarga;
  }


  public Fuente getOrigen() {
    return origen;
  }

  public List<Etiqueta> getEtiquetas() {
    return etiquetas;
  }

  boolean perteneceA(Coleccion unaColeccion) {
    return unaColeccion.contieneA(this);
  }
}

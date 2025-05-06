package ar.edu.utn.frba.dds.domain.hecho;

import ar.edu.utn.frba.dds.domain.Coleccion.Coleccion;
import ar.edu.utn.frba.dds.domain.info.Etiqueta;
import ar.edu.utn.frba.dds.domain.info.PuntoGeografico;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/*
Falta agregar el tipo de origen
Cada hecho representa una pieza de información, la cual debe contener mínimamente:
título, descripción, categoría, contenido multimedia opcional, lugar y fecha del acontecimiento,
fecha de carga y su origen (carga manual, proveniente de un dataset o provisto por un contribuyente).
*/


public class Hecho {
  private String titulo;
  private String descripcion;
  private String categoria;
  private String Direccion;
  private LocalDateTime fechaSuceso;
  private LocalDateTime fechaCarga;
  private String FuenteOrigen;
  private List<Etiqueta> etiquetas;
  private PuntoGeografico ubicacion;
  private UUID id;
  boolean vigencia; // NOTE: Nos va a servir para eliminar hechos :D

  public Hecho(
      String titulo,
      String descripcion,
      String categoria,
      String Direccion,
      PuntoGeografico ubicacion,
      LocalDateTime fechaSuceso,
      LocalDateTime fechaCarga,
      String FuenteOrigen,
      List<Etiqueta> etiquetas
  ) {
    this.titulo = titulo;
    this.descripcion = descripcion;
    this.categoria = categoria;
    this.ubicacion = ubicacion;
    this.Direccion = Direccion;
    this.fechaSuceso = fechaSuceso;
    this.fechaCarga = fechaCarga;
    this.FuenteOrigen = FuenteOrigen;
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

  public String getDireccion() {
    return Direccion;
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


  public String getOrigen() {
    return FuenteOrigen;
  }

  public boolean esDeCategoria(String categoria) {
    return this.categoria.equals(categoria);
  }

  public boolean tieneEtiqueta(Etiqueta unaEtiqueta) {
    return this.etiquetas.contains(unaEtiqueta);
  }

  public List<Etiqueta> getEtiquetas() {
    return etiquetas;
  }

  boolean perteneceA(Coleccion unaColeccion) {
    return unaColeccion.contieneA(this);
  }

  public void setOrigen(String origen) {
    this.FuenteOrigen = origen;
  }
}

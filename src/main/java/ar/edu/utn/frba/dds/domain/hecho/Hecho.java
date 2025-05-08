package ar.edu.utn.frba.dds.domain.hecho;

import ar.edu.utn.frba.dds.domain.Coleccion.Coleccion;
import ar.edu.utn.frba.dds.domain.Origen.Origen;
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
  boolean vigencia; // NOTE: Nos va a servir para eliminar hechos :D
  private String titulo;
  private String descripcion;
  private String categoria;
  private String direccion;
  private PuntoGeografico ubicacion;
  private LocalDateTime fechaSuceso;
  private LocalDateTime fechaCarga;
  private Origen fuenteOrigen;
  private List<String> etiquetas;
  private UUID id;

  public Hecho(
      String titulo,
      String descripcion,
      String categoria,
      String direccion,
      PuntoGeografico ubicacion,
      LocalDateTime fechaSuceso,
      LocalDateTime fechaCarga,
      Origen fuenteOrigen,
      List<String> etiquetas
  ) {
    this.titulo = titulo;
    this.descripcion = descripcion;
    this.categoria = categoria;
    this.ubicacion = ubicacion;
    this.direccion = direccion;
    this.fechaSuceso = fechaSuceso;
    this.fechaCarga = fechaCarga;
    this.fuenteOrigen = fuenteOrigen;
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
    return direccion;
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


  public Origen getOrigen() {
    return fuenteOrigen;
  }

  public void setOrigen(Origen origen) {
    this.fuenteOrigen = origen;
  }

  public boolean esDeCategoria(String categoria) {
    return this.categoria.equals(categoria);
  }

  public boolean tieneEtiqueta(String unaEtiqueta) {
    return this.etiquetas.contains(unaEtiqueta);
  }

  public boolean esDeTitulo(String unTitulo) {
    return this.titulo.equals(unTitulo);
  }

  public boolean sucedioEn(String unaDireccion) {
    return this.direccion.equals(unaDireccion);
  }

  public boolean esDeFecha(LocalDateTime unaFecha) {
    return this.fechaSuceso.equals(unaFecha);
  }

  public boolean seCargoEn(LocalDateTime unaFecha) {
    return this.fechaCarga.equals(unaFecha);
  }

  public boolean esDeOrigen(Origen unaOrigen) {
    return this.fuenteOrigen.equals(unaOrigen);
  }

  public List<String> getEtiquetas() {
    return etiquetas;
  }

  boolean perteneceA(Coleccion unaColeccion) {
    return unaColeccion.contieneA(this);
  }

}

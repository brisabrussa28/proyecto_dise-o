package ar.edu.utn.frba.dds.domain.hecho;

import ar.edu.utn.frba.dds.domain.coleccion.Coleccion;
import ar.edu.utn.frba.dds.domain.info.PuntoGeografico;
import ar.edu.utn.frba.dds.domain.origen.Origen;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * Hecho.
 */
public class Hecho {
  String titulo;
  String descripcion;
  String categoria;
  String direccion;
  PuntoGeografico ubicacion;
  Date fechaSuceso;
  Date fechaCarga;
  Origen fuenteOrigen;
  List<String> etiquetas;
  UUID id;

  /**
   * Constructor.
   */
  public Hecho(
      String titulo,
      String descripcion,
      String categoria,
      String direccion,
      PuntoGeografico ubicacion,
      Date fechaSuceso,
      Date fechaCarga,
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
    this.etiquetas = new ArrayList<>(etiquetas);
    this.id = UUID.randomUUID();
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

  public Date getFechaSuceso() {
    return fechaSuceso;
  }

  public Date getFechaCarga() {
    return fechaCarga;
  }

  public Origen getOrigen() {
    return fuenteOrigen;
  }

  /**
   * Si la categoría es la buscada.
   */
  public boolean esElMismo(UUID id) {
    return this.getId().equals(id);
  }

  /**
   * Si la categoria es la buscada.
   */
  public boolean esDeCategoria(String categoria) {
    return this.categoria.equals(categoria);
  }

  /**
   * Si la etiqueta es la buscada.
   */
  public boolean tieneEtiqueta(String unaEtiqueta) {
    return this.etiquetas.contains(unaEtiqueta);
  }

  /**
   * Si el título es el buscado.
   */
  public boolean esDeTitulo(String unTitulo) {
    return this.titulo.equals(unTitulo);
  }

  /**
   * Si la dirección es la buscada.
   */
  public boolean sucedioEn(String unaDireccion) {
    return this.direccion.equals(unaDireccion);
  }

  /**
   * Si la fecha del suceso es la buscada.
   */
  public boolean esDeFecha(Date unaFecha) {
    return this.fechaSuceso.equals(unaFecha);
  }

  /**
   * Si la fecha de carga es la buscada.
   */
  public boolean seCargoEl(Date unaFecha) {return this.fechaCarga.equals(unaFecha);}

  /**
   * Si no tiene datos.
   */
  public boolean estaVacio() {
    return (titulo == null || titulo.isBlank()) &&
        (descripcion == null || descripcion.isBlank()) &&
        (categoria == null || categoria.isBlank()) &&
        (direccion == null || direccion.isBlank()) &&
        ubicacion == null &&
        fechaSuceso == null &&
        (etiquetas == null || etiquetas.isEmpty());
  }
  /**
   * Si el hecho se cargó antes de la fecha buscada.
   */
  public boolean seCargoAntesDe(Date unaFecha) {
    return this.fechaCarga.before(unaFecha);
  }

  /**
   * Si el origen es el buscado.
   */
  public boolean esDeOrigen(Origen unaOrigen) {
    return this.fuenteOrigen.equals(unaOrigen);
  }

  /**
   * Si el punto geográfico es el buscado.
   */
  public boolean esDeLugar(PuntoGeografico lugar) {
    return this.ubicacion.equals(lugar);
  }

  public List<String> getEtiquetas() {
    return new ArrayList<>(this.etiquetas);
  }

  boolean perteneceA(Coleccion unaColeccion) {
    return unaColeccion.contieneA(this);
  }
}

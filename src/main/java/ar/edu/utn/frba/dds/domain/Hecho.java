package ar.edu.utn.frba.dds.domain;

import java.time.LocalDateTime;
import java.util.List;

public class Hecho {
  private String titulo;
  private String descripcion;
  private String categoria;
  private String ubicacion;
  private LocalDateTime fecha;
  private Fuente origen;
  private List<String> etiquetas;

  public Hecho(String titulo, String descripcion, String categoria, String ubicacion,
               LocalDateTime fecha, Fuente origen, List<String> etiquetas) {
    this.titulo = titulo;
    this.descripcion = descripcion;
    this.categoria = categoria;
    this.ubicacion = ubicacion;
    this.fecha = fecha;
    this.origen = origen;
    this.etiquetas = etiquetas;
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

  public String getUbicacion() {
    return ubicacion;
  }

  public LocalDateTime getFecha() {
    return fecha;
  }

  public Fuente getOrigen() {
    return origen;
  }

  public List<String> getEtiquetas() {
    return etiquetas;
  }

  boolean perteneceA(Coleccion unaColeccion) {
    return unaColeccion.contieneA(this);
  }
}

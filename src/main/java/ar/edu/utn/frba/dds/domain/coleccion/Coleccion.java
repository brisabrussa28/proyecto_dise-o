package ar.edu.utn.frba.dds.domain.coleccion;

import ar.edu.utn.frba.dds.domain.filtro.Filtro;
import ar.edu.utn.frba.dds.domain.fuentes.Fuente;
import ar.edu.utn.frba.dds.domain.hecho.Hecho;
import java.util.List;


/**
 * Clase colección.
 */
public class Coleccion {
  private final Fuente fuente;
  private final String titulo;
  private final String descripcion;
  private final String categoria;
  private Filtro filtro;
  //private String criterio;

  /**
   * Constructor.
   */
  public Coleccion(String titulo, Fuente fuente, String descripcion, String categoria) {
    this.titulo = titulo;
    this.fuente = fuente;
    this.descripcion = descripcion;
    this.categoria = categoria;
    this.filtro = new Filtro();
  }

  /**
   * Eliminar hecho de la fuente.
   */
  public void eliminarHecho(Hecho hecho) {
    fuente.eliminarHecho(hecho.getId());
  }

  /**
   * Modificación criterio de la colección.
   */
  public void agregarCriterio(Filtro nuevoFiltro) {
    this.filtro = nuevoFiltro;
  }

  /**
   * Filtrar hechos.
   */
  public List<Hecho> filtrar(Coleccion coleccion, Filtro filtro) {
    return filtro.filtrar(coleccion.getHechos());
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

  public Filtro getFiltro() {
    return filtro;
  }

  public List<Hecho> getHechos() {
    return filtro.filtrar(fuente.obtenerHechos());
  }

  /**
   * Booleano, indica si la fuente de la colección es la indicadas.
   */
  public boolean contieneFuente(Fuente unaFuente) {
    return fuente == unaFuente;
  }

  /**
   * Booleano, indica si el hecho solicitado existe en la colección.
   */
  public boolean contieneA(Hecho unHecho) {
    return this.getHechos().contains(unHecho);
  }
}


package ar.edu.utn.frba.dds.domain.coleccion;

import ar.edu.utn.frba.dds.domain.filtro.*;
import ar.edu.utn.frba.dds.domain.fuentes.Fuente;
import ar.edu.utn.frba.dds.domain.hecho.Hecho;

import java.util.ArrayList;
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
  private List<Filtro> HechosEliminados;

  /**
   * Constructor.
   */
  public Coleccion(String titulo, Fuente fuente, String descripcion, String categoria) {
    this.titulo = titulo;
    this.fuente = fuente;
    this.descripcion = descripcion;
    this.categoria = categoria;
    this.filtro = new Filtro();
    this.HechosEliminados = new ArrayList<>();
  }

  /**
   * Eliminar hecho de la fuente.
   */
  public void eliminarHecho(Hecho hecho) {
    Filtro filtroDeExclusion = new FiltroNot(new FiltroIgualHecho(hecho));
    this.HechosEliminados.add(filtroDeExclusion);
  }

  /**
   * Modificación criterio de la colección.
   */
  public void agregarCriterio(Filtro nuevoFiltro) {
    this.filtro = nuevoFiltro;
  }

  /**
   * Título de la colección.
   */
  public String getTitulo() {
    return titulo;
  }

  /**
   * Descripción de la colección.
   */
  public String getDescripcion() {
    return descripcion;
  }

  /**
   * Categoría de la colección.
   */
  public String getCategoria() {
    return categoria;
  }

  /**
   * Criterio de filtro actual.
   */
  public Filtro getFiltro() {
    return filtro;
  }

  /**
   * Obtiene los hechos filtrados aplicando criterios y exclusiones.
   */
  public List<Hecho> getHechos() {
    List<Filtro> todosLosFiltros = new ArrayList<>(HechosEliminados);
    todosLosFiltros.add(filtro);

    FiltroListaAnd filtroFinal = new FiltroListaAnd(todosLosFiltros);
    return filtroFinal.filtrar(fuente.obtenerHechos());
  }

  /**
   * Booleano, indica si la fuente de la colección es la indicada.
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

package ar.edu.utn.frba.dds.domain.coleccion;

import ar.edu.utn.frba.dds.domain.filtro.*;
import ar.edu.utn.frba.dds.domain.fuentes.Fuente;
import ar.edu.utn.frba.dds.domain.hecho.Hecho;
import ar.edu.utn.frba.dds.domain.reportes.GestorDeReportes;

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

  /**
   * Constructor.
   */
  public Coleccion(String titulo, Fuente fuente, String descripcion, String categoria) {
    this.titulo = titulo;
    this.fuente = fuente;
    this.descripcion = descripcion;
    this.categoria = categoria;
    this.filtro = new Filtro(hechos -> hechos);
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
  public List<Hecho> getHechos(GestorDeReportes gestor) {
    // Elimina los hechos que fueron eliminados por el gestor de reportes
    List<Filtro> todosLosFiltros = new ArrayList<>(gestor.hechosEliminados());

    // Agrega el filtro de la colección
    todosLosFiltros.add(filtro);

    // Genera el filtro AND con todos los filtros
    FiltroListaAnd filtroFinal = new FiltroListaAnd(todosLosFiltros);

    // Filtra los hechos de la colección
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
  public boolean contieneA(Hecho unHecho, GestorDeReportes gestor) {
    return this.getHechos(gestor).contains(unHecho);
  }
}

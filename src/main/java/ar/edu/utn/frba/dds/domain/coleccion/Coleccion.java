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
    this.filtro = new FiltroIdentidad();
  }

  /**
   * setter del filtro.
   */
  public void setFiltro(Filtro filtro) {
    this.filtro = filtro != null ? filtro : new FiltroIdentidad();
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
   * Obtiene los hechos filtrados aplicando el criterio propio y un filtro externo opcional.
   */
  public List<Hecho> getHechos(GestorDeReportes gestorDeReportes) {
    return filtrarHechos(gestorDeReportes.hechosEliminados()).filtrar(fuente.obtenerHechos());
  }

  private Filtro filtrarHechos(List<Hecho> hechosEliminados) {
    Filtro filtroFinal = filtro;
    Filtro filtroExclusion = new Filtro(hechos ->
      hechos.stream()
            .filter(h -> !hechosEliminados.contains(h))
            .toList()
    );
  //Si la lista no está vacia
  if (hechosEliminados != null && !hechosEliminados.isEmpty()) {
      filtroFinal = new FiltroListaAnd(List.of(filtro, filtroExclusion));
    }
    return filtroFinal;
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
  public boolean contieneA(Hecho unHecho, GestorDeReportes gestorDeReportes) {
    return this.getHechos(gestorDeReportes).contains(unHecho);
  }
}

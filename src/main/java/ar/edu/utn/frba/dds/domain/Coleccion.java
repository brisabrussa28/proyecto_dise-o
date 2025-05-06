package ar.edu.utn.frba.dds.domain;

import ar.edu.utn.frba.dds.domain.fuentes.Fuente;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Coleccion {
  private List<Hecho> hechos;
  private Fuente fuente;
  private String titulo;
  private String descripcion;
  private String categoria;
  //private String criterio;

  public Coleccion(String titulo/*, Fuente fuente*/, String descripcion, String categoria) {
    this.titulo = titulo;
    //this.fuente = fuente;
    this.descripcion = descripcion;
    this.hechos = new ArrayList<>();
    this.categoria = categoria;
  }

  public boolean cumpleCriterioDePertenencia(Hecho hecho) {
    return Objects.equals(hecho.getCategoria(), this.categoria);
  }

  public void agregarHecho(Hecho hecho) {
    if (!hechos.contains(hecho) && cumpleCriterioDePertenencia(hecho)) {
      hechos.add(hecho);
    }
  }

  public void eliminarHecho(Hecho hecho) {
    if(hechos.contains(hecho)){
      hechos.remove(hecho);
    }
  }

  public void agregarHechosPorCriterio(){ //Si toda colección está asociada a una fuente esta función podría realizarla también la fuente.
    //Paso 1: Buscar los hechos de la fuente que coincidan con el criterio
    //Paso 2: agregar todos los hechos a la colección
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

  //public String getCriterio(){return criterio; }
  //public void setCriterio(String criterio){this.criterio = criterio; } -- Todo criterio es configurable

  public List<Hecho> getHechos() {
    return hechos;
  }

  public boolean contieneFuente(Fuente unaFuente) {
    return fuente == unaFuente;
  }

  public boolean contieneA(Hecho unHecho) {
    return hechos.contains(unHecho);
  }
}


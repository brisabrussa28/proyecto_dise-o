package ar.edu.utn.frba.dds.domain;

import java.util.ArrayList;
import java.util.List;

public class Coleccion {
  private List<Hecho> hechos;
  private Fuente fuente;
  private String titulo;
  private String descripcion;
  //private String criterio;

  public Coleccion(String titulo, Fuente fuente, List<Hecho> hechos) {
    this.titulo = titulo;
    this.fuente = fuente;
    this.hechos = hechos;
  }

  public void agregarHecho(Hecho hecho) {
    if (!hechos.contains(hecho)) {
      hechos.add(hecho);
    }
  }

  public void agregarHechosPorCriterio(){ //Si toda colección está asociada a una fuente esta función podría realizarla también la fuente.
    //Paso 1: Buscar los hechos de la fuente que coincidan con el criterio
    //Paso 2: agregar todos los hechos a la colección
  }

  public String getTitulo(){return titulo; }
  public String getDescripcion(){return descripcion; }

  //public String getCriterio(){return criterio; }
  //public void setCriterio(String criterio){this.criterio = criterio; } -- Todo criterio es configurable

  public List<Hecho> getHechos() {
    return hechos;
  }

  public boolean contieneA(Hecho unHecho) {
    return hechos.contains(unHecho);
  }

}


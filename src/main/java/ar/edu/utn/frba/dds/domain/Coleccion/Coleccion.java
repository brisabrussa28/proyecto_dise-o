package ar.edu.utn.frba.dds.domain.Coleccion;

import ar.edu.utn.frba.dds.domain.fuentes.Fuente;
import ar.edu.utn.frba.dds.domain.hecho.Hecho;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/*
Las colecciones representan conjuntos de hechos. Las mismas pueden ser consultadas por cualquier persona,
de forma pública, y no pueden ser editadas ni eliminadas manualmente (esto último, con una sola excepción,
ver más adelante).

Las colecciones tienen un título, como por ejemplo “Desapariciones vinculadas a crímenes de odio”,
o “Incendios forestales en Argentina 2025” y una descripción. Las personas administradoras pueden crear
tantas colecciones como deseen.

Las colecciones están asociadas a una fuente y tomarán los hechos de las mismas: para esto las colecciones
también contarán con un criterio de pertenencia configurable, que dictará si un hecho pertenece o no a las mismas.
Por ejemplo, la colección de “Incendios forestales…” deberá incluir automáticamente todos los hechos de categoría
“Incendio forestal” ocurrido en Argentina, acontecido entre el 1 de enero de 2025 a las 0:00 y el 31 de diciembre
de 20205 a las 23:59.
 */

// coleccion parte de una fuente y con un criterio filtra la lista de hechos
// DEbe tener tipo de dato funcion o algo asi que devuelva un booleano y genere el criterio de pertenencia
// tambien debe tener un metodo filtrar que a partir de una funcion booleana devuelva una lista de hechos


public class Coleccion {
  private List<Hecho> hechos;
  private Fuente fuente;
  private String titulo;
  private String descripcion;
  private String categoria;
  //private String criterio;

  public Coleccion(String titulo, Fuente fuente, String descripcion, String categoria) {
    this.titulo = titulo;
    this.fuente = fuente;
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
    if (hechos.contains(hecho)) {
      hechos.remove(hecho);
    }
  }

  public void agregarHechosPorCriterio() { //Si toda colección está asociada a una fuente esta función podría realizarla también la fuente.
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


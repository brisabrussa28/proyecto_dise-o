package ar.edu.utn.frba.dds.domain;

import java.util.List;

public abstract class Fuente {
  protected String nombre;
  List<Hecho> hechos;

  public Fuente(String nombre, List<Hecho> hechos) {
    this.nombre = nombre;
    this.hechos = hechos;
  }

  public abstract List<Hecho> obtenerHechos();

  public String getNombre() {
    return nombre;
  }
}
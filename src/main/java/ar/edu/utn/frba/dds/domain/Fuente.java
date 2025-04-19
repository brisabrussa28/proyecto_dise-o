package ar.edu.utn.frba.dds.domain;

import java.util.List;

public abstract class Fuente {
  protected String nombre;

  public Fuente(String nombre) {
    this.nombre = nombre;
  }

  public abstract List<Hecho> obtenerHechos();

  public String getNombre() {
    return nombre;
  }
}
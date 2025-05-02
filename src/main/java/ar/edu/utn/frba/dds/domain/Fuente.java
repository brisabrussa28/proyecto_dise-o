package ar.edu.utn.frba.dds.domain;

import java.util.ArrayList;
import java.util.List;

public abstract class Fuente {
  protected String nombre;
  List<Hecho> hechos;

  public Fuente(String nombre, List<Hecho> hechos) {
    if (nombre == null || nombre.isEmpty()) {
      throw new IllegalArgumentException("El nombre de la fuente no puede ser nulo ni vacío.");
    }

    this.nombre = nombre;
    this.hechos = hechos == null ? new ArrayList<>() : hechos;
  }

  public abstract List<Hecho> obtenerHechos();

  public String getNombre() {
    return nombre;
  }
}
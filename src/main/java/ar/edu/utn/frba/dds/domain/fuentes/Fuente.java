package ar.edu.utn.frba.dds.domain.fuentes;

import ar.edu.utn.frba.dds.domain.filtro.FiltroDeId;
import ar.edu.utn.frba.dds.domain.hecho.Hecho;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Clase fuente.
 */
public abstract class Fuente {
  protected String nombre;

  /**
   * Constructor.
   */
  public Fuente(String nombre) {
    if (nombre == null || nombre.isEmpty()) {
      throw new IllegalArgumentException("El nombre de la fuente no puede ser nulo ni vacío.");
    }

    this.nombre = nombre;
  }
  /**
   * Funcion abstracta que devuelve lista de hechos.
   */
  public abstract List<Hecho> obtenerHechos();

  public String getNombre() {
    return nombre;
  }

  public boolean contiene(Hecho unHecho) {
    return obtenerHechos().contains(unHecho);
  }

}
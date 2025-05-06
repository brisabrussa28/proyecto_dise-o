package ar.edu.utn.frba.dds.domain.fuentes;

import ar.edu.utn.frba.dds.domain.Coleccion;
import ar.edu.utn.frba.dds.domain.Hecho;
import java.util.ArrayList;
import java.util.List;

public abstract class Fuente {
  protected String nombre;
  List<Hecho> hechos;
  List<Coleccion> colecciones;

  public Fuente(String nombre, List<Hecho> hechos) {
    if (nombre == null || nombre.isEmpty()) {
      throw new IllegalArgumentException("El nombre de la fuente no puede ser nulo ni vacío.");
    }

    this.nombre = nombre;
    this.hechos = hechos == null ? new ArrayList<>() : hechos;
  }

  public abstract List<Hecho> obtenerHechos();

  public void agregarColeccion(Coleccion coleccion) {
    this.colecciones.add(coleccion);
  }

  public void cargarHechos() {
    this.hechos = colecciones.stream()
        .flatMap(coleccion -> coleccion.getHechos().stream())
        .toList();
  }

  public String getNombre() {
    return nombre;
  }
}
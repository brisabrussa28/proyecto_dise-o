package ar.edu.utn.frba.dds.domain.fuentes;

import ar.edu.utn.frba.dds.domain.Coleccion.Coleccion;
import ar.edu.utn.frba.dds.domain.hecho.Hecho;
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

  public String getNombre() {
    return nombre;
  }

  public void eliminarHecho(Hecho hecho) {
    if (hecho == null) {
      throw new IllegalArgumentException("El hecho a eliminar no puede ser nulo.");
    }
    boolean eliminado = this.hechos.remove(hecho);
    if (!eliminado) {
      throw new IllegalStateException("El hecho no se encontró en la fuente: " + hecho.getTitulo());
    }
  }

}
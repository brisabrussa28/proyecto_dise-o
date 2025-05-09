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
  List<Hecho> hechos;

  /**
   * Constructor.
   */
  public Fuente(String nombre, List<Hecho> hechos) {
    if (nombre == null || nombre.isEmpty()) {
      throw new IllegalArgumentException("El nombre de la fuente no puede ser nulo ni vacío.");
    }

    this.nombre = nombre;
    this.hechos = hechos == null ? new ArrayList<>() : hechos;
  }

  /**
   * Funcion abstracta que devuelve lista de hechos.
   */
  public abstract List<Hecho> obtenerHechos();

  public String getNombre() {
    return nombre;
  }

  /**
   * Función que verifica que un hecho exista en la fuente y que no sea nulo.
   */
  public void eliminarHecho(UUID hechoId) {
    FiltroDeId filtro = new  FiltroDeId(hechoId);
    Hecho hecho = filtro.filtrar(this.obtenerHechos()).get(0);

    if (hecho == null) {
      throw new IllegalArgumentException("El hecho a eliminar no puede ser nulo.");
    }
    boolean eliminado = this.hechos.remove(hecho);
    if (!eliminado) {
      throw new IllegalStateException("El hecho no se encontró en la fuente: " + hecho.getTitulo());
    }
  }

}
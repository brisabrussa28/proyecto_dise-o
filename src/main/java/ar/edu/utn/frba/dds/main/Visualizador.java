package ar.edu.utn.frba.dds.main;

import ar.edu.utn.frba.dds.domain.coleccion.Coleccion;
import ar.edu.utn.frba.dds.domain.filtro.Filtro;
import ar.edu.utn.frba.dds.domain.fuentes.FuenteDinamica;
import ar.edu.utn.frba.dds.domain.hecho.Hecho;
import java.util.List;

/**
 * Visualizador.
 */
public class Visualizador extends Persona {

  /**
   * Constructor Visualizador.
   */
  public Visualizador(String nombre, String email) {
    super(nombre, email);
  }

  /**
   * Visualizar hechos.
   */
  public List<Hecho> visualizarHechos(Coleccion coleccion) {
    return coleccion.getHechos();
  }

  /**
   * Agrega un hecho a una fuente.
   */
  public void agregarHechoaFuente(FuenteDinamica fuente, Hecho hecho) {
    fuente.agregarHecho(hecho);
  }

  /**
   * Filtrar los hechos.
   */
  public List<Hecho> filtrar(Coleccion coleccion, Filtro filtro) {
    return filtro.filtrar(coleccion.getHechos());
  }

}

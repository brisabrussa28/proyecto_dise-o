package ar.edu.utn.frba.dds.model.lector.csv.filaconverter.model.serviciodevisualizacion;

import ar.edu.utn.frba.dds.model.coleccion.Coleccion;
import ar.edu.utn.frba.dds.model.filtro.Filtro;
import ar.edu.utn.frba.dds.model.hecho.Hecho;
import ar.edu.utn.frba.dds.model.reportes.GestorDeSolicitudes;
import java.util.List;


public class ServicioDeVisualizacion {

  /**
   * Obtiene la lista final de hechos visibles de una colección.
   * Aplica tanto el filtro de exclusión del repositorio como el filtro interno de la colección.
   *
   * @param coleccion         La colección de la cual se quieren obtener los hechos.
   * @param gestorDeReportes  El repositorio que provee el filtro de exclusión (ej. hechos eliminados).
   * @return Una lista de hechos filtrada y lista para ser visualizada.
   */
  public List<Hecho> obtenerHechosDeColeccion(
      Coleccion coleccion,
      GestorDeSolicitudes gestorDeReportes
  ) {
    Filtro filtroExcluyente = gestorDeReportes.filtroExcluyenteDeHechosEliminados();

    return coleccion.obtenerHechosFiltrados(filtroExcluyente);
  }

  /**
   * Obtiene los hechos de una colección y aplica un filtro adicional sobre el resultado.
   * Útil para cuando el usuario quiere aplicar un filtro dinámico en la interfaz.
   *
   * @param coleccion         La colección base.
   * @param filtroAdicional   Un filtro extra que se aplicará sobre los hechos ya filtrados.
   * @param gestorDeReportes  El repositorio que provee el filtro de exclusión.
   * @return Una lista de hechos filtrada por la colección y por el filtro adicional.
   */
  public List<Hecho> filtrarHechosDeColeccion(
      Coleccion coleccion,
      Filtro filtroAdicional,
      GestorDeSolicitudes gestorDeReportes
  ) {
    List<Hecho> hechosVisibles = obtenerHechosDeColeccion(coleccion, gestorDeReportes);

    return filtroAdicional.filtrar(hechosVisibles);
  }
}

package ar.edu.utn.frba.dds.domain.serviciodevisualizacion;

import ar.edu.utn.frba.dds.domain.coleccion.Coleccion;
import ar.edu.utn.frba.dds.domain.filtro.Filtro;
import ar.edu.utn.frba.dds.domain.hecho.Hecho;
import ar.edu.utn.frba.dds.domain.reportes.RepositorioDeSolicitudes;
import java.util.List;

/**
 * Permite visualizar los hechos de una coleccion.
 */
public class ServicioDeVisualizacion {

  //Obtener los hechos de una colección en especifico
  public List<Hecho> obtenerHechosColeccion(Coleccion coleccion, RepositorioDeSolicitudes gestorDeReportes) {
    return coleccion.getHechos(gestorDeReportes);
  }

  //Obtener los hechos de una coleccion segun un criterio
  public List<Hecho> filtrarHechosColeccion(Coleccion coleccion, Filtro filtro, RepositorioDeSolicitudes gestorDeReportes) {
    return filtro.filtrar(coleccion.getHechos(gestorDeReportes));
  }
}

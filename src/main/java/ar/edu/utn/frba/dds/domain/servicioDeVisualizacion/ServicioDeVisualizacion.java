package ar.edu.utn.frba.dds.domain.servicioDeVisualizacion;

import ar.edu.utn.frba.dds.domain.coleccion.Coleccion;
import ar.edu.utn.frba.dds.domain.filtro.Filtro;
import ar.edu.utn.frba.dds.domain.hecho.Hecho;
import ar.edu.utn.frba.dds.domain.reportes.GestorDeReportes;
import java.util.List;

public class ServicioDeVisualizacion {

  //Obtener los hechos de una colección en especifico
  public List<Hecho> obtenerHechosColeccion(Coleccion coleccion, GestorDeReportes gestorDeReportes) {
    return coleccion.getHechos(gestorDeReportes);
  }

  //Obtener los hechos de una coleccion segun un criterio
  public List<Hecho> filtrarHechosColeccion(Coleccion coleccion, Filtro filtro, GestorDeReportes gestorDeReportes) {
    return filtro.filtrar(coleccion.getHechos(gestorDeReportes));
  }
}

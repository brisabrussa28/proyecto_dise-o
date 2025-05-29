package ar.edu.utn.frba.dds.domain;

import ar.edu.utn.frba.dds.domain.coleccion.Coleccion;
import ar.edu.utn.frba.dds.domain.filtro.Filtro;
import ar.edu.utn.frba.dds.domain.hecho.Hecho;
import java.util.List;

public class ServicioDeVisualizacion {

  //Obtener los hechos de una colección en especifico
  public List<Hecho> obtenerHechosColeccion(Coleccion coleccion) {
    return coleccion.getHechos();
  }

  //Obtener los hechos de una coleccion segun un criterio
  public List<Hecho> filtrarHechosColeccion(Coleccion coleccion, Filtro filtro) {
    return filtro.filtrar(coleccion.getHechos());
  }
}

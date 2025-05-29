package ar.edu.utn.frba.dds.domain.fuentes;

import ar.edu.utn.frba.dds.domain.coleccion.Coleccion;
import ar.edu.utn.frba.dds.domain.filtro.Filtro;
import ar.edu.utn.frba.dds.domain.hecho.Hecho;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class FuenteDeVisualizacion extends Fuente{
  private final List<Fuente> fuentesCargadas;

  public FuenteDeVisualizacion(String nombre) {
    super(nombre, null);
    this.fuentesCargadas = new ArrayList<>();
  }

  //Obtiene todos los hechos de todas las fuentes
  @Override
  public List<Hecho> obtenerHechos() {
    return fuentesCargadas.stream()
        .flatMap(fuente -> fuente.obtenerHechos().stream())
        .collect(Collectors.toList());
  }

  public void agregarFuente(Fuente fuente) {
    this.fuentesCargadas.add(fuente);
  }

  //Obtener los hechos de una colección en especifico
  public List<Hecho> obtenerHechosColeccion(Coleccion coleccion) {
    return coleccion.getHechos();
  }

  //Obtener los hechos de una coleccion segun un criterio
  public List<Hecho> filtrarHechosColeccion(Coleccion coleccion, Filtro filtro) {
    return filtro.filtrar(coleccion.getHechos());
  }
}

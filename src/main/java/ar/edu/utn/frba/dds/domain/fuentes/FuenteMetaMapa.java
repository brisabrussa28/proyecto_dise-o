package ar.edu.utn.frba.dds.domain.fuentes;

import ar.edu.utn.frba.dds.domain.hecho.Hecho;
import ar.edu.utn.frba.dds.domain.hecho.HechoQuerys;
import ar.edu.utn.frba.dds.domain.serviciometamapa.ServicioMetaMapa;
import java.io.IOException;
import java.util.Collections;
import java.util.List;


public class FuenteMetaMapa extends FuenteProxy {
  private final ServicioMetaMapa servicio;
  private final HechoQuerys query;

  public FuenteMetaMapa(String nombre, ServicioMetaMapa servicio, HechoQuerys query) {
    super(nombre);
    this.servicio = servicio;
    this.query = query;
  }

  @Override
  public List<Hecho> obtenerHechos() {
    try {
      return servicio.listadoDeHechos(query)
                     .getHechos(); // Use the getter method to access the list
    } catch (IOException e) {
      System.err.println("Error al consultar MetaMapa: " + e.getMessage());
      return Collections.emptyList(); // Return an empty list in case of an error
    }
  }
}
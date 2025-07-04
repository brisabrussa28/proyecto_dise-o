package ar.edu.utn.frba.dds.domain.fuentes;

import ar.edu.utn.frba.dds.domain.hecho.Hecho;
import ar.edu.utn.frba.dds.domain.hecho.HechoQuerys;
import ar.edu.utn.frba.dds.domain.serviciometamapa.ServicioMetaMapa;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

public class FuenteMetaMapa implements Fuente {
  private final ServicioMetaMapa servicio;
  private final HechoQuerys query;
  private final String nombre;

  public FuenteMetaMapa(String nombre, ServicioMetaMapa servicio, HechoQuerys query) {
    this.validarFuente(nombre);
    this.nombre = nombre;
    this.servicio = servicio;
    this.query = query;
  }

  @Override
  public List<Hecho> obtenerHechos() {
    try {
      return servicio.listadoDeHechos(query);
    } catch (IOException e) {
      System.err.println("Error al consultar MetaMapa: " + e.getMessage());
      return Collections.emptyList();
    }
  }

  public String getNombre() {
    return nombre;
  }
}
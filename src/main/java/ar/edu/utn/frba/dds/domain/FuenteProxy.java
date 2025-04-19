package ar.edu.utn.frba.dds.domain;

import java.util.List;

public class FuenteProxy extends Fuente {

  public FuenteProxy(String nombre) {
    super(nombre);
  }

  @Override
  public List<Hecho> obtenerHechos() {
    // Simulación de integración con fuente externa
    return List.of(); // por ahora vacío
  }
}
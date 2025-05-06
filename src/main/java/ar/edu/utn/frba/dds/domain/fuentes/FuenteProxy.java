package ar.edu.utn.frba.dds.domain.fuentes;

import ar.edu.utn.frba.dds.domain.hecho.Hecho;
import java.util.List;

public class FuenteProxy extends Fuente {

  public FuenteProxy(String nombre) {
    super(nombre, null);
  }

  @Override
  public List<Hecho> obtenerHechos() {
    // Simulación de integración con fuente externa
    return List.of(); // por ahora vacío
  }
}
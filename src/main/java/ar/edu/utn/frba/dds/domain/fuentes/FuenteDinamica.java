package ar.edu.utn.frba.dds.domain.fuentes;

import ar.edu.utn.frba.dds.domain.hecho.Hecho;
import java.util.List;

public class FuenteDinamica extends Fuente {

  public FuenteDinamica(String nombre, List<Hecho> hechos) {
    super(nombre, hechos);
  }

  public void agregarHecho(Hecho hecho) {
    this.hechos.add(hecho);
  }

  @Override
  public List<Hecho> obtenerHechos() {
    return this.hechos;
  }
}
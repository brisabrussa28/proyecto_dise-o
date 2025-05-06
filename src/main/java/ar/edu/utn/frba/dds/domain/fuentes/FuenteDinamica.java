package ar.edu.utn.frba.dds.domain.fuentes;

import ar.edu.utn.frba.dds.domain.Hecho;
import java.util.List;

public class FuenteDinamica extends Fuente {
//  private List<Hecho> hechosCargados;
//  List<Hecho> hechos;

  public FuenteDinamica(String nombre, List<Hecho> hechos) {
    super(nombre, hechos);
    //this.hechosCargados = new ArrayList<>();
  }

  public void agregarHecho(Hecho hecho) {
    this.hechos.add(hecho);
  }

  @Override
  public List<Hecho> obtenerHechos() {
    return this.hechos;
  }
}
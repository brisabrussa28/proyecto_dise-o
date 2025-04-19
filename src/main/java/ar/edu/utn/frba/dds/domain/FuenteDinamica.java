package ar.edu.utn.frba.dds.domain;

import java.util.ArrayList;
import java.util.List;

public class FuenteDinamica extends Fuente {
  private List<Hecho> hechosCargados;

  public FuenteDinamica(String nombre) {
    super(nombre);
    this.hechosCargados = new ArrayList<>();
  }

  public void agregarHecho(Hecho hecho) {
    hechosCargados.add(hecho);
  }

  @Override
  public List<Hecho> obtenerHechos() {
    return hechosCargados;
  }
}
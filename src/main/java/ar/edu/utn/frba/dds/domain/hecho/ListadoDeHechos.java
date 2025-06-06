package ar.edu.utn.frba.dds.domain.hecho;

import java.util.ArrayList;
import java.util.List;

public class ListadoDeHechos {
  private List<Hecho> hechos = new ArrayList<>();

  public List<Hecho> getHechos() {
    return new ArrayList<>(hechos); // Return a copy to ensure immutability
  }

  public void addHecho(Hecho hecho) {
    this.hechos.add(hecho);
  }
}
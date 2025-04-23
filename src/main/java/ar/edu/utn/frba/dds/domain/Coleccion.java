package ar.edu.utn.frba.dds.domain;

import java.util.List;

public class Coleccion {
  private List<Hecho> hechos;
  private Fuente fuente;
  private String titulo;

  public Coleccion(String titulo, Fuente fuente, List<Hecho> hechos) {
    this.titulo = titulo;
    this.fuente = fuente;
    this.hechos = hechos;
  }

  public void agregarHecho(Hecho hecho) {
    if (!hechos.contains(hecho)) {
      hechos.add(hecho);
    }
  }



  public List<Hecho> getHechos() {
    return hechos;
  }

  public boolean contieneA(Hecho unHecho) {
    return hechos.contains(unHecho);
  }
}

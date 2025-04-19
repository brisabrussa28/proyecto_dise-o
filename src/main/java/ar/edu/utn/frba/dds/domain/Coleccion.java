package ar.edu.utn.frba.dds.domain;

import java.util.List;

public class Coleccion {
  List<Hecho> hechos;
  String titulo;

  public Coleccion(String titulo, List<Hecho> hechos) {
    this.titulo = titulo;
    this.hechos = hechos;
  }

  boolean contieneA(Hecho unHecho) {
    return hechos.contains(unHecho);
  }
}

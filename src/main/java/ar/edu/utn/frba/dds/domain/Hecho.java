package ar.edu.utn.frba.dds.domain;

import java.time.LocalDate;

public class Hecho {
  String titulo;
  String descripcion;
  //contenidoMultimedia (opcional)
  String lugar;
  LocalDate fecha;
  //origen -> Si viene de un data set o de un contribuyente.

  public Hecho() {

  }

  boolean perteneceA(Coleccion unaColeccion) {
    return unaColeccion.contieneA(this);
  }
}

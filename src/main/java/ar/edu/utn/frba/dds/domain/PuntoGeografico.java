package ar.edu.utn.frba.dds.domain;

public class PuntoGeografico {
  private double latitud;
  private double longitud;

  public PuntoGeografico(double latitud, double longitud) {
    this.latitud = latitud;
    this.longitud = longitud;
  }

  public double getLatitud() {
    return latitud;
  }
  public double getLongitud() {
    return longitud;
  }
}
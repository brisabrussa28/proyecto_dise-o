package ar.edu.utn.frba.dds.domain.info;

/**
 * Punto Geografico.
 * */
public class PuntoGeografico {
  private double latitud;
  private double longitud;

  /**
   * Constructor Punto Geográfico.
   */
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
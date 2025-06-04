package ar.edu.utn.frba.dds.domain.info;

/**
 * Punto Geografico.
 */
public class PuntoGeografico {
  private final double latitud;
  private final double longitud;

  /**
   * Constructor de PuntoGeografico.
   *
   * @param latitud  Latitud del punto geográfico
   * @param longitud Longitud del punto geográfico
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
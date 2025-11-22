package ar.edu.utn.frba.dds.model.info;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Embeddable;

/**
 * Punto Geografico.
 */
@Embeddable
public class PuntoGeografico {
  @Column(name = "hecho_latitud")
  private double latitud;
  @Column(name = "hecho_longitud")
  private double longitud;

  /**
   * Constructor de PuntoGeografico que valida las coordenadas.
   *
   * @param latitud  Latitud del punto geográfico (debe estar entre -90 y 90).
   * @param longitud Longitud del punto geográfico (debe estar entre -180 y 180).
   * @throws IllegalArgumentException si las coordenadas están fuera de rango.
   */
  @JsonCreator
  public PuntoGeografico(
      @JsonProperty("latitud") double latitud,
      @JsonProperty("longitud") double longitud
  ) {
    if (latitud < -90 || latitud > 90) {
      throw new IllegalArgumentException("La latitud debe estar entre -90 y 90.");
    }
    if (longitud < -180 || longitud > 180) {
      throw new IllegalArgumentException("La longitud debe estar entre -180 y 180.");
    }
    this.latitud = latitud;
    this.longitud = longitud;
  }

  public PuntoGeografico() { }

  public double getLatitud() {
    return latitud;
  }

  public double getLongitud() {
    return longitud;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof PuntoGeografico that)) {
      return false;
    }
    return Double.compare(that.latitud, latitud) == 0
        && Double.compare(that.longitud, longitud) == 0;
  }

  @Override
  public int hashCode() {
    return Objects.hash(latitud, longitud);
  }
}

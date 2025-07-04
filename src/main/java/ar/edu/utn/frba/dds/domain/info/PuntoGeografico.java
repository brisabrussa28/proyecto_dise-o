package ar.edu.utn.frba.dds.domain.info;

import com.fasterxml.jackson.annotation.JsonCreator; // Import para @JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty; // Import para @JsonProperty
import java.util.Objects;

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
  @JsonCreator
  public PuntoGeografico(
      @JsonProperty("latitud") double latitud,
      @JsonProperty("longitud") double longitud) {
    this.latitud = latitud;
    this.longitud = longitud;
  }

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

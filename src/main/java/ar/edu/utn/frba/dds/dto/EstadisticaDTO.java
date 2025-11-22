package ar.edu.utn.frba.dds.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class EstadisticaDTO {
  @JsonProperty
  private Long estadistica_coleccion;
  @JsonProperty
  private String estadistica_categoria;
  @JsonProperty
  private String estadistica_tipo;

  public Long getColeccion() {
    return this.estadistica_coleccion;
  }

  public String getTipo() {
    return this.estadistica_tipo;
  }

  public String getCategoria() {
    return this.estadistica_categoria;
  }
}

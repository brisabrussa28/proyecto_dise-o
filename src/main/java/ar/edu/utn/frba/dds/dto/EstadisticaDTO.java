package ar.edu.utn.frba.dds.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class EstadisticaDTO {

  @JsonProperty("coleccion")
  private Long coleccion;
  @JsonProperty("categoria")
  private String categoria;
  @JsonProperty("tipo")
  private String tipo;

  public EstadisticaDTO(String tipo, String categoria, Long coleccion) {
    this.tipo = tipo;
    this.categoria = categoria;
    this.coleccion = coleccion;
  }

  public Long getColeccion() {
    return coleccion;
  }
  public String getCategoria() {
    return categoria;
  }
  public String getTipo() {
    return tipo;
  }
}
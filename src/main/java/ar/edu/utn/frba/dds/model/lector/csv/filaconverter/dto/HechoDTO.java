package ar.edu.utn.frba.dds.model.lector.csv.filaconverter.dto;

import ar.edu.utn.frba.dds.model.hecho.etiqueta.Etiqueta;
import ar.edu.utn.frba.dds.model.info.PuntoGeografico;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;
import java.util.List;

public class HechoDTO {
  @JsonProperty
  private String hecho_titulo;
  @JsonProperty
  private String hecho_descripcion;
  @JsonProperty
  private List<Etiqueta> hecho_etiquetas;
  @JsonProperty
  private PuntoGeografico hecho_ubicacion;
  @JsonProperty
  private LocalDateTime hecho_fecha_suceso;

  public String getTitulo() {
    return this.hecho_titulo;
  }

  public String getDescripcion() {
    return this.hecho_descripcion;
  }

  public List<Etiqueta> getEtiquetas() {
    return this.hecho_etiquetas;
  }

  public PuntoGeografico getUbicacion() {
    return this.hecho_ubicacion;
  }

  public LocalDateTime getFechaSuceso() {
    return this.hecho_fecha_suceso;
  }
}

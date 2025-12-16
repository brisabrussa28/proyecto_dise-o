package ar.edu.utn.frba.dds.dto;

public class SolicitudDTO {
  public Long hecho_solicitado;
  public String motivo_solicitud;

  public String getMotivo() {
    return this.motivo_solicitud;
  }
}
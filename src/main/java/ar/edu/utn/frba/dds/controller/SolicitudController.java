package ar.edu.utn.frba.dds.controller;

import ar.edu.utn.frba.dds.dto.SolicitudDTO;
import ar.edu.utn.frba.dds.model.hecho.Hecho;
import ar.edu.utn.frba.dds.model.reportes.EstadoSolicitud;
import ar.edu.utn.frba.dds.model.reportes.Solicitud;
import ar.edu.utn.frba.dds.repositories.HechoRepository;
import ar.edu.utn.frba.dds.repositories.SolicitudesRepository;

public class SolicitudController {
  public Solicitud crearSolicitud(SolicitudDTO solicitudDTO) {
    Hecho hecho = HechoRepository.instance()
                                 .findById(solicitudDTO.hecho_solicitado);
    Solicitud solicitud = new Solicitud(hecho, solicitudDTO.motivo_solicitud);
    SolicitudesRepository.instance()
                         .guardar(solicitud);
    return solicitud;
  }

  public void aceptar(Solicitud solicitud) {
    if (solicitud.getEstado() != EstadoSolicitud.PENDIENTE) {
      throw new RuntimeException("La ya ha sido analizada");
    }
    SolicitudesRepository.instance()
                         .aceptarSolicitud(solicitud);
  }

  public void rechazar(Solicitud solicitud) {
    if (solicitud.getEstado() != EstadoSolicitud.PENDIENTE) {
      throw new RuntimeException("La ya ha sido analizada");
    }
    SolicitudesRepository.instance()
                         .rechazarSolicitud(solicitud);
  }
}

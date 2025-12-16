package ar.edu.utn.frba.dds.controller;

import ar.edu.utn.frba.dds.dto.SolicitudDTO;
import ar.edu.utn.frba.dds.model.hecho.Hecho;
import ar.edu.utn.frba.dds.model.reportes.EstadoSolicitud;
import ar.edu.utn.frba.dds.model.reportes.GestorDeSolicitudes;
import ar.edu.utn.frba.dds.model.reportes.Solicitud;
import ar.edu.utn.frba.dds.model.usuario.Usuario;
import ar.edu.utn.frba.dds.repositories.HechoRepository;
import ar.edu.utn.frba.dds.repositories.SolicitudesRepository;
import ar.edu.utn.frba.dds.repositories.UserRepository;
import io.javalin.http.Context;
import java.util.Map;

public class SolicitudController {
  public Solicitud crearSolicitud(Context ctx, SolicitudDTO solicitudDTO) {
    Hecho hecho = HechoRepository.instance()
                                 .findById(solicitudDTO.hecho_solicitado);

    Long usuarioId = ctx.sessionAttribute("usuario_id");
    Usuario usuario = UserRepository.instance()
                                    .findById(usuarioId);

    if (usuario == null) {
      ctx.status(401)
         .result("Debes iniciar sesión");
      return null;
    }
    GestorDeSolicitudes gestor = new GestorDeSolicitudes(SolicitudesRepository.instance());
    Solicitud soli = gestor.crearSolicitud(hecho, solicitudDTO.getMotivo(), usuario);
    ctx.status(201)
       .json(Map.of("mensaje", "Solicitud creada"));
    return soli;
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

  public Long countAll() {
    return SolicitudesRepository.instance()
                                .cantidadTotal();
  }
}

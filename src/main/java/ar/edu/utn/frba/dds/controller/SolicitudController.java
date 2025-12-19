package ar.edu.utn.frba.dds.controller;

import ar.edu.utn.frba.dds.dto.SolicitudDTO;
import ar.edu.utn.frba.dds.model.hecho.Hecho;
import ar.edu.utn.frba.dds.model.reportes.AceptarSolicitud;
import ar.edu.utn.frba.dds.model.reportes.GestorDeSolicitudes;
import ar.edu.utn.frba.dds.model.reportes.Solicitud;
import ar.edu.utn.frba.dds.model.usuario.Usuario;
import ar.edu.utn.frba.dds.repositories.HechoRepository;
import ar.edu.utn.frba.dds.repositories.SolicitudesRepository;
import ar.edu.utn.frba.dds.repositories.UserRepository;
import io.javalin.http.Context;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import ar.edu.utn.frba.dds.model.exceptions.RazonInvalidaException;

public class SolicitudController {

  private final GestorDeSolicitudes gestor;

  public SolicitudController() {
    this.gestor = new GestorDeSolicitudes();
  }

  // --- CREAR ---
  public Solicitud crearSolicitud(Context ctx, SolicitudDTO solicitudDTO) {
    Hecho hecho = HechoRepository.instance().findById(solicitudDTO.hecho_solicitado);

    Long usuarioId = ctx.sessionAttribute("usuario_id");
    Usuario usuario = UserRepository.instance().findById(usuarioId);

    if (usuario == null) {
      ctx.status(401).result("Debes iniciar sesión");
      return null;
    }

    return gestor.crearSolicitud(hecho, solicitudDTO.getMotivo(), usuario);
  }

  // --- LISTAR PENDIENTES (ADMIN) ---
  public void listarPendientes(Context ctx, Map<String, Object> model) {
    List<Solicitud> pendientes = gestor.getSolicitudesPendientes();
    model.put("solicitudes", pendientes);
    ctx.render("solicitudes.hbs", model);
  }

  // --- LISTAR MIS SOLICITUDES (USUARIO) ---
  public void listarMisSolicitudes(Context ctx, Map<String, Object> model) {
    Long usuarioId = ctx.sessionAttribute("usuario_id");

    List<Solicitud> misSolicitudes = SolicitudesRepository.instance().findAll().stream()
                                                          .filter(s -> s.getUsuario() != null && s.getUsuario().getId().equals(usuarioId))
                                                          .sorted((a, b) -> b.getId().compareTo(a.getId()))
                                                          .collect(Collectors.toList());

    model.put("solicitudes", misSolicitudes);
    ctx.render("solicitudes-info.hbs", model);
  }

  // --- RESOLVER (ACEPTAR/RECHAZAR/SPAM) ---
  public void procesarResolucion(Context ctx) {
    String idStr = ctx.queryParam("id");
    // Esperamos el texto del Enum: "ACEPTAR", "RECHAZAR" o "SPAM"
    String decisionStr = ctx.queryParam("decision");

    if (idStr == null || decisionStr == null) {
      ctx.status(400).result("Faltan parámetros: id o decision (ACEPTAR/RECHAZAR/SPAM)");
      return;
    }

    try {
      Long id = Long.parseLong(idStr);

      AceptarSolicitud decision = AceptarSolicitud.valueOf(decisionStr.toUpperCase());

      Solicitud solicitud = SolicitudesRepository.instance().findById(id);
      if (solicitud == null) {
        ctx.status(404).result("La solicitud no existe.");
        return;
      }

      gestor.gestionarSolicitud(solicitud, decision);

      ctx.status(200).result("Solicitud procesada correctamente como " + decision);

    } catch (NumberFormatException e) {
      ctx.status(400).result("El ID debe ser un número válido.");
    } catch (IllegalArgumentException e) {
      ctx.status(400).result("Decisión inválida. Valores permitidos: ACEPTAR, RECHAZAR.");
    } catch (Exception e) {
      ctx.status(400).result("Error: " + e.getMessage());
    }
  }

  public Long countAll() {
    return SolicitudesRepository.instance().cantidadTotal();
  }
}
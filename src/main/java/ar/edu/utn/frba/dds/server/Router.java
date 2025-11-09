package ar.edu.utn.frba.dds.server;

import ar.edu.utn.frba.dds.controller.ColeccionController;
import ar.edu.utn.frba.dds.controller.HechoController;
import ar.edu.utn.frba.dds.controller.HomeController;
import ar.edu.utn.frba.dds.controller.SolicitudController;
import ar.edu.utn.frba.dds.dto.ColeccionDTO;
import ar.edu.utn.frba.dds.dto.SolicitudDTO;
import ar.edu.utn.frba.dds.model.coleccion.Coleccion;
import ar.edu.utn.frba.dds.model.exceptions.RazonInvalidaException;
import ar.edu.utn.frba.dds.model.hecho.Hecho;
import ar.edu.utn.frba.dds.model.reportes.Solicitud;
import ar.edu.utn.frba.dds.repositories.SolicitudesRepository;
import io.javalin.Javalin;

public class Router {
  public void configure(Javalin app) {
    HomeController controller = new HomeController();
    HechoController hechoController = new HechoController();
    SolicitudController solicitudController = new SolicitudController();
    ColeccionController coleccionController = new ColeccionController();

    app.get("/", ctx -> ctx.result("BENE"));
    app.get(
        "/hechos/{id}", ctx -> {
          Long id = Long.parseLong(ctx.pathParam("id"));
          Hecho hecho = hechoController.findById(id);
          if (hecho == null) {
            ctx.status(404);
            ctx.result("Not Found");
          } else {
            ctx.json(hecho);
          }
        }
    );
    app.post(
        "/hechos", ctx -> {
          Hecho hecho = hechoController.subirHecho(ctx.bodyAsClass(Hecho.class));
          ctx.status(201);
          ctx.json(hecho);
        }
    );
    app.post(
        "/solicitudes", ctx -> {
          try {
            Solicitud solicitud = solicitudController.crearSolicitud(ctx.bodyAsClass(SolicitudDTO.class));
            ctx.status(201);
            ctx.json(solicitud);
          } catch (RazonInvalidaException e) {
            ctx.status(400);
            ctx.result("Argumento inválido: " + e.getMessage());
          }
        }
    );
    app.post(
        "/colecciones", ctx -> {
          try {
            Coleccion coleccion = coleccionController.crearColeccion(ctx.bodyAsClass(ColeccionDTO.class));
            ctx.status(201);
            ctx.json(coleccion);
          } catch (RuntimeException e) {
            ctx.status(400);
            ctx.result(e.getMessage());
          }
        }
    );
    app.put(
        "/solicitudes", ctx -> {
          String idParam = ctx.queryParam("id");
          String aceptadaParam = ctx.queryParam("aceptada");

          if (idParam == null || aceptadaParam == null) {
            ctx.status(400);
            ctx.result("Falta campo id o aceptada");
            return;
          }

          Long id = Long.parseLong(idParam);
          Boolean aceptada = Boolean.parseBoolean(aceptadaParam);
          Solicitud soli = SolicitudesRepository.instance()
                                                .findById(id);
          try {
            if (soli == null) {
              ctx.status(404);
              ctx.result("No existe una solicitud con ese id");
            } else if (aceptada) {
              //soli.aceptar();
              solicitudController.aceptar(soli);
              ctx.status(200);
              ctx.json(soli);
            } else {
              //soli.rechazar();
              solicitudController.rechazar(soli);
              ctx.status(200);
              ctx.json(soli);
            }
          } catch (RuntimeException e) {
            ctx.status(400);
            ctx.result("La solicitud ya ha sido analizada");
          }
        }
    );
  }
}

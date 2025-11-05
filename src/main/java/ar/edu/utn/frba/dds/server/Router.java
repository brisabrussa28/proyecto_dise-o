package ar.edu.utn.frba.dds.server;

import ar.edu.utn.frba.dds.controller.HechoController;
import ar.edu.utn.frba.dds.controller.HomeController;
import ar.edu.utn.frba.dds.model.hecho.Hecho;
import io.javalin.Javalin;

public class Router {
  public void configure(Javalin app) {
    HomeController controller = new HomeController();
    HechoController hechocon = new HechoController();
    app.get("/", ctx -> ctx.render("layout.hbs"));
    //app.get("/hechos", context -> context.json(hechocon.findAny()));
    app.get(
        "/hechos/{id}", ctx -> {
          ctx.json(hechocon.findById(Long.parseLong(ctx.pathParam("id"))));
        }
    );
    app.post(
        "/hechos", ctx -> {
          hechocon.subirHecho(ctx.bodyAsClass(Hecho.class));
          ctx.status(201);
        }
    );
  }
}

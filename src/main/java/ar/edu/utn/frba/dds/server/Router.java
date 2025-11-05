package ar.edu.utn.frba.dds.server;

import ar.edu.utn.frba.dds.controller.HechoController;
import ar.edu.utn.frba.dds.controller.HomeController;
import io.javalin.Javalin;

public class Router {
  public void configure(Javalin app) {
    HomeController controller = new HomeController();
    HechoController hechocon = new HechoController();

    app.get("/", ctx -> ctx.render("layout.hbs"));
    app.get("/hechos", context -> context.json(hechocon.findAny()));
  }
}

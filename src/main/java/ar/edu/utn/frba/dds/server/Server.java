package ar.edu.utn.frba.dds.server;

import ar.edu.utn.frba.dds.server.templates.JavalinHandlebars;
import ar.edu.utn.frba.dds.server.templates.JavalinRenderer;
import io.javalin.Javalin;

public class Server {

  public static void main(String[] args) {

    Javalin app = Javalin.create(config -> {
      // CONFIGURAR EL MOTOR DE PLANTILLAS (Handlebars)
      JavalinRenderer customRenderer = new JavalinRenderer();
      customRenderer.register("hbs", new JavalinHandlebars());
      config.fileRenderer(customRenderer);

      // CONFIGURAR ARCHIVOS ESTÁTICOS
      config.staticFiles.add("/public");
    });

    // CONFIGURAR RUTAS
    new Router().configure(app);

    // CARGAR DATOS INICIALES (Bootstrap)
    new Bootstrap().init();

    // INICIAR EL SERVIDOR
    System.out.println("Iniciando servidor en http://localhost:7070");
    app.start(7070);
  }
}

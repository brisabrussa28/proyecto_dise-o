package ar.edu.utn.frba.dds.server;

import ar.edu.utn.frba.dds.server.templates.JavalinHandlebars;
import ar.edu.utn.frba.dds.server.templates.JavalinRenderer;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.javalin.Javalin;
import io.javalin.json.JavalinJackson;

public class Server {

  public static void main(String[] args) {
    //new Bootstrap().init();
    var app = Javalin.create(javalinConfig -> {
      javalinConfig.jsonMapper(new JavalinJackson().updateMapper(objectMapper -> {
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        objectMapper.registerModule(new Jdk8Module());
      }));
      javalinConfig.fileRenderer(new JavalinRenderer().register("hbs", new JavalinHandlebars()));
      javalinConfig.staticFiles.add("/public");
      javalinConfig.bundledPlugins.enableCors(cors -> {
        cors.addRule(it -> {
          it.allowHost("http://localhost:3000");
        });
      });
      javalinConfig.staticFiles.add("/public");
    });

    // CONFIGURAR RUTAS
    new Router().configure(app);

    // CARGAR DATOS INICIALES (Bootstrap)
    new Bootstrap().init();

    // INICIAR EL SERVIDOR
    System.out.println("Iniciando servidor en http://localhost:9001");
    app.start(9001);
  }
}

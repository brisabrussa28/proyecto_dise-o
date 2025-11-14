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
    var app = Javalin.create(javalinConfig -> {
      // Configurar Jackson para JSON
      javalinConfig.jsonMapper(new JavalinJackson().updateMapper(objectMapper -> {
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        objectMapper.registerModule(new Jdk8Module());
      }));

      // Configurar motor de plantillas Handlebars
      javalinConfig.fileRenderer(new JavalinRenderer().register("hbs", new JavalinHandlebars()));

      // Configurar archivos estáticos
      javalinConfig.staticFiles.add("/public");

      // Configurar CORS
      javalinConfig.bundledPlugins.enableCors(cors -> {
        cors.addRule(it -> {
          it.allowHost("http://localhost:3000");
        });
      });

      // Habilitar sesiones (esto es lo que faltaba)
      javalinConfig.jetty.modifyServletContextHandler(handler -> {
        handler.setSessionHandler(new org.eclipse.jetty.server.session.SessionHandler());
      });
    });

    // Configurar rutas
    new Router().configure(app);

    // Cargar datos iniciales (Bootstrap)
    new Bootstrap().init();

    // Iniciar el servidor
    System.out.println("Iniciando servidor en http://localhost:9001");
    app.start(9001);
  }
}

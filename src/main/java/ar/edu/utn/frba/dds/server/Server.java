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
      javalinConfig.jsonMapper(new JavalinJackson().updateMapper(objectMapper -> {
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        objectMapper.registerModule(new Jdk8Module());
      }));
      javalinConfig.fileRenderer(new JavalinRenderer().register("hbs", new JavalinHandlebars()));
      javalinConfig.staticFiles.add("/public");
    });
    new Router().configure(app);
    app.start(9001);
  }
}

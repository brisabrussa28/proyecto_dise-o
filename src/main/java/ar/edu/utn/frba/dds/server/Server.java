package ar.edu.utn.frba.dds.server;

import ar.edu.utn.frba.dds.server.templates.JavalinHandlebars;
import ar.edu.utn.frba.dds.server.templates.JavalinRenderer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.javalin.Javalin;
import io.javalin.http.staticfiles.Location;
import io.javalin.json.JavalinJackson;
import java.util.TimeZone;

public class Server {

  public static void main(String[] args) {

    TimeZone.setDefault(TimeZone.getTimeZone("America/Argentina/Buenos_Aires"));

    ObjectMapper mapper = new ObjectMapper();
    mapper.registerModule(new JavaTimeModule());
    mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    mapper.registerModule(new Jdk8Module());
    Javalin app = Javalin.create(javalinConfig -> {
      javalinConfig.jsonMapper(new JavalinJackson());
      javalinConfig.fileRenderer(new JavalinRenderer().register("hbs", new JavalinHandlebars()));
      javalinConfig.staticFiles.add("/public", Location.CLASSPATH);
      javalinConfig.bundledPlugins.enableCors(cors -> {
        cors.addRule(it -> {
          it.allowHost("http://localhost:3000");
          it.allowCredentials = true;
        });
      });
    });
    // Configurar rutas
    new Router().configure(app, mapper);

    // Cargar datos iniciales (Bootstrap)
    new Bootstrap().init();

    // Iniciar el servidor
    System.out.println("Iniciando servidor en http://localhost:9001");
    app.start(9001);
  }
}

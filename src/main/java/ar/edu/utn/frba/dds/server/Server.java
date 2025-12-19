package ar.edu.utn.frba.dds.server;
import ar.edu.utn.frba.dds.server.templates.JavalinHandlebars;
import ar.edu.utn.frba.dds.server.templates.JavalinRenderer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.github.cdimascio.dotenv.Dotenv;
import io.javalin.Javalin;
import io.javalin.http.staticfiles.Location;
import io.javalin.json.JavalinJackson;
import java.util.Arrays;
import java.util.List;
import java.util.TimeZone;

public class Server {
  public static void main(String[] args) {
    Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();
    String timezone = dotenv.get("TIMEZONE", "America/Argentina/Buenos_Aires");
    int port = Integer.parseInt(dotenv.get("PORT", "9001"));
    boolean corsAllowAnyOrigin = Boolean.parseBoolean(dotenv.get("CORS_ALLOW_ANY_ORIGIN", "false"));
    String allowedOriginsStr = dotenv.get("ALLOWED_ORIGINS", "http://localhost:3000");
    boolean debugMode = Boolean.parseBoolean(dotenv.get("DEBUG_MODE", "true"));

    TimeZone.setDefault(TimeZone.getTimeZone(timezone));

    ObjectMapper mapper = new ObjectMapper();
    mapper.registerModule(new JavaTimeModule());
    mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    mapper.registerModule(new Jdk8Module());

    Javalin app = Javalin.create(javalinConfig -> {
      javalinConfig.jsonMapper(new JavalinJackson());

      javalinConfig.fileRenderer(new JavalinRenderer().register("hbs", new JavalinHandlebars()));

      javalinConfig.staticFiles.add(staticFiles -> {
        staticFiles.hostedPath = "/";
        staticFiles.directory = "/public";
        staticFiles.location = Location.CLASSPATH;
        staticFiles.precompress = false;
        staticFiles.skipFileFunction = req -> false;
      });

      if (debugMode) {
        javalinConfig.router.ignoreTrailingSlashes = true;
      }

      javalinConfig.bundledPlugins.enableCors(cors -> {
        cors.addRule(it -> {
          if (corsAllowAnyOrigin) {
            it.anyHost();
          } else {
            List<String> origins = Arrays.stream(allowedOriginsStr.split(","))
                                         .map(String::trim)
                                         .filter(s -> !s.isEmpty())
                                         .toList();
            if (!origins.isEmpty()) {
              String firstOrigin = origins.get(0);
              String[] otherOrigins = origins.subList(1, origins.size()).toArray(new String[0]);
              it.allowHost(firstOrigin, otherOrigins);
            }
          }
          it.allowCredentials = true;
        });
      });
    });

    new Router().configure(app, mapper, debugMode);

    System.out.println("Iniciando servidor en puerto " + port);
    System.out.println("Modo debug: " + (debugMode ? "HABILITADO" : "DESHABILITADO"));
    if (corsAllowAnyOrigin) {
      System.out.println("CORS: Permitido CUALQUIER origen (Modo Inseguro/Dev)");
    } else {
      System.out.println("CORS: Restringido a los orígenes: " + allowedOriginsStr);
    }

    app.start(port);

    new EstadisticasScheduler().iniciar();
  }
}
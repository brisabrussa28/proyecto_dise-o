package ar.edu.utn.frba.dds.model.lector.csv.filaconverter.server;
import ar.edu.utn.frba.dds.server.EstadisticasScheduler;
import ar.edu.utn.frba.dds.server.Router;
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
    // Cargar configuración de entorno
    Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();
    String timezone = dotenv.get("TIMEZONE", "America/Argentina/Buenos_Aires");
    int port = Integer.parseInt(dotenv.get("PORT", "9001"));
    boolean corsAllowAnyOrigin = Boolean.parseBoolean(dotenv.get("CORS_ALLOW_ANY_ORIGIN", "false"));
    String allowedOriginsStr = dotenv.get("ALLOWED_ORIGINS", "http://localhost:3000");
    boolean debugMode = Boolean.parseBoolean(dotenv.get("DEBUG_MODE", "true"));

    // Configurar TimeZone por defecto para toda la JVM
    TimeZone.setDefault(TimeZone.getTimeZone(timezone));

    // --- CONFIGURACIÓN CENTRALIZADA DEL MAPPER ---
    ObjectMapper mapper = new ObjectMapper();
    mapper.registerModule(new JavaTimeModule());
    mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    mapper.registerModule(new Jdk8Module());

    Javalin app = Javalin.create(javalinConfig -> {
      // 1. IMPORTANTE: Usar el mapper configurado arriba para APIs JSON
      javalinConfig.jsonMapper(new JavalinJackson());

      // 2. Registrar el motor de plantillas
      javalinConfig.fileRenderer(new JavalinRenderer().register("hbs", new JavalinHandlebars()));

      // Configurar archivos estáticos
      javalinConfig.staticFiles.add(staticFiles -> {
        staticFiles.hostedPath = "/";
        staticFiles.directory = "/public";
        staticFiles.location = Location.CLASSPATH;
        staticFiles.precompress = false;
        staticFiles.skipFileFunction = req -> false;
      });

      // Habilitar registro de rutas para debugging
      if (debugMode) {
        javalinConfig.router.ignoreTrailingSlashes = true;
      }

      // Configuración de CORS
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

    // Configurar rutas
    new Router().configure(app, mapper, debugMode);

    // Iniciar el servidor
    System.out.println("Iniciando servidor en puerto " + port);
    System.out.println("Modo debug: " + (debugMode ? "HABILITADO" : "DESHABILITADO"));
    if (corsAllowAnyOrigin) {
      System.out.println("CORS: Permitido CUALQUIER origen (Modo Inseguro/Dev)");
    } else {
      System.out.println("CORS: Restringido a los orígenes: " + allowedOriginsStr);
    }

    app.start(port);

    // Iniciar scheduler de estadísticas
    new EstadisticasScheduler().iniciar();
  }
}
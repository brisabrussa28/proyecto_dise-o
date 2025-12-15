package ar.edu.utn.frba.dds.server.templates;

import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Options;
import com.github.jknack.handlebars.Template;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import io.javalin.rendering.FileRenderer;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;

public class JavalinHandlebars implements FileRenderer {

  private final Handlebars handlebars;

  public JavalinHandlebars() {
    this.handlebars = new Handlebars();

    // --- REGISTRO DE HELPERS (Una sola vez al inicio) ---

    // 1. Helper "eq": Compara si dos valores son iguales (maneja Strings, Longs, Enums)
    this.handlebars.registerHelper("eq", (Object a, Options options) -> {
      Object b = options.param(0);
      return Objects.equals(a, b) || (a != null && b != null && a.toString().equals(b.toString()));
    });

    // 2. Helper "gt": Mayor que
    this.handlebars.registerHelper("gt", (Object a, Options options) -> {
      Object b = options.param(0);
      if (a instanceof Number && b instanceof Number) {
        return ((Number) a).doubleValue() > ((Number) b).doubleValue();
      }
      return false;
    });

    // 3. Helper "lt": Menor que
    this.handlebars.registerHelper("lt", (Object a, Options options) -> {
      Object b = options.param(0);
      if (a instanceof Number && b instanceof Number) {
        return ((Number) a).doubleValue() < ((Number) b).doubleValue();
      }
      return false;
    });

    // 4. Helper "add": Suma
    this.handlebars.registerHelper("add", (Object a, Options options) -> {
      try {
        int x = Integer.parseInt(a.toString());
        int y = Integer.parseInt(options.param(0).toString());
        return x + y;
      } catch (Exception e) {
        return a;
      }
    });

    // 5. Helper "subtract": Resta
    this.handlebars.registerHelper("subtract", (Object a, Options options) -> {
      try {
        int x = Integer.parseInt(a.toString());
        int y = Integer.parseInt(options.param(0).toString());
        return x - y;
      } catch (Exception e) {
        return a;
      }
    });

    // 6. Helper "range": Genera lista de números para iterar
    this.handlebars.registerHelper("range", (Object start, Options options) -> {
      int s = Integer.parseInt(start.toString());
      int e = Integer.parseInt(options.param(0).toString());
      List<Integer> list = new ArrayList<>();
      for (int i = s; i <= e; i++) {
        list.add(i);
      }
      return list;
    });

    // 7. Helper "json": Serializa objetos a JSON (CRÍTICO para pasar datos a JS)
    this.handlebars.registerHelper("json", (value, options) -> {
      try {
        return new com.fasterxml.jackson.databind.ObjectMapper()
            .registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule())
            .disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .writeValueAsString(value);
      } catch (Exception e) {
        return "{}";
      }
    });

    // 8. Helper "formatDate": Formatea fechas Java (LocalDateTime, ZonedDateTime)
    this.handlebars.registerHelper("formatDate", (value, options) -> {
      if (value == null) return "";
      String pattern = options.param(0, "dd/MM/yyyy HH:mm");
      if (value instanceof TemporalAccessor) {
        return DateTimeFormatter.ofPattern(pattern).format((TemporalAccessor) value);
      }
      return value.toString();
    });
  }

  @NotNull
  @Override
  public String render(
      @NotNull String path,
      @NotNull Map<String, ?> model,
      @NotNull Context context
  ) {
    try {
      // Mantenemos tu lógica de rutas: asume que los templates están en la carpeta "templates/"
      // y elimina la extensión .hbs si viene incluida.
      String templatePath = "templates/" + path.replace(".hbs", "");

      Template template = handlebars.compile(templatePath);
      return template.apply(model);
    } catch (IOException e) {
      e.printStackTrace();
      context.status(HttpStatus.NOT_FOUND);
      return "Error interno: No se pudo renderizar la plantilla '" + path + "'. Revisa la consola.";
    }
  }
}
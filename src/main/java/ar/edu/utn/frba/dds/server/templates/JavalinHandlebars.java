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
import org.jetbrains.annotations.NotNull;

public class JavalinHandlebars implements FileRenderer {

  Handlebars handlebars = new Handlebars();

  @NotNull
  @Override
  public String render(
      @NotNull String path,
      @NotNull Map<String, ?> model,
      @NotNull Context context
  ) {
    Template template = null;
    try {
      handlebars.registerHelper(
          "eq", (Object a, Options options) -> {
            Object b = options.param(0);
            if (a == null || b == null) {
              return false;
            }
            return a.toString()
                    .equals(b.toString());
          }
      );
      handlebars.registerHelper(
          "gt", (Object a, Options options) -> {
            Object b = options.param(0);
            if (a instanceof Number && b instanceof Number) {
              return ((Number) a).doubleValue() > ((Number) b).doubleValue();
            }
            return false;
          }
      );

      handlebars.registerHelper("range", (Object start, Options options) -> {
        int s = Integer.parseInt(start.toString());
        int e = Integer.parseInt(options.param(0).toString());

        List<Integer> list = new ArrayList<>();
        for (int i = s; i <= e; i++) {
          list.add(i);
        }
        return list;
      });

      handlebars.registerHelper("add", (Object a, Options options) -> {
        int x = Integer.parseInt(a.toString());
        int y = Integer.parseInt(options.param(0).toString());
        return x + y;
      });

      handlebars.registerHelper("subtract", (Object a, Options options) -> {
        int x = Integer.parseInt(a.toString());
        int y = Integer.parseInt(options.param(0).toString());
        return x - y;
      });

      handlebars.registerHelper("lt", (Object a, Options options) -> {
        int x = Integer.parseInt(a.toString());
        int y = Integer.parseInt(options.param(0).toString());
        return x < y;
      });

      handlebars.registerHelper(
          "formatDate", (value, options) -> {
            if (value == null) {
              return "";
            }

            // 1. Obtenemos el formato que pasaste en el HBS (param 0).
            // Si no pasas nada, usamos "dd/MM/yyyy" por defecto.
            String pattern = options.param(0, "dd/MM/yyyy");

            // 2. Formateamos si es del tipo correcto (LocalDateTime, LocalDate, etc)
            if (value instanceof TemporalAccessor) {
              return DateTimeFormatter.ofPattern(pattern)
                                      .format((TemporalAccessor) value);
            }

            return value.toString();
          }
      );
      handlebars.registerHelper(
          "or", (value, options) -> {
            Object otherValue = options.param(0);

            // Función auxiliar para saber si algo es "verdadero" (no nulo, no vacío, no false)
            boolean a = value != null
                && !(value instanceof Boolean && !((Boolean) value))
                && !(value instanceof java.util.Collection && ((java.util.Collection<?>) value).isEmpty());

            boolean b = otherValue != null
                && !(otherValue instanceof Boolean && !((Boolean) otherValue))
                && !(otherValue instanceof java.util.Collection && ((java.util.Collection<?>) otherValue).isEmpty());

            return a || b;
          }
      );
      template = handlebars.compile("templates/" + path.replace(".hbs", ""));
      return template.apply(model);
    } catch (IOException e) {
      e.printStackTrace();
      context.status(HttpStatus.NOT_FOUND);
      return "No se encuentra la página indicada...";
    }
  }
}
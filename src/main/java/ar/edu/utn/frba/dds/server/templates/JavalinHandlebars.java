package ar.edu.utn.frba.dds.server.templates;


import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Helper;
import com.github.jknack.handlebars.Options;
import com.github.jknack.handlebars.Template;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import io.javalin.rendering.FileRenderer;
import java.io.IOException;
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
      template = handlebars.compile("templates/" + path.replace(".hbs", ""));
      return template.apply(model);
    } catch (IOException e) {
      e.printStackTrace();
      context.status(HttpStatus.NOT_FOUND);
      return "No se encuentra la página indicada...";
    }
  }
}
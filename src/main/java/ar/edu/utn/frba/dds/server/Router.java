package ar.edu.utn.frba.dds.server;

import ar.edu.utn.frba.dds.controller.AdminController;
import ar.edu.utn.frba.dds.controller.ColeccionController;
import ar.edu.utn.frba.dds.controller.EstadisticaController;
import ar.edu.utn.frba.dds.controller.FuenteController;
import ar.edu.utn.frba.dds.controller.HechoController;
import ar.edu.utn.frba.dds.controller.SolicitudController;
import ar.edu.utn.frba.dds.controller.UserController;
import ar.edu.utn.frba.dds.dto.EstadisticaDTO;
import ar.edu.utn.frba.dds.dto.SolicitudDTO;
import ar.edu.utn.frba.dds.model.estadisticas.Estadistica;
import ar.edu.utn.frba.dds.model.exceptions.RazonInvalidaException;
import ar.edu.utn.frba.dds.model.hecho.Hecho;
import ar.edu.utn.frba.dds.model.reportes.EstadoSolicitud;
import ar.edu.utn.frba.dds.model.reportes.Solicitud;
import ar.edu.utn.frba.dds.model.usuario.Rol;
import ar.edu.utn.frba.dds.model.usuario.Usuario;
import ar.edu.utn.frba.dds.repositories.SolicitudesRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Router {

  public void configure(Javalin app, ObjectMapper mapper, boolean debugMode) {

    HechoController hechoController = new HechoController();
    SolicitudController solicitudController = new SolicitudController();
    ColeccionController coleccionController = new ColeccionController();
    FuenteController fuenteController = new FuenteController();
    EstadisticaController estadisticaController = new EstadisticaController();
    UserController userController = new UserController();
    AdminController adminController = new AdminController();

    // ========== MIDDLEWARE GLOBAL ==========
    app.before(ctx -> {
      Long id = ctx.sessionAttribute("usuario_id");

      if (id != null) {
        ctx.sessionAttribute("estaLogueado", true);
        ctx.sessionAttribute("nombreUsuario", ctx.sessionAttribute("usuario_nombre"));
        ctx.sessionAttribute("emailUsuario", ctx.sessionAttribute("usuario_email"));

        Rol rol = ctx.sessionAttribute("usuario_rol");
        ctx.sessionAttribute("rolUsuario", rol);
        ctx.sessionAttribute("esAdmin", Rol.ADMINISTRADOR.equals(rol));
        ctx.sessionAttribute("esUsuario", Rol.CONTRIBUYENTE.equals(rol));
      } else {
        ctx.sessionAttribute("estaLogueado", false);
        ctx.sessionAttribute("nombreUsuario", null);
        ctx.sessionAttribute("emailUsuario", null);
        ctx.sessionAttribute("rolUsuario", null);
        ctx.sessionAttribute("esAdmin", false);
        ctx.sessionAttribute("esUsuario", false);
      }
    });

    // Middleware para proteger rutas de admin
    app.before("/admin/*", ctx -> {
      Rol rol = ctx.sessionAttribute("usuario_rol");
      if (rol != Rol.ADMINISTRADOR) {
        ctx.redirect("/");
      }
    });

    // ========== RUTAS DE AUTENTICACIÓN ==========
    app.post("/login", userController::login);
    app.post("/logout", userController::logout);
    app.post("/usuarios", userController::register);
    app.get("/usuarios", ctx -> ctx.json(userController.findAll()));
    app.post("/usuarios/administrador", userController::registerAdmin);
    app.get("/auth/register", userController::mostrarRegistro);
    app.get("/auth/login", userController::mostrarLogin);

    // ========== RUTAS PRINCIPALES ==========
    app.get("/", ctx -> {
      List<Hecho> hechos = hechoController.findAll();
      String hechosJson = mapper.writeValueAsString(hechos);

      Map<String, Object> model = modeloConSesion(ctx);
      model.put("hechosJson", hechosJson);
      model.put("hechos", hechos);
      model.put("categorias", hechoController.getCategorias());
      model.put("colecciones", coleccionController.findAll());
      model.put("fuentes", fuenteController.findAll());

      ctx.render("index.hbs", model);
    });

    // ========== RUTAS DE HECHOS ==========
    app.get("/home", ctx -> ctx.json(hechoController.findAll()));
    app.get("/hechos", ctx -> ctx.json(hechoController.findAll()));

    app.get("/api/hechos", ctx -> {
      int page = ctx.queryParamAsClass("page", Integer.class).getOrDefault(1);
      int pageSize = 15;

      List<Hecho> hechos = hechoController.findAll();
      int totalHechos = hechos.size();
      int totalPages = (int) Math.ceil((double) totalHechos / pageSize);

      int fromIndex = (page - 1) * pageSize;
      int toIndex = Math.min(fromIndex + pageSize, totalHechos);

      List<Hecho> hechosPagina = hechos.subList(fromIndex, toIndex);

      Map<String, Object> model = modeloConSesion(ctx);
      model.put("hechos", hechosPagina);
      model.put("paginaActual", page);
      model.put("totalPaginas", totalPages);

      ctx.render("hechos.hbs", model);
    });
    app.get(
        "/hechos/nuevo", ctx -> {
          Boolean estaLogueado = ctx.sessionAttribute("estaLogueado");
          if (estaLogueado == null || !estaLogueado) {
            String redirect = "/hechos/nuevo";
            String fuenteId = ctx.queryParam("fuente_id");
            if (fuenteId != null) {
              redirect += "?fuente_id=" + fuenteId;
            }
            ctx.redirect("/auth/login?redirect=" + redirect);
            return;
          }
          Map<String, Object> model = modeloConSesion(ctx);
          model.put("fuente_id", ctx.queryParam("fuente_id"));
          ctx.render("hecho-nuevo.hbs", model);
        }
    );

    app.get("/hechos/{id}", ctx -> {
      Map<String, Object> model = modeloConSesion(ctx);
      hechoController.verHecho(ctx, model);
    });

    app.get("/hechos/{id}/fotos/{indice}", hechoController::getFoto);

    app.get("/hechos/{id}/editar", ctx -> {
      Map<String, Object> model = modeloConSesion(ctx);
      hechoController.editarHecho(ctx, model);
    });

    app.post("/hechos/{id}/editar", hechoController::actualizarHecho);


    app.post("/hechos/nuevo", ctx -> {
      try {
        hechoController.crearHecho(ctx);
      } catch (Exception e) {
        e.printStackTrace();
        ctx.status(400).result("Error: " + e.getMessage());
      }
    });

    // ========== RUTAS DE BÚSQUEDA ==========
    app.get("/api/hechos/buscar", ctx -> {
      String titulo = ctx.queryParam("titulo");
      if (titulo == null || titulo.trim().isEmpty()) {
        ctx.json(Collections.emptyList());
        return;
      }
      List<Hecho> resultados = hechoController.findByTitle(titulo);
      ctx.json(resultados);
    });

    // ========== RUTAS DE CATEGORÍAS Y ETIQUETAS ==========
    app.get("/hechos/categorias", ctx -> {
      List<String> categorias = hechoController.getCategorias();
      ctx.json(categorias);
      ctx.status(200);
    });

    app.get("/etiquetas", ctx -> {
      List<String> etiquetas = hechoController.getEtiquetas();
      ctx.json(etiquetas);
    });

    // ========== RUTAS DE SOLICITUDES ==========
    app.post("/solicitudes", ctx -> {
      try {
        Solicitud solicitud = solicitudController.crearSolicitud(ctx.bodyAsClass(SolicitudDTO.class));
        ctx.status(201).json(solicitud);
      } catch (RazonInvalidaException e) {
        ctx.status(400).result("Error: " + e.getMessage());
      }
    });

    app.get("/admin/solicitudes", ctx -> {
      List<Solicitud> pendientes = SolicitudesRepository.instance()
                                                        .obtenerPorEstado(EstadoSolicitud.PENDIENTE);
      Map<String, Object> model = modeloConSesion(ctx);
      model.put("solicitudes", pendientes);
      ctx.render("solicitudes.hbs", model);
    });

    app.put("/admin/solicitudes", ctx -> {
      if ("PUT".equalsIgnoreCase(String.valueOf(ctx.method()))) {
        tieneRol(Rol.ADMINISTRADOR).handle(ctx);
      }
    });

    // ========== RUTAS DE FUENTES ==========
    app.get("/fuentes", ctx -> {
      Boolean soloSimples = ctx.queryParamAsClass("soloSimples", Boolean.class).getOrDefault(false);
      ctx.json(fuenteController.obtenerFuentesConTipo(soloSimples));
    });

    app.post("/admin/fuentes", ctx -> adminController.crearFuente(ctx));
    app.get("/admin/fuentes", ctx -> adminController.listarFuentes(ctx, modeloConSesion(ctx)));
    app.get("/admin/fuentes/{id}", ctx -> adminController.editarFuente(ctx, modeloConSesion(ctx)));
    app.post("/admin/fuentes/{id}/configurar", adminController::configurarFuente);
    app.post("/admin/fuentes/{id}/borrar", adminController::borrarFuente);
    app.post("/admin/fuentes/{id}/hechos/{idHecho}/borrar", adminController::borrarHechoDeFuente);
    app.post("/admin/fuentes/{id}/agregar-fuente", adminController::agregarFuenteAAgregacion);
    app.post("/admin/fuentes/{id}/quitar-fuente/{idHija}", adminController::quitarFuenteDeAgregacion);

    // ========== RUTAS DE COLECCIONES ==========
    app.get("/admin/colecciones", ctx -> adminController.listarColecciones(ctx, modeloConSesion(ctx)));
    app.get("/admin/colecciones/{id}", ctx -> adminController.editarColeccion(ctx, modeloConSesion(ctx)));
    app.post("/admin/colecciones", ctx -> adminController.crearColeccion(ctx));
    app.post("/admin/colecciones/{id}/configurar", adminController::configurarColeccion);
    app.post("/admin/colecciones/{id}/agregarHecho", adminController::agregarHechoAColeccion);
    app.post("/admin/colecciones/{id}/quitarHecho", adminController::removerHechoDeColeccion);
    app.post("/admin/colecciones/{id}/calcular-consenso", coleccionController::calcularConsenso);

    // ========== RUTAS DE ESTADÍSTICAS ==========
    app.before("/admin/estadisticas", ctx -> {
      if (!"OPTIONS".equalsIgnoreCase(String.valueOf(ctx.method()))) {
        tieneRol(Rol.ADMINISTRADOR).handle(ctx);
      }
    });

    app.get("/admin/estadisticas", ctx -> {
      List<Estadistica> todas = estadisticaController.getEstadisticas();
      Map<String, Object> model = modeloConSesion(ctx);
      model.put("cantidadHechos", filtrar(todas, "CANTIDAD DE HECHOS"));
      model.put("solicitudesPendientes", filtrar(todas, "CANTIDAD DE SOLICITUDES PENDIENTES"));
      model.put("solicitudesSpam", filtrar(todas, "CANTIDAD DE SPAM"));
      model.put("categorias", hechoController.getCategorias());
      model.put("colecciones", coleccionController.getColeccionesDTO());
      ctx.render("estadisticas.hbs", model);
    });

    app.get("/admin/estadisticas/coleccion/{id}", ctx -> {
      Long id = Long.parseLong(ctx.pathParam("id"));
      List<Estadistica> todas = estadisticaController.getEstadisticas();

      List<Map<String, Object>> resultado = todas.stream()
                                                 .filter(e -> "HECHOS REPORTADOS POR PROVINCIA Y COLECCION".equals(e.getTipo()))
                                                 .filter(e -> e.getColeccion() != null && e.getColeccion().getId().equals(id))
                                                 .map(e -> {
                                                   Map<String, Object> m = new HashMap<>();
                                                   m.put("grupo", e.getGrupo());
                                                   m.put("cantidad", e.getCantidad());
                                                   return m;
                                                 })
                                                 .toList();

      ctx.json(resultado);
    });

    app.get("/admin/estadisticas/provincia", ctx -> {
      String categoria = ctx.queryParam("categoria");
      List<Estadistica> todas = estadisticaController.getEstadisticas();

      List<Map<String, Object>> resultado = todas.stream()
                                                 .filter(e -> "HECHOS POR PROVINCIA Y CATEGORIA".equals(e.getTipo()))
                                                 .filter(e -> categoria == null || categoria.equals(e.getCategoria()))
                                                 .map(e -> {
                                                   Map<String, Object> m = new HashMap<>();
                                                   m.put("grupo", e.getGrupo());
                                                   m.put("cantidad", e.getCantidad());
                                                   return m;
                                                 })
                                                 .toList();

      ctx.json(resultado);
    });

    app.get("/admin/estadisticas/hora", ctx -> {
      String categoria = ctx.queryParam("categoria");
      List<Estadistica> todas = estadisticaController.getEstadisticas();

      List<Map<String, Object>> resultado = todas.stream()
                                                 .filter(e -> "HECHOS POR HORA Y CATEGORIA".equals(e.getTipo()))
                                                 .filter(e -> categoria == null || categoria.equals(e.getCategoria()))
                                                 .map(e -> {
                                                   Map<String, Object> m = new HashMap<>();
                                                   m.put("grupo", e.getGrupo());
                                                   m.put("cantidad", e.getCantidad());
                                                   return m;
                                                 })
                                                 .toList();

      ctx.json(resultado);
    });

    app.get("/admin/estadisticas/categorias", ctx -> {
      Integer top = ctx.queryParamAsClass("top", Integer.class).getOrDefault(10);
      String orden = ctx.queryParam("orden");

      List<Estadistica> todas = estadisticaController.getEstadisticas();

      List<Map<String, Object>> resultado = todas.stream()
                                                 .filter(e -> "HECHOS REPORTADOS POR CATEGORIA".equals(e.getTipo()))
                                                 .sorted((a, b) -> {
                                                   if ("asc".equalsIgnoreCase(orden)) {
                                                     return a.getCantidad().compareTo(b.getCantidad());
                                                   }
                                                   return b.getCantidad().compareTo(a.getCantidad());
                                                 })
                                                 .limit(top)
                                                 .map(e -> {
                                                   Map<String, Object> m = new HashMap<>();
                                                   m.put("categoria", e.getCategoria());
                                                   m.put("cantidad", e.getCantidad());
                                                   return m;
                                                 })
                                                 .toList();

      ctx.json(resultado);
    });

    app.get("/admin/estadisticas/coleccion", ctx -> {
      List<Estadistica> todas = estadisticaController.getEstadisticas();

      List<Map<String, Object>> resultado = todas.stream()
                                                 .filter(e -> "HECHOS REPORTADOS POR PROVINCIA Y COLECCION".equals(e.getTipo()))
                                                 .map(e -> {
                                                   Map<String, Object> m = new HashMap<>();
                                                   m.put("coleccion", e.getColeccion().getTitulo());
                                                   m.put("provincia", e.getGrupo());
                                                   m.put("cantidad", e.getCantidad());
                                                   return m;
                                                 })
                                                 .toList();

      ctx.json(resultado);
    });

    app.get("/admin/estadisticas/categorias/todas", ctx -> {
      List<Estadistica> todas = estadisticaController.getEstadisticas();

      List<Map<String, Object>> resultado = todas.stream()
                                                 .filter(e -> "HECHOS REPORTADOS POR CATEGORIA".equals(e.getTipo()))
                                                 .map(e -> {
                                                   Map<String, Object> m = new HashMap<>();
                                                   m.put("categoria", e.getCategoria());
                                                   m.put("cantidad", e.getCantidad());
                                                   return m;
                                                 })
                                                 .toList();

      ctx.json(resultado);
    });

    app.get("/admin/estadisticas/hechos/todas", ctx -> {
      List<Estadistica> todas = estadisticaController.getEstadisticas();

      Map<String, Object> resultado = new HashMap<>();

      List<Map<String, Object>> provincia = todas.stream()
                                                 .filter(e -> "HECHOS POR PROVINCIA Y CATEGORIA".equals(e.getTipo()))
                                                 .map(e -> {
                                                   Map<String, Object> m = new HashMap<>();
                                                   m.put("provincia", e.getGrupo());
                                                   m.put("categoria", e.getCategoria());
                                                   m.put("cantidad", e.getCantidad());
                                                   return m;
                                                 })
                                                 .collect(Collectors.toList());

      List<Map<String, Object>> hora = todas.stream()
                                            .filter(e -> "HECHOS POR HORA Y CATEGORIA".equals(e.getTipo()))
                                            .map(e -> {
                                              Map<String, Object> m = new HashMap<>();
                                              m.put("hora", e.getGrupo());
                                              m.put("categoria", e.getCategoria());
                                              m.put("cantidad", e.getCantidad());
                                              return m;
                                            })
                                            .collect(Collectors.toList());

      resultado.put("provincia", provincia);
      resultado.put("hora", hora);

      ctx.json(resultado);
    });

    app.post("/admin/estadisticas", ctx -> {
      try {
        var stat = estadisticaController.calcularEstadisticas(ctx.bodyAsClass(EstadisticaDTO.class));
        ctx.status(200).json(stat);
      } catch (RuntimeException e) {
        ctx.status(400).result(e.getMessage());
      }
    });

    // ========== RUTAS DE PERFILES ==========
    app.get("/perfil", ctx -> {
      Boolean estaLogueado = ctx.sessionAttribute("estaLogueado");
      if (estaLogueado == null || !estaLogueado) {
        ctx.redirect("/auth/login?redirect=/perfil");
        return;
      }
      Usuario user = userController.finByName(ctx.sessionAttribute("nombreUsuario"));
      Map<String, Object> model = modeloConSesion(ctx);
      model.put("perfil", user);
      ctx.render("perfil.hbs", model);
    });

    app.get("/perfiles/{nombre}/", ctx -> {
      Usuario user = userController.finByName(ctx.pathParam("nombre"));
      Map<String, Object> model = modeloConSesion(ctx);
      model.put("perfil", user);
      ctx.render("perfil.hbs", model);
    });

    // ========== RUTAS DE REPORTES ==========
    app.get("/hechos/reporte/{id}/", ctx -> {
      Boolean estaLogueado = ctx.sessionAttribute("estaLogueado");
      if (estaLogueado == null || !estaLogueado) {
        ctx.redirect("/auth/login");
        return;
      }
      Hecho hecho = hechoController.findById(Long.parseLong(ctx.pathParam("id")));
      Map<String, Object> model = modeloConSesion(ctx);
      model.put("hecho", hecho);
      ctx.render("reportar.hbs", model);
    });

    // ========== RUTAS DE ADMINISTRACIÓN ==========
    app.get("/admin/dashboard", ctx -> adminController.mostrarDashboard(ctx, modeloConSesion(ctx)));

    // ========== MANEJO DE ERRORES ==========

    // Error 404 - Página no encontrada
    app.error(404, ctx -> {
      Map<String, Object> model = modeloConSesion(ctx);
      model.put("errorMessage", "Página no encontrada");
      model.put("errorStatus", 404);
      model.put("timestamp", LocalDateTime.now().toString());

      // Si es una petición API, devolver JSON
      if (ctx.path().startsWith("/api/")) {
        ctx.json(model);
      } else {
        ctx.render("404.hbs", model);
      }
    });

    // Error 500 - Error interno del servidor
    app.error(500, ctx -> {
      Map<String, Object> model = modeloConSesion(ctx);

      Throwable error = ctx.attribute("javalin-exception");
      String errorMessage = "Error interno del servidor";
      String errorDetails = "";

      if (error != null) {
        errorMessage = error.getMessage() != null ? error.getMessage() : errorMessage;
        errorDetails = getStackTraceAsString(error);

        // Log del error
        System.err.println("Error 500: " + errorMessage);
        if (debugMode) {
          error.printStackTrace();
        }
      }

      model.put("errorMessage", errorMessage);
      model.put("errorDetails", errorDetails);
      model.put("errorStatus", 500);
      model.put("showDetails", debugMode);
      model.put("timestamp", LocalDateTime.now().toString());

      ctx.render("error.hbs", model);
    });

    // Otros códigos de error
    app.error(400, ctx -> mostrarError(ctx, "Solicitud incorrecta", 400));
    app.error(403, ctx -> mostrarError(ctx, "Acceso denegado", 403));
    app.error(401, ctx -> mostrarError(ctx, "No autorizado", 401));

    // Manejo de excepciones no capturadas
    app.exception(Exception.class, (e, ctx) -> {
      System.err.println("Excepción no capturada: " + e.getMessage());
      if (debugMode) {
        e.printStackTrace();
      }

      Map<String, Object> model = modeloConSesion(ctx);
      model.put("errorMessage", debugMode ? e.getMessage() : "Ha ocurrido un error inesperado");
      model.put("errorDetails", debugMode ? getStackTraceAsString(e) : "");
      model.put("errorStatus", 500);
      model.put("showDetails", debugMode);
      model.put("timestamp", LocalDateTime.now().toString());

      ctx.status(500).render("error.hbs", model);
    });
  }

  // ========== MÉTODOS AUXILIARES ==========

  private Handler tieneRol(Rol rol) {
    return ctx -> {
      Rol rolEnSesion = ctx.sessionAttribute("usuario_rol");
      if (rolEnSesion == null || !rolEnSesion.equals(rol)) {
        throw new io.javalin.http.ForbiddenResponse("Acceso denegado");
      }
    };
  }

  private Map<String, Object> modeloConSesion(Context ctx) {
    Map<String, Object> model = new HashMap<>();
    model.put("estaLogueado", ctx.sessionAttribute("estaLogueado"));
    model.put("nombreUsuario", ctx.sessionAttribute("usuario_nombre"));
    model.put("rolUsuario", ctx.sessionAttribute("usuario_rol"));
    model.put("esAdmin", ctx.sessionAttribute("esAdmin"));
    model.put("esUsuario", ctx.sessionAttribute("esUsuario"));
    model.put("emailUsuario", ctx.sessionAttribute("emailUsuario"));
    model.put("usuario_id", ctx.sessionAttribute("usuario_id"));
    return model;
  }

  private List<Estadistica> filtrar(List<Estadistica> lista, String tipo) {
    return lista.stream()
                .filter(e -> tipo.equals(e.getTipo()))
                .toList();
  }

  private void mostrarError(Context ctx, String mensaje, int status) {
    Map<String, Object> model = modeloConSesion(ctx);
    model.put("errorMessage", mensaje);
    model.put("errorStatus", status);
    model.put("showDetails", false);
    model.put("timestamp", LocalDateTime.now().toString());

    ctx.status(status).render("error.hbs", model);
  }

  private String getStackTraceAsString(Throwable throwable) {
    if (throwable == null) return "";

    StringWriter sw = new StringWriter();
    PrintWriter pw = new PrintWriter(sw);
    throwable.printStackTrace(pw);
    return sw.toString();
  }
}
package ar.edu.utn.frba.dds.server;

import ar.edu.utn.frba.dds.controller.*;
import ar.edu.utn.frba.dds.dto.EstadisticaDTO;
import ar.edu.utn.frba.dds.dto.SolicitudDTO;
import ar.edu.utn.frba.dds.model.coleccion.Coleccion;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Router {

  private Map<String, Object> modeloConSesion(Context ctx) {
    Map<String, Object> model = new HashMap<>();

    Long id = ctx.sessionAttribute("usuario_id");
    Boolean estaLogueado = id != null;

    model.put("estaLogueado", estaLogueado);

    if (estaLogueado) {
      model.put("usuarioId", id);
      model.put("nombreUsuario", ctx.sessionAttribute("usuario_nombre"));
      model.put("emailUsuario", ctx.sessionAttribute("usuario_email"));

      Rol rol = ctx.sessionAttribute("usuario_rol");
      model.put("rolUsuario", rol);
      model.put("esAdmin", Rol.ADMINISTRADOR.equals(rol));
      model.put("esUsuario", Rol.CONTRIBUYENTE.equals(rol));
    } else {
      model.put("esAdmin", false);
      model.put("esUsuario", false);
    }

    return model;
  }

  private Handler tieneRol(Rol rolRequerido) {
    return ctx -> {
      Rol rolUsuario = ctx.sessionAttribute("usuario_rol");
      if (rolUsuario != rolRequerido) {
        ctx.status(403).result("Acceso denegado");
      }
    };
  }

  private List<Estadistica> filtrar(List<Estadistica> todas, String tipo) {
    return todas.stream()
                .filter(e -> tipo.equals(e.getTipo()))
                .collect(Collectors.toList());
  }

  private void mostrarError(Context ctx, String mensaje, int status) {
    ctx.status(status);

    if (ctx.path().startsWith("/api/")) {
      ctx.json(Map.of(
          "error", true,
          "mensaje", mensaje,
          "status", status
      ));
    } else {
      Map<String, Object> model = modeloConSesion(ctx);
      model.put("errorMessage", mensaje);
      model.put("errorStatus", status);
      model.put("timestamp", LocalDateTime.now().toString());

      switch (status) {
        case 404:
          ctx.render("404.hbs", model);
          break;
        case 403:
          ctx.render("403.hbs", model);
          break;
        default:
          ctx.render("error.hbs", model);
      }
    }
  }

  private String getStackTraceAsString(Throwable throwable) {
    StringWriter sw = new StringWriter();
    PrintWriter pw = new PrintWriter(sw);
    throwable.printStackTrace(pw);
    return sw.toString();
  }

  public void configure(Javalin app, ObjectMapper mapper, boolean debugMode) {
    HechoController hechoController = new HechoController();
    SolicitudController solicitudController = new SolicitudController();
    ColeccionController coleccionController = new ColeccionController();
    FuenteController fuenteController = new FuenteController();
    EstadisticaController estadisticaController = new EstadisticaController();
    UserController userController = new UserController();
    AdminController adminController = new AdminController();

    // Middleware de sesión
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

    // Middleware de administración
    app.before("/admin/*", ctx -> {
      Rol rol = ctx.sessionAttribute("usuario_rol");
      if (rol != Rol.ADMINISTRADOR) {
        ctx.redirect("/");
      }
    });

    // ==================== AUTENTICACIÓN ====================
    app.get("/auth/register", userController::mostrarRegistro);
    app.get("/auth/login", userController::mostrarLogin);
    app.post("/login", userController::login);
    app.post("/logout", userController::logout);
    app.post("/usuarios", userController::register);
    app.post("/usuarios/administrador", userController::registerAdmin);
    app.get("/usuarios", ctx -> ctx.json(userController.findAll()));

    // ==================== HECHOS ====================
    app.get("/", ctx -> {
      // Usar el método paginado por defecto
      Map<String, Object> resultado = hechoController.buscarAvanzadoCompletoPaginated(
          null, null, null, null, null, null, false, false, 1, 15
      );

      @SuppressWarnings("unchecked")
      List<Map<String, Object>> hechosValidos = (List<Map<String, Object>>) resultado.get("resultados");
      String hechosJson = mapper.writeValueAsString(hechosValidos);

      Map<String, Object> model = modeloConSesion(ctx);
      model.put("hechosJson", hechosJson);
      model.put("hechos", hechosValidos);
      model.put("categorias", hechoController.getCategorias());
      model.put("colecciones", coleccionController.findAll());
      model.put("fuentes", fuenteController.findAll());
      model.put("totalHechos", resultado.get("total"));
      model.put("totalPages", resultado.get("totalPages"));
      model.put("paginaActual", 1);

      ctx.render("index.hbs", model);
    });

    app.get("/home", ctx -> ctx.redirect("/"));
    app.get("/hechos", ctx -> ctx.json(hechoController.findAll()));

    app.get("/api/hechos", ctx -> {
      int page = ctx.queryParamAsClass("page", Integer.class).getOrDefault(1);
      int pageSize = 15;

      List<Hecho> hechos = hechoController.findAll();
      int totalHechos = hechos.size();
      int totalPages = (int) Math.ceil((double) totalHechos / pageSize);

      int fromIndex = (page - 1) * pageSize;
      int toIndex = Math.min(fromIndex + pageSize, totalHechos);

      List<Hecho> hechosPagina = (fromIndex < totalHechos)
                                 ? hechos.subList(fromIndex, toIndex)
                                 : Collections.emptyList();

      Map<String, Object> model = modeloConSesion(ctx);
      model.put("hechos", hechosPagina);
      model.put("paginaActual", page);
      model.put("totalPaginas", totalPages);

      ctx.render("hechos.hbs", model);
    });

    app.get("/hechos/nuevo", ctx -> {
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
    });

    app.post("/hechos/nuevo", hechoController::crearHecho);

    app.get("/hechos/{id}", ctx -> {
      Map<String, Object> model = modeloConSesion(ctx);
      hechoController.verHecho(ctx, model);
    });

    app.get("/hechos/{id}/multimedia/{indice}", hechoController::getMultimedia);

    app.get("/hechos/{id}/editar", ctx -> {
      Map<String, Object> model = modeloConSesion(ctx);
      hechoController.editarHecho(ctx, model);
    });

    app.post("/hechos/{id}/editar", hechoController::actualizarHecho);

    // Método alternativo si aún necesitas getFoto para compatibilidad
    app.get("/hechos/{id}/foto/{indice}", ctx -> {
      Long idHecho = Long.parseLong(ctx.pathParam("id"));
      int indice = Integer.parseInt(ctx.pathParam("indice"));

      Hecho hecho = hechoController.findById(idHecho);

      if (hecho != null && hecho.getFotos() != null && indice < hecho.getFotos().size()) {
        var foto = hecho.getFotos().get(indice);
        ctx.contentType(foto.getMimetype());
        ctx.result(foto.getDatos());
      } else {
        ctx.status(404);
      }
    });

    // ==================== BÚSQUEDAS API ====================
    app.get("/api/hechos/buscar", ctx -> {
      try {
        String titulo = ctx.queryParam("titulo");
        Boolean soloConsensuados = ctx.queryParamAsClass("soloConsensuados", Boolean.class)
                                      .getOrDefault(false);

        Map<String, Object> resultados = hechoController.buscarAvanzadoCompleto(
            titulo, null, null, null, null, null, soloConsensuados, false
        );

        ctx.json(resultados);
      } catch (Exception e) {
        ctx.status(400).json(Map.of(
            "error", true,
            "mensaje", e.getMessage()
        ));
      }
    });

    app.get("/api/hechos/buscar-completo", ctx -> {
      try {
        System.out.println("=== BÚSQUEDA COMPLETA ===");
        ctx.queryParamMap().forEach((key, values) ->
                                        System.out.println("  " + key + " = " + values)
        );

        String titulo = ctx.queryParam("titulo");
        String categoria = ctx.queryParam("categoria");
        String fuente = ctx.queryParam("fuente");
        String coleccion = ctx.queryParam("coleccion");
        String fechaDesde = ctx.queryParam("fechaDesde");
        String fechaHasta = ctx.queryParam("fechaHasta");
        Boolean soloConsensuados = ctx.queryParamAsClass("soloConsensuados", Boolean.class)
                                      .getOrDefault(false);
        Boolean incluirEliminados = ctx.queryParamAsClass("incluirEliminados", Boolean.class)
                                       .getOrDefault(false);

        int page = ctx.queryParamAsClass("page", Integer.class).getOrDefault(1);
        int pageSize = ctx.queryParamAsClass("pageSize", Integer.class).getOrDefault(15);

        boolean usarPaginacion = ctx.queryParam("paginar") != null &&
            Boolean.parseBoolean(ctx.queryParam("paginar"));

        Map<String, Object> resultados;

        if (usarPaginacion) {
          resultados = hechoController.buscarAvanzadoCompletoPaginated(
              titulo, categoria, fuente, coleccion,
              fechaDesde, fechaHasta, soloConsensuados, incluirEliminados,
              page, pageSize
          );
        } else {
          resultados = hechoController.buscarAvanzadoCompleto(
              titulo, categoria, fuente, coleccion,
              fechaDesde, fechaHasta, soloConsensuados, incluirEliminados
          );
        }

        ctx.json(resultados);
      } catch (Exception e) {
        System.err.println("ERROR en búsqueda completa: " + e.getMessage());
        e.printStackTrace();

        ctx.status(400).json(Map.of(
            "error", true,
            "mensaje", e.getMessage(),
            "detalle", e.getClass().getName()
        ));
      }
    });

    app.get("/api/hechos/buscar-rapido", ctx -> {
      try {
        String titulo = ctx.queryParam("titulo");
        Boolean soloConsensuados = ctx.queryParamAsClass("soloConsensuados", Boolean.class)
                                      .getOrDefault(false);
        List<Hecho> resultados = hechoController.buscarRapido(titulo, soloConsensuados);
        ctx.json(resultados);
      } catch (Exception e) {
        ctx.status(400).json(Map.of(
            "error", true,
            "mensaje", e.getMessage()
        ));
      }
    });

    // ==================== CATEGORÍAS Y ETIQUETAS ====================
    app.get("/hechos/categorias", ctx -> {
      List<String> categorias = hechoController.getCategorias();
      ctx.json(categorias);
    });

    app.get("/etiquetas", ctx -> {
      List<String> etiquetas = hechoController.getEtiquetas();
      ctx.json(etiquetas);
    });

    // ==================== SOLICITUDES ====================
    app.post("/solicitudes", ctx -> {
      try {
        Solicitud solicitud = solicitudController.crearSolicitud(
            ctx,
            ctx.bodyAsClass(SolicitudDTO.class)
        );
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

    app.get("/solicitudes/mis-solicitudes", ctx -> {
      Boolean estaLogueado = ctx.sessionAttribute("estaLogueado");
      if (estaLogueado == null || !estaLogueado) {
        ctx.redirect("/auth/login?redirect=/solicitudes/mis-solicitudes");
        return;
      }

      Long usuarioId = ctx.sessionAttribute("usuario_id");

      List<Solicitud> todas = SolicitudesRepository.instance().findAll();
      List<Solicitud> misSolicitudes = todas.stream()
                                            .filter(s -> s.getUsuario() != null && s.getUsuario().getId().equals(usuarioId))
                                            .sorted((a, b) -> b.getId().compareTo(a.getId()))
                                            .collect(Collectors.toList());

      Map<String, Object> model = modeloConSesion(ctx);
      model.put("solicitudes", misSolicitudes);

      ctx.render("solicitudes-info.hbs", model);
    });

    // ==================== FUENTES ====================
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
    app.post("/admin/fuentes/{id}/quitar-fuente/{idHija}",
             adminController::quitarFuenteDeAgregacion);

    // ==================== COLECCIONES ====================
    app.get("/api/admin/colecciones/buscar", ctx -> {
      try {
        String titulo = ctx.queryParam("titulo");
        String categoria = ctx.queryParam("categoria");

        List<Coleccion> resultados = coleccionController.buscarRapido(titulo, categoria);
        ctx.json(resultados);
      } catch (Exception e) {
        e.printStackTrace();
        ctx.status(500).json(Map.of("mensaje", "Error en la base de datos: " + e.getMessage()));
      }
    });

    app.get("/admin/colecciones", ctx -> {
      Map<String, Object> model = modeloConSesion(ctx);

      List<String> categorias = coleccionController.getCategorias();
      model.put("categorias", categorias);

      adminController.listarColecciones(ctx, model);
    });
    app.get("/admin/colecciones/{id}",
            ctx -> adminController.editarColeccion(ctx, modeloConSesion(ctx)));
    app.post("/admin/colecciones", ctx -> adminController.crearColeccion(ctx));
    app.post("/admin/colecciones/{id}/configurar", adminController::configurarColeccion);
    app.post("/admin/colecciones/{id}/agregarHecho", adminController::agregarHechoAColeccion);
    app.post("/admin/colecciones/{id}/quitarHecho", adminController::removerHechoDeColeccion);
    app.post("/admin/colecciones/{id}/calcular-consenso", coleccionController::calcularConsenso);
    app.post("/admin/colecciones/{id}/agregar-condicion", coleccionController::agregarCondicion);
    app.post("/admin/colecciones/{id}/eliminar-condicion", coleccionController::eliminarCondicion);

    // ==================== ESTADÍSTICAS ====================
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

    // ==================== PERFILES ====================
    app.get("/perfil", ctx -> {
      Boolean estaLogueado = ctx.sessionAttribute("estaLogueado");
      if (estaLogueado == null || !estaLogueado) {
        ctx.redirect("/auth/login?redirect=/perfil");
        return;
      }
      Usuario user = userController.findByName(ctx.sessionAttribute("nombreUsuario"));
      Map<String, Object> model = modeloConSesion(ctx);

      List<Hecho> ultimosHechos = new ArrayList<>();
      if (user.getHechos() != null) {
        ultimosHechos = user.getHechos()
                            .stream()
                            .sorted(Comparator.comparing(
                                Hecho::getFechacarga,
                                Comparator.nullsLast(Comparator.reverseOrder())
                            ))
                            .limit(5)
                            .collect(Collectors.toList());
      }
      model.put("perfil", user);
      model.put("ultimosHechos", ultimosHechos);
      ctx.render("perfil.hbs", model);
    });

    app.get("/usuarios/{id}/foto", userController::obtenerFotoPerfil);

    app.get("/perfiles/{nombre}/", ctx -> {
      Usuario user = userController.findByName(ctx.pathParam("nombre"));
      Map<String, Object> model = modeloConSesion(ctx);
      List<Hecho> ultimosHechos = new ArrayList<>();
      if (user.getHechos() != null) {
        ultimosHechos = user.getHechos()
                            .stream()
                            .sorted(Comparator.comparing(
                                Hecho::getFechacarga,
                                Comparator.nullsLast(Comparator.reverseOrder())
                            ))
                            .limit(5)
                            .collect(Collectors.toList());
      }
      model.put("perfil", user);
      model.put("ultimosHechos", ultimosHechos);
      ctx.render("perfil.hbs", model);
    });

    // ==================== REPORTES ====================
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

    app.get("/admin/dashboard", ctx -> adminController.mostrarDashboard(ctx, modeloConSesion(ctx)));

    // ==================== ERROR HANDLERS ====================
    app.error(404, ctx -> {
      if (ctx.attribute("error-handled") != null) {
        return;
      }

      Map<String, Object> model = modeloConSesion(ctx);
      model.put("errorMessage", "Página no encontrada");
      model.put("errorStatus", 404);
      model.put("timestamp", LocalDateTime.now().toString());

      if (ctx.path().startsWith("/api/")) {
        ctx.json(model);
      } else {
        ctx.render("404.hbs", model);
      }
    });

    app.error(500, ctx -> {
      if (ctx.attribute("error-handled") != null) {
        return;
      }

      Map<String, Object> model = modeloConSesion(ctx);

      Throwable error = ctx.attribute("javalin-exception");
      String errorMessage = "Error interno del servidor";
      String errorDetails = "";

      if (error != null) {
        errorMessage = error.getMessage() != null ? error.getMessage() : errorMessage;
        errorDetails = getStackTraceAsString(error);

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

    app.error(400, ctx -> {
      if (ctx.attribute("error-handled") != null) {
        return;
      }

      if (ctx.path().startsWith("/api/")) {
        mostrarError(ctx, "Solicitud incorrecta", 400);
      }
    });

    app.error(403, ctx -> {
      if (ctx.attribute("error-handled") != null) {
        return;
      }
      mostrarError(ctx, "Acceso denegado", 403);
    });

    app.error(401, ctx -> {
      if (ctx.attribute("error-handled") != null) {
        return;
      }
      mostrarError(ctx, "No autorizado", 401);
    });
  }
}
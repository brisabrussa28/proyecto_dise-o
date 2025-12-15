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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


public class Router {

  public void configure(Javalin app, ObjectMapper mapper) {

    HechoController hechoController = new HechoController();
    SolicitudController solicitudController = new SolicitudController();
    ColeccionController coleccionController = new ColeccionController();
    FuenteController fuenteController = new FuenteController();
    EstadisticaController estadisticaController = new EstadisticaController();
    UserController userController = new UserController();
    AdminController adminController = new AdminController();

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

    app.before(
        "/admin/*", ctx -> {
          Rol rol = ctx.sessionAttribute("usuario_rol");
          if (rol != Rol.ADMINISTRADOR) {
            ctx.redirect("/");
          }
        }
    );

    app.post("/login", userController::login);
    app.post("/logout", userController::logout);
    app.post("/usuarios", userController::register);
    app.get("/usuarios", ctx -> ctx.json(userController.findAll()));
    app.post("/usuarios/administrador", userController::registerAdmin);

    // --- RUTAS DE ADMINISTRACIÓN ---

    // Solicitudes
    app.put(
        "/admin/solicitudes", ctx -> {
          if ("PUT".equalsIgnoreCase(String.valueOf(ctx.method()))) {
            tieneRol(Rol.ADMINISTRADOR).handle(ctx);
          }
        }
    );

    // Fuentes
    app.post("/admin/fuentes", ctx -> adminController.crearFuente(ctx));
    app.get("/admin/fuentes", ctx -> adminController.listarFuentes(ctx, modeloConSesion(ctx)));
    app.get("/admin/fuentes/{id}", ctx -> adminController.editarFuente(ctx, modeloConSesion(ctx)));
    app.post("/admin/fuentes/{id}/configurar", adminController::configurarFuente);

    // Gestión de Fuentes
    app.post("/admin/fuentes/{id}/borrar", adminController::borrarFuente);
    app.post("/admin/fuentes/{id}/hechos/{idHecho}/borrar", adminController::borrarHechoDeFuente);

    // Gestión de Fuentes de Agregación
    app.post("/admin/fuentes/{id}/agregar-fuente", adminController::agregarFuenteAAgregacion);
    app.post(
        "/admin/fuentes/{id}/quitar-fuente/{idHija}",
        adminController::quitarFuenteDeAgregacion
    );

    // Colecciones
    app.get(
        "/admin/colecciones",
        context -> adminController.listarColecciones(context, modeloConSesion(context))
    );
    app.get(
        "/admin/colecciones/{id}",
        ctx -> adminController.editarColeccion(ctx, modeloConSesion(ctx))
    );
    app.post("/admin/colecciones", ctx -> adminController.crearColeccion(ctx));
    app.post("/admin/colecciones/{id}/configurar", adminController::configurarColeccion);
    app.post("/admin/colecciones/{id}/agregarHecho", adminController::agregarHechoAColeccion);
    app.post("/admin/colecciones/{id}/quitarHecho", adminController::removerHechoDeColeccion);
    app.post("/admin/colecciones/{id}/calcular-consenso", coleccionController::calcularConsenso);

    // Estadísticas
    app.before(
        "/admin/estadisticas", ctx -> {
          if (!"OPTIONS".equalsIgnoreCase(String.valueOf(ctx.method()))) {
            tieneRol(Rol.ADMINISTRADOR).handle(ctx);
          }
        }
    );

    app.get(
        "/", ctx -> {
          List<Hecho> hechos = hechoController.findAll();
          String hechosJson = mapper.writeValueAsString(hechos);

          Map<String, Object> model = modeloConSesion(ctx);
          model.put("hechosJson", hechosJson);
          model.put("hechos", hechos);
          model.put("categorias", hechoController.getCategorias());
          model.put("colecciones", coleccionController.findAll());
          model.put("fuentes", fuenteController.findAll());

          ctx.render("index.hbs", model);
        }
    );

    app.get("/api/hechos/buscar", ctx -> {
      String titulo = ctx.queryParam("titulo");

      if (titulo == null || titulo.trim().isEmpty()) {
        ctx.json(Collections.emptyList());
        return;
      }

      List<Hecho> resultados = hechoController.findByTitle(titulo);
      ctx.json(resultados);
    });

    app.get(
        "/admin/dashboard", ctx -> {
          adminController.mostrarDashboard(ctx, modeloConSesion(ctx));
        }
    );

    app.get(
        "/hechos/{id}/editar", ctx -> {
          Map<String, Object> model = modeloConSesion(ctx);
          hechoController.editarHecho(ctx, model);
        }
    );
    app.post("/hechos/{id}/editar", hechoController::actualizarHecho);
    app.get(
        "/hechos/categorias", ctx -> {
          List<String> categorias = hechoController.getCategorias();
          ctx.json(categorias);
          ctx.status(200);
        }
    );
    app.get(
        "/etiquetas", ctx -> {
          List<String> etiquetas = hechoController.getEtiquetas();
          ctx.json(etiquetas);
        }
    );
    app.get("/auth/register", userController::mostrarRegistro);
    app.get("/auth/login", userController::mostrarLogin);

    // Ruta para crear nuevo hecho (con o sin fuente)
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

    // Ruta para crear hecho (POST)
    app.post(
        "/hechos/nuevo", ctx -> {
          try {
            hechoController.crearHecho(ctx);
          } catch (Exception e) {
            e.printStackTrace();
            ctx.status(400)
               .result("Error: " + e.getMessage());
          }
        }
    );

    app.get("/home", ctx -> ctx.json(hechoController.findAll()));

    app.get(
        "/hechos", ctx -> {
          List<Hecho> hechos = hechoController.findAll();
          ctx.json(hechos);
        }
    );

    app.get(
        "/api/hechos", ctx -> {
          int page = ctx.queryParamAsClass("page", Integer.class)
                        .getOrDefault(1);
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
        }
    );

    app.get(
        "/hechos/{id}", ctx -> {
          Map<String, Object> model = modeloConSesion(ctx);
          hechoController.verHecho(ctx, model);
        }
    );

    app.get("/hechos/{id}/fotos/{indice}", hechoController::getFoto);

    app.post(
        "/solicitudes", ctx -> {
          try {
            Solicitud solicitud = solicitudController.crearSolicitud(ctx.bodyAsClass(SolicitudDTO.class));
            ctx.status(201);
            ctx.json(solicitud);
          } catch (RazonInvalidaException e) {
            ctx.status(400);
            ctx.result("Error: " + e.getMessage());
          }
        }
    );

    app.get(
        "/fuentes", ctx -> {
          Boolean soloSimples = ctx.queryParamAsClass("soloSimples", Boolean.class)
                                   .getOrDefault(false);
          ctx.json(fuenteController.obtenerFuentesConTipo(soloSimples));
        }
    );

    // Rutas de estadísticas
    app.get(
        "/admin/estadisticas", ctx -> {
          List<Estadistica> todas = estadisticaController.getEstadisticas();
          Map<String, Object> model = modeloConSesion(ctx);
          model.put("cantidadHechos", filtrar(todas, "CANTIDAD DE HECHOS"));
          model.put("solicitudesPendientes", filtrar(todas, "CANTIDAD DE SOLICITUDES PENDIENTES"));
          model.put("solicitudesSpam", filtrar(todas, "CANTIDAD DE SPAM"));
          model.put("categorias", hechoController.getCategorias());
          model.put("colecciones", coleccionController.getColeccionesDTO());
          ctx.render("estadisticas.hbs", model);
        }
    );
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
                                                   m.put("grupo", e.getGrupo());     // provincia
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
                                                   m.put("grupo", e.getGrupo());     // hora
                                                   m.put("cantidad", e.getCantidad());
                                                   return m;
                                                 })
                                                 .toList();

      ctx.json(resultado);
    });

    app.get("/admin/estadisticas/categorias", ctx -> {
      Integer top = ctx.queryParamAsClass("top", Integer.class).getOrDefault(10);
      String orden = ctx.queryParam("orden"); // "asc" o "desc"

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


    app.post(
        "/admin/estadisticas", ctx -> {
          try {
            var stat = estadisticaController.calcularEstadisticas(ctx.bodyAsClass(EstadisticaDTO.class));
            ctx.status(200);
            ctx.json(stat);
          } catch (RuntimeException e) {
            ctx.status(400);
            ctx.result(e.getMessage());
          }
        }
    );

    app.get(
        "/admin/solicitudes",
        ctx -> {
          List<Solicitud> pendientes = SolicitudesRepository.instance()
                                                            .obtenerPorEstado(EstadoSolicitud.PENDIENTE);
          Map<String, Object> model = modeloConSesion(ctx);
          model.put("solicitudes", pendientes);

          ctx.render("solicitudes.hbs", model);
        }
    );

    app.get(
        "/perfil", ctx -> {
          Boolean estaLogueado = ctx.sessionAttribute("estaLogueado");

          if (estaLogueado == null || !estaLogueado) {
            ctx.redirect("/auth/login?redirect=/perfil");
            return;
          }

          Usuario user = userController.finByName(ctx.sessionAttribute("nombreUsuario"));

          Map<String, Object> model = modeloConSesion(ctx);
          model.put("perfil", user);

          ctx.render("perfil.hbs", model);
        }
    );

    app.get(
        "/perfiles/{nombre}/", ctx -> {
          Usuario user = userController.finByName(ctx.pathParam("nombre"));
          System.out.println(user);

          Map<String, Object> model = modeloConSesion(ctx);
          model.put("perfil", user);

          ctx.render("perfil.hbs", model);
        }
    );

    app.get(
        "/hechos/reporte/{id}/", ctx -> {
          Boolean estaLogueado = ctx.sessionAttribute("estaLogueado");

          if (estaLogueado == null || !estaLogueado) {
            ctx.redirect("/auth/login");
            return;
          }

          Hecho hecho = hechoController.findById(Long.parseLong(ctx.pathParam("id")));

          Map<String, Object> model = modeloConSesion(ctx);
          model.put("hecho", hecho);

          ctx.render("reportar.hbs", model);
        }
    );

  }

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
}



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
import ar.edu.utn.frba.dds.model.fuentes.Fuente;
import ar.edu.utn.frba.dds.model.hecho.Hecho;
import ar.edu.utn.frba.dds.model.hecho.Origen;
import ar.edu.utn.frba.dds.model.hecho.etiqueta.Etiqueta;
import ar.edu.utn.frba.dds.model.hecho.multimedia.Multimedia;
import ar.edu.utn.frba.dds.model.info.PuntoGeografico;
import ar.edu.utn.frba.dds.model.reportes.EstadoSolicitud;
import ar.edu.utn.frba.dds.model.reportes.Solicitud;
import ar.edu.utn.frba.dds.model.usuario.Rol;
import ar.edu.utn.frba.dds.model.usuario.Usuario;
import ar.edu.utn.frba.dds.repositories.SolicitudesRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import io.javalin.http.HttpStatus;
import io.javalin.http.UploadedFile;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
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
    // --- 4. PROTECCIÓN DE RUTAS (app.before) ---

    // Protegemos la ACEPTACIÓN/RECHAZO de solicitudes
    app.before(
        "/admin/solicitudes", ctx -> {
          if ("PUT".equalsIgnoreCase(String.valueOf(ctx.method()))) {
            tieneRol(Rol.ADMINISTRADOR).handle(ctx);
          }
        }
    );

    // Protegemos la CREACIÓN de fuentes
    app.before(
        "/admin/fuentes", ctx -> {
          if ("POST".equalsIgnoreCase(String.valueOf(ctx.method()))) {
            tieneRol(Rol.ADMINISTRADOR).handle(ctx);
          }
        }
    );


    // Protegemos TODAS las rutas de /estadisticas
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

    app.get(
        "/admin/dashboard", ctx -> {
          adminController.mostrarDashboard(ctx, modeloConSesion(ctx));
        }
    );

    app.get(
        "/admin/colecciones", context -> {
          adminController.listarColecciones(context, modeloConSesion(context));
        }
    );
    app.get(
        "/admin/colecciones/{id}", ctx -> {
          adminController.editarColeccion(ctx, modeloConSesion(ctx));
        }
    ); // Vista Detalle
    app.post("/admin/colecciones", ctx -> adminController.crearColeccion(ctx));
    app.post("/admin/colecciones/{id}/agregarHecho", adminController::agregarHechoAColeccion);
    app.post("/admin/colecciones/{id}/quitarHecho", adminController::removerHechoDeColeccion);

    // --- NUEVO ENDPOINT PARA CALCULO MANUAL ---
    app.post("/admin/colecciones/{id}/calcular-consenso", coleccionController::calcularConsenso);

    app.post("/admin/fuentes", ctx -> adminController.crearFuente(ctx));
    app.post("/admin/colecciones/{id}/configurar", adminController::configurarColeccion);


    app.before(
        "/hechos/{id}/editar", ctx -> {
          Map<String, Object> model = modeloConSesion(ctx);
          Long hechoId = Long.parseLong(ctx.pathParam("id"));
          Hecho hecho = hechoController.findById(hechoId);
          Boolean estaLogueado = ctx.sessionAttribute("estaLogueado");
          if ((!hecho.getAutor()
                     .getId()
                     .equals(ctx.sessionAttribute("usuario_id"))) || estaLogueado == false) {
            ctx.status(HttpStatus.FORBIDDEN);
            ctx.redirect("/");
          }
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
        "/admin/fuentes", ctx -> {
          adminController.listarFuentes(ctx, modeloConSesion(ctx));
        }
    );

    app.get(
        "/admin/fuentes/{id}", ctx -> {
          adminController.editarFuente(ctx, modeloConSesion(ctx));
        }
    );
    app.post("/admin/fuentes/{id}/configurar", adminController::configurarFuente);

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

    app.get(
        "/hechos/nuevo", ctx -> {
          Boolean estaLogueado = ctx.sessionAttribute("estaLogueado");

          if (estaLogueado == null || !estaLogueado) {
            ctx.redirect("/auth/login?redirect=/hechos/nuevo");
            return;
          }
          ctx.render("hecho-nuevo.hbs", modeloConSesion(ctx));
        }
    );

    app.get("/home", ctx -> ctx.json(hechoController.findAll()));

    app.post(
        "/hechos", ctx -> {
          try {
            String titulo = ctx.formParam("titulo");
            String descripcion = ctx.formParam("descripcion");
            String categoria = ctx.formParam("categoria");
            String direccion = ctx.formParam("direccion");
            String provincia = ctx.formParam("provincia");
            String etiquteas = ctx.formParam("etiquetas");
            Long userId = ctx.sessionAttribute("usuario_id");

            List<Etiqueta> listaEtiquetas = new ArrayList<>();
            if (etiquteas != null && !etiquteas.isBlank()) {
              String[] tagsArray = etiquteas.split(",");

              for (String tagNombre : tagsArray) {
                Etiqueta nuevaEtiqueta = new Etiqueta(tagNombre.trim());
                listaEtiquetas.add(nuevaEtiqueta);
              }
            }

            LocalDateTime fechaSuceso = LocalDateTime.parse(ctx.formParam("fechaSuceso"));

            Origen origen = Origen.valueOf(ctx.formParam("origen"));

            Double lat = Double.parseDouble(ctx.formParam("latitud"));
            Double lng = Double.parseDouble(ctx.formParam("longitud"));
            PuntoGeografico ubicacion = new PuntoGeografico(lat, lng);

            List<Multimedia> listaFotos = new ArrayList<>();

            for (UploadedFile file : ctx.uploadedFiles("fotos")) {
              if (file.size() > 0) {
                Multimedia foto = new Multimedia(
                    file.filename(),
                    file.contentType(),
                    file.content()
                        .readAllBytes()
                );
                listaFotos.add(foto);
              }
            }

            Hecho nuevoHecho = new Hecho(
                titulo,
                descripcion,
                categoria,
                direccion,
                provincia,
                ubicacion,
                fechaSuceso,
                LocalDateTime.now(),
                origen,
                listaEtiquetas,
                listaFotos,
                userController.finById(userId)
            );

            // Con esto evito que se persista un archivo vacio y rompa el diseño
            if (nuevoHecho.getFotos()
                          .isEmpty()) {
              nuevoHecho.setFotos(null);
            }
            Hecho hechoGuardado = hechoController.subirHecho(nuevoHecho);

            ctx.status(201);
            ctx.redirect("/");

          }
          catch (RazonInvalidaException e) {
            ctx.status(403)
               .result(e.getMessage());
          }
          catch (Exception e) {
            // Capturamos errores de parseo (fechas, enums, nulos)
            e.printStackTrace();
            ctx.status(400)
               .result("Error en los datos del formulario: " + e.getMessage());
          }
        }
    );

    app.get(
        "/hechos", ctx -> {
          List<Hecho> hechos = hechoController.findAll();
          ctx.json(hechos);
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
          }
          catch (RazonInvalidaException e) {
            ctx.status(400);
            ctx.result("Argumento inválido: " + e.getMessage());
          }
        }
    );

    app.put(
        "/admin/solicitudes", ctx -> {
          String idParam = ctx.queryParam("id");
          String aceptadaParam = ctx.queryParam("aceptada");

          if (idParam == null || aceptadaParam == null) {
            ctx.status(400);
            ctx.result("Falta campo id o aceptada");
            return;
          }

          Long id = Long.parseLong(idParam);
          Boolean aceptada = Boolean.parseBoolean(aceptadaParam);
          Solicitud soli = SolicitudesRepository.instance()
                                                .findById(id);
          try {
            if (soli == null) {
              ctx.status(404);
              ctx.result("No existe una solicitud con ese id");
            } else if (aceptada) {
              solicitudController.aceptar(soli);
              ctx.status(200);
              ctx.json(soli);
            } else {
              solicitudController.rechazar(soli);
              ctx.status(200);
              ctx.json(soli);
            }
          }
          catch (RuntimeException e) {
            ctx.status(400);
            ctx.result("La solicitud ya ha sido analizada");
          }
        }
    );
    app.get(
        "/fuentes", ctx -> {
          Boolean soloSimples = ctx.queryParamAsClass("soloSimples", Boolean.class).getOrDefault(false);
          // Llamamos al método enriquecido, NO a findAll() directo
          ctx.json(fuenteController.obtenerFuentesConTipo(soloSimples));
        }
    );

    app.get("/admin/estadisticas", ctx -> {

      List<Estadistica> todas = estadisticaController.getEstadisticas();

      Map<String, Object> model = modeloConSesion(ctx);

      model.put("cantidadHechos",
                filtrar(todas, "CANTIDAD DE HECHOS"));

      model.put("solicitudesPendientes",
                filtrar(todas, "CANTIDAD DE SOLICITUDES PENDIENTES"));

      model.put("solicitudesSpam",
                filtrar(todas, "CANTIDAD DE SPAM"));

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
          }
          catch (RuntimeException e) {
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
        throw new io.javalin.http.ForbiddenResponse("-- ACCESO DENEGADO: SE REQUIERE EL ROL " + rol.toString()
                                                                                                   .toUpperCase() + " --");
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
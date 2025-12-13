package ar.edu.utn.frba.dds.server;

import ar.edu.utn.frba.dds.controller.AdminController;
import ar.edu.utn.frba.dds.controller.ColeccionController;
import ar.edu.utn.frba.dds.controller.EstadisticaController;
import ar.edu.utn.frba.dds.controller.FuenteController;
import ar.edu.utn.frba.dds.controller.HechoController;
import ar.edu.utn.frba.dds.controller.HomeController;
import ar.edu.utn.frba.dds.controller.SolicitudController;
import ar.edu.utn.frba.dds.controller.UserController;
import ar.edu.utn.frba.dds.dto.EstadisticaDTO;
import ar.edu.utn.frba.dds.dto.HechoDTO;
import ar.edu.utn.frba.dds.dto.SolicitudDTO;
import ar.edu.utn.frba.dds.model.coleccion.Coleccion;
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
import io.javalin.http.UploadedFile;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Router {

  public void configure(Javalin app, ObjectMapper mapper) {

    HomeController controller = new HomeController();
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
    app.before(
        "/colecciones", ctx -> {
          if ("POST".equalsIgnoreCase(String.valueOf(ctx.method()))) {
            tieneRol(Rol.ADMINISTRADOR).handle(ctx);
          }
        }
    );

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

    // Protegemos la MODIFICACIÓN de hechos
    app.before(
        "/hechos/{id}", ctx -> {
          if ("PUT".equalsIgnoreCase(String.valueOf(ctx.method()))) {
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
    app.post("/admin/fuentes", ctx -> adminController.crearFuente(ctx));
    app.post("/admin/colecciones/{id}/configurar", adminController::configurarColeccion);


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

    app.get(
        "/hechos/{id}/fotos/{indice}", ctx -> {
          Long id = Long.parseLong(ctx.pathParam("id"));
          int indice = Integer.parseInt(ctx.pathParam("indice"));

          Hecho hecho = hechoController.findById(id);

          if (hecho != null && hecho.getFotos()
                                    .size() > indice) {
            Multimedia foto = hecho.getFotos()
                                   .get(indice);
            ctx.contentType(foto.getMimetype());
            ctx.result(foto.getDatos());
          } else {
            ctx.status(404)
               .result("Foto no encontrada en DB");
          }
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

    app.get(
        "/hechos/{id}", ctx -> {
          Long id = Long.parseLong(ctx.pathParam("id"));
          Hecho hecho = hechoController.findById(id);
          if (hecho == null) {
            ctx.status(404);
            ctx.result("Not Found");
          } else {
            ctx.json(hecho);
          }
        }
    );

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
              Multimedia foto = new Multimedia(
                  file.filename(),
                  file.contentType(),
                  file.content()
                      .readAllBytes()
              );
              listaFotos.add(foto);
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

            Hecho hechoGuardado = hechoController.subirHecho(nuevoHecho);

            ctx.status(201);
            ctx.redirect("/");

          } catch (RazonInvalidaException e) {
            ctx.status(403)
               .result(e.getMessage());
          } catch (Exception e) {
            // Capturamos errores de parseo (fechas, enums, nulos)
            e.printStackTrace();
            ctx.status(400)
               .result("Error en los datos del formulario: " + e.getMessage());
          }
        }
    );

    app.put(
        "/hechos/{id}", context -> {
          Long idABuscar = Long.parseLong(context.pathParam("id"));
          Long userId = context.sessionAttribute("usuario_id");
          HechoDTO hechoModificado = context.bodyAsClass(HechoDTO.class);
          try {
            Usuario usuarioEditor = userController.finById(userId);
            Hecho hechoOriginal = hechoController.findById(idABuscar);
            hechoController.modificarHecho(hechoOriginal, hechoModificado, usuarioEditor);
            context.json(hechoOriginal);
          } catch (RuntimeException e) {
            context.status(400);
            context.result(e.getMessage());
          }
        }
    );


    app.get(
        "/hechos", ctx -> {
          List<Hecho> hechos = hechoController.findAll();
          ctx.json(hechos);
        }
    );


    app.post(
        "/solicitudes", ctx -> {
          try {
            Solicitud solicitud = solicitudController.crearSolicitud(ctx.bodyAsClass(SolicitudDTO.class));
            ctx.status(201);
            ctx.json(solicitud);
          } catch (RazonInvalidaException e) {
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
          } catch (RuntimeException e) {
            ctx.status(400);
            ctx.result("La solicitud ya ha sido analizada");
          }
        }
    );
    app.get(
        "/fuentes", ctx -> {
          List<Fuente> fuentes = fuenteController.findAll();
          ctx.json(fuentes);
        }
    );

    app.get(
        "/colecciones", ctx -> {
          List<Coleccion> colecciones = coleccionController.findAll();
          ctx.json(colecciones);
          ctx.status(200);
        }
    );

    app.get(
        "/colecciones/categorias", ctx -> {
          List<String> categorias = coleccionController.getCategorias();
          ctx.json(categorias);
          ctx.status(200);
        }
    );

    app.get(
        "/admin/estadisticas/", ctx -> {
          var todas = estadisticaController.getEstadisticas();
          ctx.json(todas);
          ctx.status(200);

          Map<String, Object> model = modeloConSesion(ctx);
          model.put("estadisticas", todas);

          ctx.render("estadisticas.hbs", model);
        }
    );

    app.post(
        "/admin/estadisticas", ctx -> {
          try {
            var stat = estadisticaController.calcularEstadistica(ctx.bodyAsClass(EstadisticaDTO.class));
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

          ctx.render("perfil.hbs", modeloConSesion(ctx));
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
    model.put("nombreUsuario", ctx.sessionAttribute("nombreUsuario"));
    model.put("rolUsuario", ctx.sessionAttribute("rolUsuario"));
    model.put("esAdmin", ctx.sessionAttribute("esAdmin"));
    model.put("esUsuario", ctx.sessionAttribute("esUsuario"));
    model.put("emailUsuario", ctx.sessionAttribute("emailUsuario"));
    return model;
  }
}
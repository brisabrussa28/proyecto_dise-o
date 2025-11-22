package ar.edu.utn.frba.dds.server;

import ar.edu.utn.frba.dds.controller.ColeccionController;
import ar.edu.utn.frba.dds.controller.EstadisticaController;
import ar.edu.utn.frba.dds.controller.FuenteController;
import ar.edu.utn.frba.dds.controller.HechoController;
import ar.edu.utn.frba.dds.controller.HomeController;
import ar.edu.utn.frba.dds.controller.SolicitudController;
import ar.edu.utn.frba.dds.controller.UserController;
import ar.edu.utn.frba.dds.dto.ColeccionDTO;
import ar.edu.utn.frba.dds.dto.EstadisticaDTO;
import ar.edu.utn.frba.dds.dto.HechoDTO;
import ar.edu.utn.frba.dds.dto.SolicitudDTO;
import ar.edu.utn.frba.dds.model.coleccion.Coleccion;
import ar.edu.utn.frba.dds.model.exceptions.RazonInvalidaException;
import ar.edu.utn.frba.dds.model.fuentes.Fuente;
import ar.edu.utn.frba.dds.model.hecho.Hecho;
import ar.edu.utn.frba.dds.model.reportes.EstadoSolicitud;
import ar.edu.utn.frba.dds.model.reportes.Solicitud;
import ar.edu.utn.frba.dds.repositories.SolicitudesRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.javalin.Javalin;
import io.javalin.http.Handler;
import io.javalin.http.UploadedFile;
import java.util.List;
import java.util.Map;

public class Router {


  public static class LoginDTO {
    private String email;
    private String password;

    public String getEmail() {
      return email;
    }

    public void setEmail(String email) {
      this.email = email;
    }

    public String getPassword() {
      return password;
    }

    public void setPassword(String password) {
      this.password = password;
    }
  }


  public void configure(Javalin app) {

    HomeController controller = new HomeController();
    HechoController hechoController = new HechoController();
    SolicitudController solicitudController = new SolicitudController();
    ColeccionController coleccionController = new ColeccionController();
    FuenteController fuenteController = new FuenteController();
    EstadisticaController estadisticaController = new EstadisticaController();
    UserController userController = new UserController(); // <-- El nuevo


    // --- 3. RUTAS DE AUTENTICACIÓN Y USUARIOS ---
    app.post("/login", userController::login);
    app.post("/logout", userController::logout);
    app.post("/usuarios", userController::crearUsuario);


    // --- 4. PROTECCIÓN DE RUTAS (app.before) ---
    app.before(
        "/colecciones", ctx -> {
          if ("POST".equalsIgnoreCase(String.valueOf(ctx.method()))) {
            tieneRol("administrador").handle(ctx);
          }
        }
    );

    // Protegemos la ACEPTACIÓN/RECHAZO de solicitudes
    app.before(
        "/solicitudes", ctx -> {
          if ("PUT".equalsIgnoreCase(String.valueOf(ctx.method()))) {
            tieneRol("administrador").handle(ctx);
          }
        }
    );

    // Protegemos la CREACIÓN de fuentes
    app.before("/fuentes", ctx -> {
      if ("POST".equalsIgnoreCase(String.valueOf(ctx.method()))) {
        tieneRol("administrador").handle(ctx);
      }
    });

    // Protegemos la MODIFICACIÓN de hechos
    app.before("/hechos/{id}", ctx -> {
      if ("PUT".equalsIgnoreCase(String.valueOf(ctx.method()))) {
        tieneRol("administrador").handle(ctx);
      }
    });


    // Protegemos TODAS las rutas de /estadisticas
    app.before("/estadisticas", ctx -> {
      if (!"OPTIONS".equalsIgnoreCase(String.valueOf(ctx.method()))) {
        tieneRol("administrador").handle(ctx);
      }
    });

    app.get("/", ctx -> ctx.json(hechoController.findAll()));

    app.get(
        "/hechos/categorias", ctx -> {
          List<String> categorias = hechoController.getCategorias();
          ctx.json(categorias);
          ctx.status(200);
        }
    );

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
//          Hecho hecho = hechoController.subirHecho(hechoDTO);
          try {
            Hecho hecho = hechoController.subirHecho(ctx.bodyAsClass(Hecho.class));
            ctx.status(201);
            ctx.json(hecho);
          } catch (RazonInvalidaException e) {
            ctx.status(403);
            ctx.result(e.getMessage());
          }

        }
    );
    app.put(
        "/hechos/{id}", context -> {
          Long idABuscar = Long.parseLong(context.pathParam("id"));
          HechoDTO hechoModificado = context.bodyAsClass(HechoDTO.class);
          try {
            Hecho hechoOriginal = hechoController.findById(idABuscar);
            hechoController.modificarHecho(hechoOriginal, hechoModificado);
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

    app.post(
        "/colecciones",
        ctx -> {
          try {
            Coleccion coleccion = coleccionController.crearColeccion(ctx.bodyAsClass(ColeccionDTO.class));
            ctx.status(201);
            ctx.json(coleccion);
          } catch (RuntimeException e) {
            ctx.status(400);
            ctx.result(e.getMessage());
          }
        }
    );

    app.put(
        "/solicitudes", ctx -> {
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

    app.post(
        "/fuentes", ctx -> {
          UploadedFile csvSubido = ctx.uploadedFile("archivoCsv");
          String nombreFuente = ctx.formParam("nombre");

          if (nombreFuente == null || nombreFuente.isEmpty()) {
            ctx.status(400)
               .json("Error: El campo 'nombre' es requerido.");
            return;
          }

          if (csvSubido != null) {
            // Es una fuente estática (CSV)
            String formatoFecha = ctx.formParam("formatoFecha");
            String separadorStr = ctx.formParam("separador");
            String columnasJson = ctx.formParam("columnas");

            if (columnasJson == null) {
              ctx.status(400)
                 .json("Error: Falta aclarar columnas para la fuente estática.");
              return;
            }

            if (formatoFecha == null || formatoFecha.isEmpty()) {
              formatoFecha = "dd/MM/yyyy";
            }

            char separador = ',';
            if (separadorStr != null && !separadorStr.isEmpty()) {
              separador = separadorStr.charAt(0);
            }

            ObjectMapper mapper = new ObjectMapper();
            Map<String, List<String>> columnas = mapper.readValue(
                columnasJson,
                new TypeReference<Map<String, List<String>>>() {
                }
            );

            ctx.json(
                fuenteController.crearFuente(
                    nombreFuente, csvSubido, formatoFecha, columnas, separador
                )
            );
            ctx.status(201);

          } else {
            // Es una fuente dinámica
            ctx.json(fuenteController.crearFuente(nombreFuente));
            ctx.status(201);
          }
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

    app.get("/estadisticas/", ctx -> {
      var todas = estadisticaController.getEstadisticas();
      ctx.json(todas);
      ctx.status(200);
    });

    app.post(
        "/estadisticas", ctx -> {
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

    app.get("/solicitudes", ctx -> {
      List<Solicitud> pendientes = SolicitudesRepository.instance().obtenerPorEstado(EstadoSolicitud.PENDIENTE);
      ctx.json(pendientes);
    });

  }

  private Handler tieneRol(String rol) {
    return ctx -> {
      String rolEnSesion = ctx.sessionAttribute("rol");

      if (rolEnSesion == null || !rolEnSesion.equalsIgnoreCase(rol)) {
        throw new io.javalin.http.ForbiddenResponse("-- ACCESO DENEGADO: SE REQUIERE EL ROL " + rol.toUpperCase() + " --");
      }
    };
  }
}
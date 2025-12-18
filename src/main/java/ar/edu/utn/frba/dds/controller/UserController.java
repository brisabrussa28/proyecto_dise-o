package ar.edu.utn.frba.dds.controller;

import ar.edu.utn.frba.dds.model.coleccion.Coleccion;
import ar.edu.utn.frba.dds.model.fuentes.Fuente;
import ar.edu.utn.frba.dds.model.hecho.Hecho;
import ar.edu.utn.frba.dds.model.hecho.multimedia.Multimedia;
import ar.edu.utn.frba.dds.model.usuario.Rol;
import ar.edu.utn.frba.dds.model.usuario.Usuario;
import ar.edu.utn.frba.dds.repositories.HechoRepository;
import ar.edu.utn.frba.dds.repositories.UserRepository;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import io.javalin.http.UploadedFile;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import javax.persistence.PersistenceException;
import org.apache.commons.codec.digest.DigestUtils;
import org.hibernate.exception.ConstraintViolationException;

public class UserController {
  private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
  ColeccionController coleccionController = new ColeccionController();
  FuenteController fuenteController = new FuenteController();

  public UserController() {
  }

  public void login(Context ctx) {
    String email = ctx.formParam("email");
    String password = ctx.formParam("password");
    String redirect = ctx.formParam("redirect");

    Map<String, Object> model = new HashMap<>();
    model.put("email", email);

    if (redirect != null && !redirect.isEmpty()) {
      model.put("redirect", redirect);
    }

    // Validación de campos vacíos
    if (esVacio(email) || esVacio(password)) {
      renderizarLoginConError(ctx, model, "Ingrese usuario y contraseña.");
      return;
    }

    try {
      Usuario usuario = UserRepository.instance()
                                      .findByEmail(email);

      // Validación de credenciales
      if (usuario == null || !DigestUtils.sha256Hex(password)
                                         .equals(usuario.getPassword())) {
        renderizarLoginConError(ctx, model, "Usuario o contraseña incorrectos.");
        return;
      }

      // Login exitoso
      iniciarSesion(ctx, usuario, email);
      String destino = (redirect != null && !redirect.isEmpty()) ? redirect : "/";
      ctx.redirect(destino);

    } catch (Exception e) {
      System.err.println("Error en login: " + e.getMessage());
      e.printStackTrace();
      renderizarLoginConError(ctx, model, "Error al iniciar sesión. Intente nuevamente.");
    }
  }

  public void logout(Context ctx) {
    ctx.req()
       .getSession()
       .invalidate();
    ctx.redirect("/");
  }

  public void register(Context ctx) {
    procesarRegistro(ctx, Rol.CONTRIBUYENTE, true);
  }

  public void registerAdmin(Context ctx) {
    procesarRegistro(ctx, Rol.ADMINISTRADOR, false);
  }

  private void procesarRegistro(Context ctx, Rol rol, boolean redirigirAlFinal) {
    String email = ctx.formParam("email");
    String password = ctx.formParam("password");
    String repeatPassword = ctx.formParam("repetir_password");
    String userName = ctx.formParam("username");
    String redirect = ctx.formParam("redirect");

    Map<String, Object> model = new HashMap<>();
    model.put("nombre", userName);
    model.put("email", email);
    if (redirect != null) {
      model.put("redirect", redirect);
    }

    // Validaciones
    if (esVacio(email) || esVacio(userName) || esVacio(password)) {
      manejarErrorRegistro(ctx, model, "Todos los campos son obligatorios.", redirigirAlFinal);
      return;
    }

    if (!EMAIL_PATTERN.matcher(email)
                      .matches()) {
      manejarErrorRegistro(ctx, model, "El email es inválido.", redirigirAlFinal);
      return;
    }

    if (!password.equals(repeatPassword)) {
      manejarErrorRegistro(ctx, model, "Las contraseñas no coinciden.", redirigirAlFinal);
      return;
    }

    if (UserRepository.instance()
                      .emailExists(email)) {
      manejarErrorRegistro(ctx, model, "El email ya está en uso.", redirigirAlFinal);
      return;
    }

    Multimedia fotoDePerfil = null;
    UploadedFile foto = ctx.uploadedFile("foto_perfil");

    if (foto != null && foto.size() > 0) {
      try {
        byte[] bytes = foto.content()
                           .readAllBytes();
        fotoDePerfil = new Multimedia("Foto de perfil", foto.contentType(), bytes);
      } catch (IOException e) {
        manejarErrorRegistro(ctx, model, "Error al procesar la imagen.", redirigirAlFinal);
        return;
      }
    }

    Usuario nuevoUsuario = new Usuario(email, userName, fotoDePerfil, password, rol);

    try {
      UserRepository.instance()
                    .guardar(nuevoUsuario);

      if (redirigirAlFinal) {
        iniciarSesion(ctx, nuevoUsuario, email);
        String destino = (redirect != null && !redirect.isEmpty()) ? redirect : "/";
        ctx.redirect(destino);
      } else {
        ctx.status(HttpStatus.CREATED)
           .json(Map.of(
               "message", "Admin creado con éxito",
               "id", nuevoUsuario.getId()
           ));
      }

    } catch (PersistenceException e) {
      if (e.getCause() instanceof ConstraintViolationException) {
        manejarErrorRegistro(
            ctx, model,
            "Error: El usuario '" + userName + "' o el email ya existe.",
            redirigirAlFinal
        );
      } else {
        e.printStackTrace();
        manejarErrorRegistro(ctx, model, "Error: Nombre de usuario no disponible.", redirigirAlFinal);
      }
    } catch (Exception e) {
      e.printStackTrace();
      manejarErrorRegistro(ctx, model, "Error inesperado: " + e.getMessage(), redirigirAlFinal);
    }
  }

  private void manejarErrorRegistro(
      Context ctx,
      Map<String, Object> model,
      String error,
      boolean esWeb
  ) {
    if (esWeb) {
      renderizarRegisterConError(ctx, model, error);
    } else {
      ctx.status(HttpStatus.BAD_REQUEST)
         .json(Map.of("error", error));
    }
  }

  private void iniciarSesion(Context ctx, Usuario usuario, String email) {
    ctx.sessionAttribute("usuario_id", usuario.getId());
    ctx.sessionAttribute("usuario_nombre", usuario.getUserName());
    ctx.sessionAttribute("usuario_rol", usuario.getRol());
    ctx.sessionAttribute("usuario_email", email);
  }

  public List<Usuario> findAll() {
    return UserRepository.instance()
                         .findAll();
  }

  public Usuario findById(Long id) {
    return UserRepository.instance()
                         .findById(id);
  }

  public Usuario findByName(String name) {
    return UserRepository.instance()
                         .findByName(name);
  }

  private boolean esVacio(String texto) {
    return texto == null || texto.trim()
                                 .isEmpty();
  }

  private void renderizarRegisterConError(Context ctx, Map<String, Object> model, String error) {
    model.put("error", error);
    // Agregar datos de sesión al modelo
    agregarDatosSesion(ctx, model);
    // NO usar status 400 para formularios web - deja que sea 200
    ctx.render("register.hbs", model);
  }

  private void renderizarLoginConError(Context ctx, Map<String, Object> model, String error) {
    model.put("error", error);
    // Agregar datos de sesión al modelo
    agregarDatosSesion(ctx, model);
    // NO usar status 400 para formularios web - deja que sea 200
    ctx.render("login.hbs", model);
  }

  private void agregarDatosSesion(Context ctx, Map<String, Object> model) {
    model.put("estaLogueado", ctx.sessionAttribute("estaLogueado"));
    model.put("nombreUsuario", ctx.sessionAttribute("usuario_nombre"));
    model.put("rolUsuario", ctx.sessionAttribute("usuario_rol"));
    model.put("esAdmin", ctx.sessionAttribute("esAdmin"));
    model.put("esUsuario", ctx.sessionAttribute("esUsuario"));
    model.put("emailUsuario", ctx.sessionAttribute("emailUsuario"));
    model.put("usuario_id", ctx.sessionAttribute("usuario_id"));
  }

  public void mostrarLogin(Context ctx) {
    mostrarVistaAutenticacion(ctx, "login.hbs");
  }

  public void mostrarRegistro(Context ctx) {
    mostrarVistaAutenticacion(ctx, "register.hbs");
  }

  private void mostrarVistaAutenticacion(Context ctx, String vista) {
    String redirect = ctx.queryParam("redirect");
    Map<String, Object> model = new HashMap<>();

    if (redirect != null && !redirect.isEmpty()) {
      model.put("redirect", redirect);
    }

    agregarDatosSesion(ctx, model);
    ctx.render(vista, model);
  }

  public void obtenerFotoPerfil(Context ctx) {
    Long id = Long.parseLong(ctx.pathParam("id"));
    Usuario usuario = findById(id);

    if (usuario != null && usuario.getFoto() != null) {
      ctx.contentType(usuario.getFoto()
                             .getMimetype());
      ctx.result(usuario.getFoto()
                        .getDatos());
    } else {
      ctx.redirect("/img/default-user.png");
    }
  }

  public void actualizarFoto(Context ctx) {
    Long idUsuario = ctx.sessionAttribute("usuario_id");
    if (idUsuario == null) {
      ctx.redirect("/auth/login");
      return;
    }

    UploadedFile archivo = ctx.uploadedFile("nueva_foto");

    if (archivo != null && archivo.size() > 0) {
      try {
        Usuario usuario = UserRepository.instance()
                                        .findById(idUsuario);

        byte[] bytes = archivo.content()
                              .readAllBytes();
        Multimedia nuevaFoto = new Multimedia("Foto de perfil", archivo.contentType(), bytes);

        usuario.setFoto(nuevaFoto);
        UserRepository.instance()
                      .guardar(usuario);

      } catch (IOException e) {
        e.printStackTrace();
        ctx.sessionAttribute("error", "Error al procesar la imagen.");
      }
    }
    ctx.redirect("/perfil");
  }

  public void listarColecciones(Context ctx, Map<String, Object> model) {
    List<Coleccion> colecciones = coleccionController.findAll();
    List<Fuente> fuentes = fuenteController.findAll();
    model.put("colecciones", colecciones);
    model.put("fuentes", fuentes);
    ctx.render("/colecciones-lista-usuario.hbs", model);
  }

  public void hechosColeccion(Context ctx, Map<String, Object> model) {
    Long id = Long.parseLong(ctx.pathParam("id"));
    Coleccion coleccion = coleccionController.findById(id);
    List<Hecho> hechosColeccion = HechoRepository.instance()
                                                 .buscarAvanzadoCompleto(
                                                     null,
                                                     null,
                                                     null,
                                                     null,
                                                     id,
                                                     null,
                                                     null,
                                                     false,
                                                     null
                                                 );

    model.put("coleccion", coleccion);
    model.put("hechosColeccion", hechosColeccion);
    ctx.render("/colecciones-detalle-usuario.hbs", model);
  }
}
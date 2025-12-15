package ar.edu.utn.frba.dds.controller;

import ar.edu.utn.frba.dds.model.usuario.Rol;
import ar.edu.utn.frba.dds.model.usuario.Usuario;
import ar.edu.utn.frba.dds.repositories.UserRepository;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import javax.persistence.PersistenceException;
import org.apache.commons.codec.digest.DigestUtils;
import org.hibernate.exception.ConstraintViolationException;

public class UserController {
  private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");

  public UserController() {
  }

  public void login(Context ctx) {
    String email = ctx.formParam("email");
    String password = ctx.formParam("password");
    String redirect = ctx.formParam("redirect");
    Map<String, Object> model = new HashMap<>();
    model.put("email", email);

    if (redirect != null) {
      model.put("redirect", redirect);
    }

    if (esVacio(email) || esVacio(password)) {
      renderizarLoginConError(ctx, model, "Ingrese usuario y contraseña.");
      return;
    }

    try {
      Usuario usuario = UserRepository.instance().findByEmail(email);

      if (usuario == null || !DigestUtils.sha256Hex(password).equals(usuario.getPassword())) {
        renderizarLoginConError(ctx, model, "Usuario o contraseña incorrectos.");
        return;
      }

      iniciarSesion(ctx, usuario, email);
      String destino = (redirect != null && !redirect.isEmpty()) ? redirect : "/";
      ctx.redirect(destino);

    } catch (Exception e) {
      e.printStackTrace();
      renderizarLoginConError(ctx, model, "Error al iniciar sesión.");
    }
  }

  public void logout(Context ctx) {
    ctx.req().getSession().invalidate();
    ctx.redirect("/");
  }

  // Registro normal (usuario contribuyente) -> Redirige al home
  public void register(Context ctx) {
    procesarRegistro(ctx, Rol.CONTRIBUYENTE, true);
  }

  // Registro Admin -> Devuelve JSON/OK (No redirige)
  public void registerAdmin(Context ctx) {
    procesarRegistro(ctx, Rol.ADMINISTRADOR, false);
  }

  /**
   * Lógica unificada de registro.
   * @param redirigirAlFinal Si es true, hace redirect. Si es false, devuelve 201 OK.
   */
  private void procesarRegistro(Context ctx, Rol rol, boolean redirigirAlFinal) {
    String email = ctx.formParam("email");
    String password = ctx.formParam("password");
    String repeatPassword = ctx.formParam("repetir_password");
    String userName = ctx.formParam("username");
    String redirect = ctx.formParam("redirect");

    Map<String, Object> model = new HashMap<>();
    model.put("nombre", userName);
    model.put("email", email);
    if (redirect != null) model.put("redirect", redirect);

    // Validaciones
    if (esVacio(email) || esVacio(userName) || esVacio(password)) {
      manejarErrorRegistro(ctx, model, "Todos los campos son obligatorios.", redirigirAlFinal);
      return;
    }

    if (!EMAIL_PATTERN.matcher(email).matches()) {
      manejarErrorRegistro(ctx, model, "El email es inválido.", redirigirAlFinal);
      return;
    }

    if (!password.equals(repeatPassword)) {
      manejarErrorRegistro(ctx, model, "Las contraseñas no coinciden.", redirigirAlFinal);
      return;
    }

    if (UserRepository.instance().emailExists(email)) {
      manejarErrorRegistro(ctx, model, "El email ya está en uso.", redirigirAlFinal);
      return;
    }

    Usuario nuevoUsuario = new Usuario(email, userName, password, rol);

    try {
      UserRepository.instance().guardar(nuevoUsuario);


      if (redirigirAlFinal) {
        iniciarSesion(ctx, nuevoUsuario, email);
        String destino = (redirect != null && !redirect.isEmpty()) ? redirect : "/";
        ctx.redirect(destino);
      } else {
        // Respuesta API para creación de admin
        ctx.status(HttpStatus.CREATED).json(Map.of("message", "Admin creado con éxito", "id", nuevoUsuario.getId()));
      }

    } catch (PersistenceException e) {
      if (e.getCause() instanceof ConstraintViolationException) {
        manejarErrorRegistro(ctx, model, "Error: El usuario '" + userName + "' o el email ya existe.", redirigirAlFinal);
      } else {
        e.printStackTrace();
        manejarErrorRegistro(ctx, model, "Error de base de datos.", redirigirAlFinal);
      }
    } catch (Exception e) {
      e.printStackTrace();
      manejarErrorRegistro(ctx, model, "Error inesperado: " + e.getMessage(), redirigirAlFinal);
    }
  }

  private void manejarErrorRegistro(Context ctx, Map<String, Object> model, String error, boolean esWeb) {
    if (esWeb) {
      renderizarRegisterConError(ctx, model, error);
    } else {
      ctx.status(HttpStatus.BAD_REQUEST).json(Map.of("error", error));
    }
  }

  private void iniciarSesion(Context ctx, Usuario usuario, String email) {
    ctx.sessionAttribute("usuario_id", usuario.getId());
    ctx.sessionAttribute("usuario_nombre", usuario.getUserName());
    ctx.sessionAttribute("usuario_rol", usuario.getRol());
    ctx.sessionAttribute("usuario_email", email);
  }

  public List<Usuario> findAll() {
    return UserRepository.instance().findAll();
  }

  public Usuario findById(Long id) {
    return UserRepository.instance().findById(id);
  }

  public Usuario finByName(String name) {
    return UserRepository.instance().findByName(name);
  }

  private boolean esVacio(String texto) {
    return texto == null || texto.trim().isEmpty();
  }

  private void renderizarRegisterConError(Context ctx, Map<String, Object> model, String error) {
    model.put("error", error);
    ctx.render("register.hbs", model);
  }

  private void renderizarLoginConError(Context ctx, Map<String, Object> model, String error) {
    model.put("error", error);
    ctx.render("login.hbs", model);
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
    ctx.render(vista, model);
  }
}
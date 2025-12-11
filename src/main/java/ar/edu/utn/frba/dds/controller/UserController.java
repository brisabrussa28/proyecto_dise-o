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

public class UserController {
  private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");

  public UserController() {
  }

  /**
   * Maneja el endpoint de Login (POST /login)
   */
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
      Usuario usuario = UserRepository.instance()
                                      .findByEmail(email);

      if (usuario == null || !DigestUtils.sha256Hex(password)
                                         .equals(usuario.getPassword())) {
        renderizarLoginConError(ctx, model, "Usuario o contraseña incorrectos.");
        return;
      }

      ctx.sessionAttribute("usuario_id", usuario.getId());
      ctx.sessionAttribute("usuario_nombre", usuario.getUserName());
      ctx.sessionAttribute("usuario_rol", usuario.getRol());

      String destino = (redirect != null && !redirect.isEmpty()) ? redirect : "/";
      ctx.redirect(destino);
    } catch (Exception e) {
      e.printStackTrace();
      renderizarLoginConError(ctx, model, "Error al iniciar sesión.");
    }

  }

  /**
   * Maneja el endpoint de Logout (POST /logout)
   */
  public void logout(Context ctx) {
    ctx.req()
       .getSession()
       .invalidate();
    ctx.redirect("/");
  }

  /**
   * Maneja el endpoint de Creación de Usuario (POST /usuarios)
   */
  public void register(Context ctx) {
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


    if (UserRepository.instance()
                      .emailExists(email)) {
      ctx.status(HttpStatus.CONFLICT)
         .result("El email ya está en uso"); // 409 Conflict
      return;
    }
    if (!EMAIL_PATTERN.matcher(email)
                      .matches()) {
      renderizarRegisterConError(ctx, model, "El formato del email no es válido.");
      return;
    }
    if (!password.equals(repeatPassword)) {
      renderizarRegisterConError(ctx, model, "Las contraseñas no coinciden.");
      return;
    }

    // Asignamos rol "Contribuyente" por defecto.
    // Podrías pasar el rol en el DTO si quisieras.
    // Se hashea la password cuando se instancia al usuario
    Usuario nuevoUsuario = new Usuario(
        email,
        userName,
        password,
        Rol.CONTRIBUYENTE
    );

    try {
      UserRepository.instance()
                    .guardar(nuevoUsuario);
      ctx.sessionAttribute("usuario_id", nuevoUsuario.getId());
      ctx.sessionAttribute("usuario_nombre", nuevoUsuario.getUserName());
      ctx.sessionAttribute("usuario_rol", nuevoUsuario.getRol());

      String destino = (redirect != null && !redirect.isEmpty()) ? redirect : "/";
      ctx.redirect(destino);
    } catch (PersistenceException e) {
      e.printStackTrace();
      renderizarRegisterConError(ctx, model, "Error: El usuario o email ya existe.");
    } catch (Exception e) {
      e.printStackTrace();
      renderizarRegisterConError(ctx, model, "Ocurrió un error inesperado.");
    }

  }

  public void registerAdmin(Context ctx) {
    String email = ctx.formParam("email");
    String password = ctx.formParam("password");
    String repeatPassword = ctx.formParam("repetir_password");
    String userName = ctx.formParam("username");

    Map<String, Object> model = new HashMap<>();
    model.put("nombre", userName);
    model.put("email", email);


    if (UserRepository.instance()
                      .emailExists(email)) {
      ctx.status(HttpStatus.CONFLICT)
         .result("El email ya está en uso"); // 409 Conflict
      return;
    }
    if (!EMAIL_PATTERN.matcher(email)
                      .matches()) {
      renderizarRegisterConError(ctx, model, "El formato del email no es válido.");
      return;
    }
    if (!password.equals(repeatPassword)) {
      renderizarRegisterConError(ctx, model, "Las contraseñas no coinciden.");
      return;
    }

    // Asignamos rol "Contribuyente" por defecto.
    // Podrías pasar el rol en el DTO si quisieras.
    // Se hashea la password cuando se instancia al usuario
    Usuario nuevoUsuario = new Usuario(
        email,
        userName,
        password,
        Rol.ADMINISTRADOR
    );

    try {
      UserRepository.instance()
                    .guardar(nuevoUsuario);
      ctx.sessionAttribute("usuario_id", nuevoUsuario.getId());
      ctx.sessionAttribute("usuario_nombre", nuevoUsuario.getUserName());
      ctx.sessionAttribute("usuario_rol", nuevoUsuario.getRol());
      ctx.redirect("/");
    } catch (PersistenceException e) {
      renderizarRegisterConError(ctx, model, "Error: El usuario o email ya existe.");
    } catch (Exception e) {
      renderizarRegisterConError(ctx, model, "Ocurrió un error inesperado.");
    }

  }

  public List<Usuario> findAll() {
    return UserRepository.instance()
                         .findAll();
  }

  public Usuario finById(Long id) {
    return UserRepository.instance()
                         .findById(id);
  }

  private boolean esVacio(String texto) {
    return texto == null || texto.trim()
                                 .isEmpty();
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
    String redirect = ctx.queryParam("redirect");

    Map<String, Object> model = new HashMap<>();

    if (redirect != null && !redirect.isEmpty()) {
      model.put("redirect", redirect);
    }

    ctx.render("login.hbs", model);
  }

  public void mostrarRegistro(Context ctx) {
    String redirect = ctx.queryParam("redirect");
    Map<String, Object> model = new HashMap<>();

    if (redirect != null && !redirect.isEmpty()) {
      model.put("redirect", redirect);
    }

    ctx.render("register.hbs", model);
  }
}
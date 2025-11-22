package ar.edu.utn.frba.dds.controller;

import ar.edu.utn.frba.dds.model.usuario.Usuario;
import ar.edu.utn.frba.dds.repositories.UserRepository;
import ar.edu.utn.frba.dds.server.Router.LoginDTO; // Reutilizamos el DTO del Router
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;

public class UserController {

  private final UserRepository userRepo;

  public UserController() {
    this.userRepo = UserRepository.instance();
  }

  /**
   * Maneja el endpoint de Login (POST /login)
   */
  public void login(Context ctx) {
    LoginDTO loginData = ctx.bodyAsClass(LoginDTO.class);

    Usuario usuario = userRepo.findByEmail(loginData.getEmail());

    if (usuario == null) {
      ctx.status(HttpStatus.UNAUTHORIZED).result("Email o password incorrectos");
      return;
    }

    // Comparamos el password
    // En un proyecto real, el password estaría hasheado
    if (usuario.chequearPassword(loginData.getPassword())) {

      // ¡Login exitoso! Guardamos los datos en la SESIÓN.
      ctx.sessionAttribute("usuarioLogueado", usuario.getEmail());
      ctx.sessionAttribute("rol", usuario.getRol());

      ctx.status(HttpStatus.OK).result("Login exitoso. Rol: " + usuario.getRol());
    } else {
      // Password incorrecto
      ctx.status(HttpStatus.UNAUTHORIZED).result("Email o password incorrectos");
    }
  }

  /**
   * Maneja el endpoint de Logout (POST /logout)
   */
  public void logout(Context ctx) {
    ctx.req().getSession().invalidate(); // Mata la sesión
    ctx.status(HttpStatus.OK).result("Sesión cerrada");
  }

  /**
   * Maneja el endpoint de Creación de Usuario (POST /usuarios)
   */
  public void crearUsuario(Context ctx) {
    LoginDTO crearData = ctx.bodyAsClass(LoginDTO.class); // Reutilizamos LoginDTO

    // Validaciones
    if (crearData.getEmail() == null || crearData.getPassword() == null) {
      ctx.status(HttpStatus.BAD_REQUEST).result("Faltan email o password");
      return;
    }

    if (userRepo.emailExists(crearData.getEmail())) {
      ctx.status(HttpStatus.CONFLICT).result("El email ya está en uso"); // 409 Conflict
      return;
    }

    // --- Lógica para hashear el password iría aquí ---
    // String passwordHasheado = BCrypt.hashpw(crearData.getPassword(), BCrypt.gensalt());
    // Por ahora, lo guardamos en texto plano (NO HACER EN PRODUCCIÓN)
    String passwordPlano = crearData.getPassword();

    // Asignamos rol "user" por defecto.
    // Podrías pasar el rol en el DTO si quisieras.
    Usuario nuevoUsuario = new Usuario(crearData.getEmail(), passwordPlano, "user");

    try {
      userRepo.guardar(nuevoUsuario);
      ctx.status(HttpStatus.CREATED).json(nuevoUsuario); // Devolvemos el usuario creado (sin el pass)
    } catch (Exception e) {
      ctx.status(HttpStatus.INTERNAL_SERVER_ERROR).result("Error al guardar usuario: " + e.getMessage());
    }
  }
}
package ar.edu.utn.frba.dds.main;

import ar.edu.utn.frba.dds.domain.coleccion.Coleccion;
import ar.edu.utn.frba.dds.domain.filtro.Filtro;
import ar.edu.utn.frba.dds.domain.fuentes.Fuente;
import ar.edu.utn.frba.dds.domain.fuentes.FuenteDinamica;
import ar.edu.utn.frba.dds.domain.fuentes.FuenteEstatica;
import ar.edu.utn.frba.dds.domain.hecho.CampoHecho;
import ar.edu.utn.frba.dds.domain.hecho.Hecho;
import ar.edu.utn.frba.dds.domain.info.PuntoGeografico;
import ar.edu.utn.frba.dds.domain.origen.Origen;
import ar.edu.utn.frba.dds.domain.reportes.GestorDeReportes;
import ar.edu.utn.frba.dds.domain.reportes.Solicitud;
import ar.edu.utn.frba.dds.domain.rol.Rol;
import ar.edu.utn.frba.dds.domain.servicioDeVisualizacion.ServicioDeVisualizacion;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Clase Usuario.
 * Representa a un usuario del sistema, que puede crear colecciones,
 * importar datos desde CSV, gestionar solicitudes y crear hechos.
 */
public class Usuario {
  protected String nombre;
  protected String email;
  protected UUID id;
  protected java.util.Set<Rol> roles;

  /**
   * Constructor.
   *
   * @param nombre Nombre del usuario
   * @param email  Email del usuario
   */
  public Usuario(String nombre, String email) {
    this.nombre = nombre;
    this.email = email;
    this.id = UUID.randomUUID();
  }

  /**
   * Constructor extendido con roles.
   *
   * @param nombre Nombre del usuario
   * @param email  Email del usuario
   * @param roles  Conjunto de roles asignados
   */
  public Usuario(String nombre, String email, java.util.Set<Rol> roles) {
    this(nombre, email);
    this.roles = roles;
  }

  /**
   * Verifica si el usuario tiene un rol específico.
   *
   * @param rol Rol a verificar
   * @return true si el usuario tiene el rol
   */
  public boolean tieneRol(Rol rol) {
    return this.roles != null && this.roles.contains(rol);
  }

  /**
   * Crear una colección de hechos.
   *
   * @param titulo      Título de la colección
   * @param descripcion Descripción de la colección
   * @param categoria   Categoría de la colección
   * @param fuente      Fuente de la colección
   * @return Colección creada
   */
  public Coleccion crearColeccion(String titulo, String descripcion, String categoria, Fuente fuente) {
    if (!tieneRol(Rol.ADMINISTRADOR)) throw new RuntimeException("No tenés permisos para usar este método.");
    return new Coleccion(titulo, fuente, descripcion, categoria);
  }

  /**
   * Importar datos desde un archivo CSV.
   *
   * @param rutaCsv      Ruta del archivo CSV
   * @param separador    Carácter separador de campos
   * @param nombreFuente Nombre de la fuente
   * @param formatoFecha Formato de fecha para los campos de fecha
   * @param mapeo        Mapeo de campos del hecho a columnas del CSV
   * @return FuenteEstatica creada a partir del CSV
   */
  public FuenteEstatica importardesdeCsv(String rutaCsv, char separador, String nombreFuente, String formatoFecha, Map<CampoHecho, List<String>> mapeo) {
    if (!tieneRol(Rol.ADMINISTRADOR)) throw new RuntimeException("No tenés permisos para usar este método.");
    if (rutaCsv == null || nombreFuente == null) {
      throw new IllegalArgumentException("Ruta y nombre de fuente deben estar definidos");
    }
    return new FuenteEstatica(nombreFuente, rutaCsv, separador, formatoFecha, mapeo);
  }

  /**
   * Importar datos desde un archivo CSV con separador por defecto (',').
   *
   * @param rutaCsv      Ruta del archivo CSV
   * @param nombreFuente Nombre de la fuente
   * @param formatoFecha Formato de fecha para los campos de fecha
   * @param mapeo        Mapeo de campos del hecho a columnas del CSV
   * @return FuenteEstatica creada a partir del CSV
   */
  public FuenteEstatica importardesdeCsv(String rutaCsv, String nombreFuente, String formatoFecha, Map<CampoHecho, List<String>> mapeo) {
    if (!tieneRol(Rol.ADMINISTRADOR)) throw new RuntimeException("No tenés permisos para usar este método.");
    return importardesdeCsv(rutaCsv, ',', nombreFuente, formatoFecha, mapeo);
  }

  /**
   * Obtener una solicitud del gestor de reportes.
   *
   * @param gestorDeReportes
   * @return Solicitud obtenida del gestor de reportes
   */
  public Solicitud obtenerSolicitud(GestorDeReportes gestorDeReportes) {
    if (!tieneRol(Rol.ADMINISTRADOR)) throw new RuntimeException("No tenés permisos para usar este método.");
    else return gestorDeReportes.obtenerSolicitud();
  }

  /**
   * Obtener una solicitud por su posición en la lista de solicitudes.
   *
   * @param posicion         Posición de la solicitud
   * @param gestorDeReportes Gestor de reportes para acceder a las solicitudes
   * @return Solicitud en la posición especificada
   */
  public Solicitud obtenerSolicitudPorPosicion(int posicion, GestorDeReportes gestorDeReportes) {
    if (!tieneRol(Rol.ADMINISTRADOR)) throw new RuntimeException("No tenés permisos para usar este método.");
    else return gestorDeReportes.obtenerSolicitudPorPosicion(posicion);
  }

  /**
   * Gestionar una solicitud, aceptándola o rechazándola.
   *
   * @param solicitud        Solicitud a gestionar
   * @param aceptarSolicitud Indica si se acepta o rechaza la solicitud
   * @param gestorDeReportes Gestor de reportes para gestionar la solicitud
   */
  public void gestionarSolicitud(Solicitud solicitud, boolean aceptarSolicitud, GestorDeReportes gestorDeReportes) {
    if (!tieneRol(Rol.ADMINISTRADOR)) throw new RuntimeException("No tenés permisos para usar este método.");
    gestorDeReportes.gestionarSolicitud(solicitud, aceptarSolicitud);
  }

  /**
   * Solicitar la eliminación de un hecho.
   *
   * @param hecho            Hecho a eliminar
   * @param motivo           Motivo de la eliminación
   * @param fuente           Fuente del hecho
   * @param gestorDeReportes Gestor de reportes para registrar la solicitud
   * @return Solicitud creada para la eliminación del hecho
   */
  public Solicitud solicitarEliminacion(Hecho hecho, String motivo, Fuente fuente, GestorDeReportes gestorDeReportes) {
    if (!tieneRol(Rol.CONTRIBUYENTE)) throw new RuntimeException("No tenés permisos para usar este método.");
    if (hecho == null || motivo == null || motivo.isBlank()) {
      throw new IllegalArgumentException("Hecho y motivo deben estar definidos");
    }
    Solicitud solicitud = new Solicitud(this, hecho, motivo);
    gestorDeReportes.agregarSolicitud(solicitud);
    return solicitud;
  }

  /**
   * Crear Hecho.
   *
   * @param fuente      FuenteDinamica
   * @param categoria   String
   * @param descripcion String
   * @param direccion   String
   * @param etiquetas   List
   * @param fecha       LocalDateTime
   * @param titulo      String
   * @param ubicacion   PuntoGeografico
   */
  public Hecho crearHecho(String titulo, String descripcion, String categoria, String direccion, PuntoGeografico ubicacion, Date fecha, List<String> etiquetas, FuenteDinamica fuente) {
    if (!tieneRol(Rol.CONTRIBUYENTE)) throw new RuntimeException("No tenés permisos para usar este método.");
    Hecho hecho = new Hecho(titulo, descripcion, categoria, direccion, ubicacion, fecha, Date.from(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant()), Origen.PROVISTO_CONTRIBUYENTE, etiquetas);
    fuente.agregarHecho(hecho);
    return hecho;
  }
  // Sume los metodos como una fachada (Façade pattern) para facilitar el acceso desde el punto de vista del dominio o del código cliente.

  /**
   * Visualizar hechos de una colección.
   *
   * @param coleccion Colección de hechos
   * @param gestor    Gestor de reportes
   * @param servicio  Servicio de visualización
   * @return Lista de hechos visualizados
   */

  public List<Hecho> visualizarHechos(Coleccion coleccion, GestorDeReportes gestor, ServicioDeVisualizacion servicio) {
    if (!tieneRol(Rol.VISUALIZADOR)) {
      throw new RuntimeException("No tenés permisos para visualizar hechos.");
    }
    return servicio.obtenerHechosColeccion(coleccion, gestor);
  }

  /**
   * Filtrar hechos de una colección.
   *
   * @param coleccion Colección de hechos
   * @param filtro    Filtro a aplicar
   * @param gestor    Gestor de reportes
   * @param servicio  Servicio de visualización
   * @return Lista de hechos filtrados
   */
  public List<Hecho> filtrarHechos(Coleccion coleccion, Filtro filtro, GestorDeReportes gestor, ServicioDeVisualizacion servicio) {
    if (!tieneRol(Rol.VISUALIZADOR)) {
      throw new RuntimeException("No tenés permisos para filtrar hechos.");
    }
    return servicio.filtrarHechosColeccion(coleccion, filtro, gestor);
  }


  /**
   * Obtiene el ID del usuario.
   *
   * @return UUID del usuario
   */
  public String getNombre() {
    return nombre;
  }

  /**
   * Obtiene el email del usuario.
   *
   * @return Email del usuario
   */
  public String getEmail() {
    return email;
  }
}

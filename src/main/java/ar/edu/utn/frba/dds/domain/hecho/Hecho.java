package ar.edu.utn.frba.dds.domain.hecho;

import ar.edu.utn.frba.dds.domain.info.PuntoGeografico;
import ar.edu.utn.frba.dds.domain.origen.Origen;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * Hecho.
 */
public class Hecho {
  private final List<String> etiquetas;
  private final UUID id;
  private final UUID idUsuarioCreador;
  private String titulo;
  private String descripcion;
  private String categoria;
  private String direccion;
  private PuntoGeografico ubicacion;
  private LocalDateTime fechaSuceso;
  private LocalDateTime fechaCarga;
  private Origen fuenteOrigen;

  /**
   * Constructor básico.
   *
   * @param titulo       string
   * @param descripcion  string
   * @param categoria    string
   * @param direccion    string
   * @param ubicacion    PuntoGeografico
   * @param fechaSuceso  LocalDateTime
   * @param fechaCarga   LocalDateTime
   * @param fuenteOrigen Origen
   * @param etiquetas    List<String>
   */
  public Hecho(
      String titulo,
      String descripcion,
      String categoria,
      String direccion,
      PuntoGeografico ubicacion,
      LocalDateTime fechaSuceso,
      LocalDateTime fechaCarga,
      Origen fuenteOrigen,
      List<String> etiquetas
  ) {
    this(titulo, descripcion, categoria, direccion, ubicacion, fechaSuceso, fechaCarga, fuenteOrigen, etiquetas, null);
  }

  /**
   * Constructor completo.
   *
   * @param titulo           string
   * @param descripcion      string
   * @param categoria        string
   * @param direccion        string
   * @param ubicacion        PuntoGeografico
   * @param fechaSuceso      LocalDateTime
   * @param fechaCarga       LocalDateTime
   * @param fuenteOrigen     Origen
   * @param etiquetas        List<String>
   * @param idUsuarioCreador UUID
   */
  public Hecho(
      String titulo,
      String descripcion,
      String categoria,
      String direccion,
      PuntoGeografico ubicacion,
      LocalDateTime fechaSuceso,
      LocalDateTime fechaCarga,
      Origen fuenteOrigen,
      List<String> etiquetas,
      UUID idUsuarioCreador
  ) {
    this.titulo = titulo;
    this.descripcion = descripcion;
    this.categoria = categoria;
    this.ubicacion = ubicacion;
    this.direccion = direccion;
    this.fechaSuceso = fechaSuceso;
    this.fechaCarga = fechaCarga;
    this.fuenteOrigen = fuenteOrigen;
    this.etiquetas = new ArrayList<>(etiquetas);
    this.id = UUID.randomUUID();
    this.idUsuarioCreador = idUsuarioCreador;
  }

  /**
   * Obtiene el ID del hecho.
   *
   * @return UUID del hecho
   */
  public UUID getId() {
    return id;
  }

  /**
   * Obtiene el titulo del hecho.
   *
   * @return string titulo del hecho
   */
  public String getTitulo() {
    return titulo;
  }

  /**
   * Obtiene la descripción del hecho.
   *
   * @return string descripción del hecho
   */
  public String getDescripcion() {
    return descripcion;
  }

  /**
   * Obtiene la categoría del hecho.
   *
   * @return string categoría del hecho
   */
  public String getCategoria() {
    return categoria;
  }

  /**
   * Obtiene la dirección del hecho.
   *
   * @return string dirección del hecho
   */
  public String getDireccion() {
    return direccion;
  }

  /**
   * Obtiene la ubicación del hecho.
   *
   * @return PuntoGeografico ubicación del hecho
   */
  public PuntoGeografico getUbicacion() {
    return ubicacion;
  }

  /**
   * Obtiene la fecha del suceso del hecho.
   *
   * @return Date fecha del suceso
   */
  public LocalDateTime getFechaSuceso() {
    return fechaSuceso;
  }

  /**
   * Obtiene la fecha de carga del hecho.
   *
   * @return Date fecha de carga del hecho
   */
  public LocalDateTime getFechaCarga() {
    return fechaCarga;
  }

  /**
   * Obtiene el origen del hecho.
   *
   * @return Origen fuente del hecho
   */
  public Origen getOrigen() {
    return fuenteOrigen;
  }

  /**
   * Obtiene el ID del usuario creador del hecho.
   *
   * @return UUID del usuario creador
   */
  public List<String> getEtiquetas() {
    return new ArrayList<>(this.etiquetas);
  }

  /**
   * verifica si el hecho es de una categoría específica.
   *
   * @param categoria categoría a verificar
   * @return true si es de la categoría, false en caso contrario
   */
  public boolean esDeCategoria(String categoria) {
    return this.categoria.equals(categoria);
  }

  /**
   * Verifica si el hecho tiene una etiqueta específica.
   *
   * @param etiqueta etiqueta a verificar
   * @return true si tiene la etiqueta, false en caso contrario
   */
  public boolean tieneEtiqueta(String etiqueta) {
    return this.etiquetas.contains(etiqueta);
  }

  /**
   * Verifica si el hecho es de un título específico.
   *
   * @param titulo título a verificar
   * @return true si es del título, false en caso contrario
   */
  public boolean esDeTitulo(String titulo) {
    return this.titulo.equals(titulo);
  }

  /**
   * Verifica si el hecho es de una descripción específica.
   *
   * @param direccion descripción a verificar
   * @return true si es de la descripción, false en caso contrario
   */
  public boolean sucedioEn(String direccion) {
    return this.direccion.equals(direccion);
  }

  /**
   * Verifica si el hecho es de una fecha específica.
   *
   * @param fecha fecha a verificar
   * @return true si es de la fecha, false en caso contrario
   */
  public boolean esDeFecha(Date fecha) {
    return this.fechaSuceso.equals(fecha);
  }

  /**
   * Verifica si el hecho ocurrió antes de una fecha específica.
   *
   * @param fecha fecha a verificar
   * @return true si ocurrió antes de la fecha, false en caso contrario
   */
  public boolean seCargoEl(Date fecha) {
    return this.fechaCarga.equals(fecha);
  }

  /**
   * Verifica si el hecho ocurrió antes de una fecha específica.
   *
   * @param fecha fecha a verificar
   * @return true si ocurrió antes de la fecha, false en caso contrario
   */
  public boolean seCargoAntesDe(LocalDateTime fecha) {
    return this.fechaCarga.isBefore(fecha);
  }

  /**
   * Verifica si el hecho es de un origen específico.
   *
   * @param origen origen a verificar
   * @return true si es del origen, false en caso contrario
   */
  public boolean esDeOrigen(Origen origen) {
    return this.fuenteOrigen.equals(origen);
  }

  /**
   * Verifica si el hecho es de un lugar específico.
   *
   * @param lugar PuntoGeografico a verificar
   * @return true si es del lugar, false en caso contrario
   */
  public boolean esDeLugar(PuntoGeografico lugar) {
    return this.ubicacion.equals(lugar);
  }

  /**
   * Verifica si el hecho está vacío.
   * Un hecho se considera vacío si no tiene título, descripción, categoría, dirección,
   * ubicación, fecha de suceso o etiquetas.
   *
   * @return true si el hecho está vacío, false en caso contrario
   */
  public boolean estaVacio() {
    return (titulo == null || titulo.isBlank()) &&
        (descripcion == null || descripcion.isBlank()) &&
        (categoria == null || categoria.isBlank()) &&
        (direccion == null || direccion.isBlank()) &&
        ubicacion == null &&
        fechaSuceso == null &&
        (etiquetas == null || etiquetas.isEmpty());
  }

  /**
   * Verifica si el hecho es editable por un usuario específico.
   * Un hecho es editable por su creador durante una semana desde su fecha de carga.
   *
   * @param idUsuarioEditor ID del usuario que intenta editar el hecho
   * @return true si el hecho es editable por el usuario, false en caso contrario
   */
  public boolean esEditablePor(UUID idUsuarioEditor) {
    if (this.idUsuarioCreador == null || !this.idUsuarioCreador.equals(idUsuarioEditor)) {
      return false;
    }
    LocalDateTime hoy = LocalDateTime.now();
    LocalDateTime fechaDeCarga = this.fechaCarga;
    return hoy.isBefore(fechaDeCarga.plusWeeks(1));
  }

  /**
   * Edita los detalles del hecho.
   * Permite cambiar el título, descripción, categoría, dirección, ubicación,
   * etiquetas y fecha de suceso del hecho si el usuario tiene permisos para editarlo.
   *
   * @param idUsuarioEditor  ID del usuario que intenta editar el hecho
   * @param nuevoTitulo      Nuevo título del hecho
   * @param nuevaDescripcion Nueva descripción del hecho
   * @param nuevaCategoria   Nueva categoría del hecho
   * @param nuevaDireccion   Nueva dirección del hecho
   * @param nuevaUbicacion   Nueva ubicación del hecho
   * @param nuevasEtiquetas  Nuevas etiquetas del hecho
   * @param nuevaFechaSuceso Nueva fecha de suceso del hecho
   * @return true si la edición fue exitosa, false si el usuario no tiene permisos para editarlo
   */
  public boolean editarHecho(
      UUID idUsuarioEditor,
      String nuevoTitulo,
      String nuevaDescripcion,
      String nuevaCategoria,
      String nuevaDireccion,
      PuntoGeografico nuevaUbicacion,
      List<String> nuevasEtiquetas,
      LocalDateTime nuevaFechaSuceso
  ) {
    if (!this.esEditablePor(idUsuarioEditor)) {
      return false;
    }

    if (nuevoTitulo != null && !nuevoTitulo.isBlank()) {
      this.titulo = nuevoTitulo;
    }
    if (nuevaDescripcion != null && !nuevaDescripcion.isBlank()) {
      this.descripcion = nuevaDescripcion;
    }
    if (nuevaCategoria != null && !nuevaCategoria.isBlank()) {
      this.categoria = nuevaCategoria;
    }
    if (nuevaDireccion != null && !nuevaDireccion.isBlank()) {
      this.direccion = nuevaDireccion;
    }
    if (nuevaUbicacion != null) {
      this.ubicacion = nuevaUbicacion;
    }
    if (nuevasEtiquetas != null) {
      this.etiquetas.clear();
      this.etiquetas.addAll(nuevasEtiquetas);
    }
    if (nuevaFechaSuceso != null) {
      this.fechaSuceso = nuevaFechaSuceso;
    }

    return true;
  }
}

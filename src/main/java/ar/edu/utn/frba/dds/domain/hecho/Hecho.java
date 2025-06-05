package ar.edu.utn.frba.dds.domain.hecho;

import ar.edu.utn.frba.dds.domain.info.PuntoGeografico;
import ar.edu.utn.frba.dds.domain.origen.Origen;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Hecho.
 */
public class Hecho {
  private final List<String> etiquetas;
  private final UUID id;
  private final UUID idUsuarioCreador;
  private final LocalDateTime fechaCarga;
  private final Origen fuenteOrigen;
  private String titulo;
  private String descripcion;
  private String categoria;
  private String direccion;
  private PuntoGeografico ubicacion;
  private LocalDateTime fechaSuceso;

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
   * @param etiquetas    List
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
    this(
        titulo,
        descripcion,
        categoria,
        direccion,
        ubicacion,
        fechaSuceso,
        fechaCarga,
        fuenteOrigen,
        etiquetas,
        null
    );
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
   * @param etiquetas        List
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
    if (fechaSuceso != null && fechaCarga != null) {
      if (fechaSuceso.isAfter(fechaCarga)) {
        throw new RuntimeException("fechaSuceso no puede ser posterior a fechaCarga");
      }
      if (fechaSuceso.isAfter(LocalDateTime.now()) || fechaCarga.isAfter(LocalDateTime.now())) {
        throw new RuntimeException("Las fechs no pueden ser futuras");
      }
    }
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
    return hoy.isBefore(fechaCarga.plusWeeks(1));
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
      if (this.fechaCarga != null) {
        if (nuevaFechaSuceso.isAfter(LocalDateTime.now())) {
          throw new RuntimeException("Fecha de suceso no puede ser posterior al momento actual");
        }
      }
      this.fechaSuceso = nuevaFechaSuceso;
    }

    return true;
  }
}

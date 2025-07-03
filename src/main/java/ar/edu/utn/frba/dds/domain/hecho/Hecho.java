package ar.edu.utn.frba.dds.domain.hecho;

import ar.edu.utn.frba.dds.domain.info.PuntoGeografico;
import ar.edu.utn.frba.dds.domain.origen.Origen;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Hecho.
 */
public class Hecho {
  // Los campos que eran 'final' ahora son no-final para permitir la deserialización de Jackson
  private List<String> etiquetas;
  private UUID id;
  private UUID idUsuarioCreador;
  private LocalDateTime fechaCarga;
  private Origen fuenteOrigen; // Ya no es final
  private String titulo;
  private String descripcion;
  private String categoria;
  private String direccion;
  private PuntoGeografico ubicacion;
  private LocalDateTime fechaSuceso;
  private Estado estado;
  // Constructor público sin argumentos: NECESARIO para la deserialización de Jackson

  /**
   * Constructor para un Hecho.
   */
  public Hecho() {
    this.etiquetas = new ArrayList<>(); // Inicializar para evitar NullPointerException
    this.id = UUID.randomUUID(); // Generar un ID por defecto (puede ser sobrescrito por JSON)
    this.idUsuarioCreador = null; // Puede ser sobrescrito por JSON
    this.fechaCarga = LocalDateTime.now(); // Puede ser sobrescrito por JSON
    this.fuenteOrigen = null; // Puede ser sobrescrito por JSON
    this.estado = Estado.ORIGINAL;
  }

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
   * @return true si el hecho es editable por el usuario, false en caso contrario
   */
  public boolean esEditable() {
    LocalDateTime hoy = LocalDateTime.now();
    return hoy.isBefore(fechaCarga.plusWeeks(1));
  }

  // Setters para los campos que no son 'final' y que Jackson necesita para la deserialización
  public void setTitulo(String titulo) {
    this.titulo = titulo;
  }

  public void setDescripcion(String descripcion) {
    this.descripcion = descripcion;
  }

  public void setCategoria(String categoria) {
    this.categoria = categoria;
  }

  public void setDireccion(String direccion) {
    this.direccion = direccion;
  }

  public void setUbicacion(PuntoGeografico ubicacion) {
    this.ubicacion = ubicacion;
  }

  public void setFechaSuceso(LocalDateTime fechaSuceso) {
    this.fechaSuceso = fechaSuceso;
  }

  // Nuevos setters para los campos que eran 'final' y ahora son no-final
  public void setEtiquetas(List<String> etiquetas) {
    this.etiquetas = new ArrayList<>(etiquetas);
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public void setIdUsuarioCreador(UUID idUsuarioCreador) {
    this.idUsuarioCreador = idUsuarioCreador;
  }

  public void setEstado(Estado estado) {
    this.estado = estado;
  }

  public void setFechaCarga(LocalDateTime fechaCarga) {
    this.fechaCarga = fechaCarga;
  }

  // Anotación para mapear la propiedad JSON "origen" al setter de 'fuenteOrigen'
  @JsonProperty("origen")
  public void setFuenteOrigen(Origen fuenteOrigen) {
    this.fuenteOrigen = fuenteOrigen;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Hecho hecho = (Hecho) o;
    return Objects.equals(titulo, hecho.titulo) &&
        Objects.equals(descripcion, hecho.descripcion) &&
        Objects.equals(categoria, hecho.categoria) &&
        Objects.equals(direccion, hecho.direccion) &&
        Objects.equals(ubicacion, hecho.ubicacion) &&
        Objects.equals(fechaSuceso, hecho.fechaSuceso);
  }

  @Override
  public int hashCode() {
    return Objects.hash(titulo, descripcion, categoria, direccion, ubicacion, fechaSuceso);
  }
}

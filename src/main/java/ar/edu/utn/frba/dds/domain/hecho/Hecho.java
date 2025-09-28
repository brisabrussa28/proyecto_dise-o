package ar.edu.utn.frba.dds.domain.hecho;

import ar.edu.utn.frba.dds.domain.hecho.etiqueta.Etiqueta;
import ar.edu.utn.frba.dds.domain.info.PuntoGeografico;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.FullTextField;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.Indexed;

/**
 * Hecho.
 */

@Entity
@Indexed
public class Hecho {
  // Los campos que eran 'final' ahora son no-final para permitir la deserialización de Jackson
  @Id
  @GeneratedValue
  Long id;
  @OneToMany
  private List<Etiqueta> etiquetas;
  private LocalDateTime fechaCarga;
  private Origen fuenteOrigen; // Ya no es final
  @FullTextField
  private String titulo;
  @FullTextField
  private String descripcion;
  private String categoria;
  private String direccion;
  @Embedded
  private PuntoGeografico ubicacion;
  private LocalDateTime fechaSuceso;
  private Estado estado;
  private String provincia;
  // Constructor público sin argumentos: NECESARIO para la deserialización de Jackson

  /**
   * Constructor para un Hecho.
   */
  public Hecho() {
    this.fechaCarga = LocalDateTime.now();
    this.estado = Estado.ORIGINAL;
    this.etiquetas = new ArrayList<>();
  }

  /**
   * Constructor completo.
   *
   * @param titulo       string
   * @param descripcion  string
   * @param categoria    string
   * @param direccion    string
   * @param provincia    string
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
      String provincia,
      PuntoGeografico ubicacion,
      LocalDateTime fechaSuceso,
      LocalDateTime fechaCarga,
      Origen fuenteOrigen,
      List<Etiqueta> etiquetas
  ) {
    this.titulo = titulo;
    this.descripcion = descripcion;
    this.categoria = categoria;
    this.ubicacion = ubicacion;
    this.direccion = direccion;
    this.fechaSuceso = fechaSuceso;
    this.fechaCarga = fechaCarga;
    this.fuenteOrigen = fuenteOrigen;
    this.etiquetas = new ArrayList<>();
    this.provincia = provincia;
    this.estado = Estado.ORIGINAL;
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

  public Long getId() {
    return id;
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
  public LocalDateTime getFechasuceso() {
    return fechaSuceso;
  }

  /**
   * Obtiene la fecha de carga del hecho.
   *
   * @return Date fecha de carga del hecho
   */
  public LocalDateTime getFechacarga() {
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
  public List<Etiqueta> getEtiquetas() {
    return new ArrayList<>(this.etiquetas);
  }

  /**
   * Obtiene el estado del del hecho.
   *
   * @return estado del del hecho.
   */
  public Estado getEstado() {
    return estado;
  }

  public String getProvincia() {
    return provincia;
  }

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

  public void setFechasuceso(LocalDateTime fechaSuceso) {
    this.fechaSuceso = fechaSuceso;
  }

  public void setEtiquetas(List<Etiqueta> etiquetas) {
    this.etiquetas = new ArrayList<>(etiquetas);
  }

  public void setEstado(Estado estado) {
    this.estado = estado;
  }

  public void setFechacarga(LocalDateTime fechaCarga) {
    this.fechaCarga = fechaCarga;
  }

  public void setProvincia(String provincia) {
    this.provincia = provincia;
  }

  @JsonProperty("origen")
  public void setOrigen(Origen fuenteOrigen) {
    this.fuenteOrigen = fuenteOrigen;
  }


  /**
   * Verifica si el hecho es editable por un usuario específico.
   * Un hecho es editable por su creador durante una semana desde su fecha de carga.
   *
   * @return true si el hecho es editable por el usuario, false en caso contrario
   */
  public boolean esEditable() {
    return LocalDateTime.now()
                        .isBefore(fechaCarga.plusWeeks(1));
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Hecho hecho = (Hecho) o;
    return Objects.equals(titulo, hecho.titulo)
        && Objects.equals(descripcion, hecho.descripcion)
        && Objects.equals(categoria, hecho.categoria)
        && Objects.equals(direccion, hecho.direccion)
        && Objects.equals(ubicacion, hecho.ubicacion)
        && Objects.equals(fechaSuceso, hecho.fechaSuceso);
  }

  @Override
  public int hashCode() {
    return Objects.hash(titulo, descripcion, categoria, direccion, ubicacion, fechaSuceso);
  }


}

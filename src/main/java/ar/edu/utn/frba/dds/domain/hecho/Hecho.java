package ar.edu.utn.frba.dds.domain.hecho;

import ar.edu.utn.frba.dds.domain.hecho.etiqueta.Etiqueta;
import ar.edu.utn.frba.dds.domain.info.PuntoGeografico;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinTable;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.FullTextField;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.Indexed;

/**
 * Hecho.
 */

@Entity
@Indexed
public class Hecho {
  // Quiza deberiamos poner el allocation a 10 pq la mayoria de los hechos se cargan en bulk
  @Id
  @SequenceGenerator(name = "hecho_seq", sequenceName = "hecho_sequence", allocationSize = 1)
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "hecho_seq")
  @Column(name = "hecho_id")
  Long id;
  @OneToMany
  @JoinTable(name = "hecho_etiqueta")
  private List<Etiqueta> etiquetas;
  private LocalDateTime hecho_fecha_carga;
  @Enumerated(EnumType.STRING)
  @Column(name = "hecho_origen", nullable = false)
  private Origen fuenteOrigen; // Ya no es final
  @FullTextField
  private String hecho_titulo;
  @FullTextField
  private String hecho_descripcion;
  private String hecho_categoria;
  private String hecho_direccion;
  @Embedded
  private PuntoGeografico hecho_ubicacion;
  private LocalDateTime hecho_fecha_suceso;
  @Enumerated(EnumType.STRING)
  @Column(name = "hecho_estado", nullable = false)
  private Estado estado;
  private String hecho_provincia;

  /**
   * Constructor para un Hecho.
   */
  public Hecho() {
    this.hecho_fecha_carga = LocalDateTime.now();
    this.estado = Estado.ORIGINAL;
    this.etiquetas = new ArrayList<>();
  }

  /**
   * Constructor completo.
   *
   * @param hecho_titulo       string
   * @param hecho_descripcion  string
   * @param hecho_categoria    string
   * @param hecho_direccion    string
   * @param hecho_provincia    string
   * @param hecho_ubicacion    PuntoGeografico
   * @param hecho_fecha_suceso LocalDateTime
   * @param hecho_fecha_carga         LocalDateTime
   * @param fuenteOrigen       Origen
   * @param etiquetas          List
   */
  public Hecho(
      String hecho_titulo,
      String hecho_descripcion,
      String hecho_categoria,
      String hecho_direccion,
      String hecho_provincia,
      PuntoGeografico hecho_ubicacion,
      LocalDateTime hecho_fecha_suceso,
      LocalDateTime hecho_fecha_carga,
      Origen fuenteOrigen,
      List<Etiqueta> etiquetas
  ) {
    this.hecho_titulo = hecho_titulo;
    this.hecho_descripcion = hecho_descripcion;
    this.hecho_categoria = hecho_categoria;
    this.hecho_ubicacion = hecho_ubicacion;
    this.hecho_direccion = hecho_direccion;
    this.hecho_fecha_suceso = hecho_fecha_suceso;
    this.hecho_fecha_carga = hecho_fecha_carga;
    this.fuenteOrigen = fuenteOrigen;
    this.etiquetas = new ArrayList<>();
    this.hecho_provincia = hecho_provincia;
    this.estado = Estado.ORIGINAL;
  }

  /**
   * Obtiene el titulo del hecho.
   *
   * @return string titulo del hecho
   */
  public String getHecho_titulo() {
    return hecho_titulo;
  }

  /**
   * Obtiene la descripción del hecho.
   *
   * @return string descripción del hecho
   */
  public String getHecho_descripcion() {
    return hecho_descripcion;
  }

  public Long getId() {
    return id;
  }

  /**
   * Obtiene la categoría del hecho.
   *
   * @return string categoría del hecho
   */
  public String getHecho_categoria() {
    return hecho_categoria;
  }

  /**
   * Obtiene la dirección del hecho.
   *
   * @return string dirección del hecho
   */
  public String getHecho_direccion() {
    return hecho_direccion;
  }

  /**
   * Obtiene la ubicación del hecho.
   *
   * @return PuntoGeografico ubicación del hecho
   */
  public PuntoGeografico getHecho_ubicacion() {
    return hecho_ubicacion;
  }

  /**
   * Obtiene la fecha del suceso del hecho.
   *
   * @return Date fecha del suceso
   */
  public LocalDateTime getFechasuceso() {
    return hecho_fecha_suceso;
  }

  /**
   * Obtiene la fecha de carga del hecho.
   *
   * @return Date fecha de carga del hecho
   */
  public LocalDateTime getFechacarga() {
    return hecho_fecha_carga;
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

  public String getHecho_provincia() {
    return hecho_provincia;
  }

  public void setHecho_titulo(String hecho_titulo) {
    this.hecho_titulo = hecho_titulo;
  }

  public void setHecho_descripcion(String hecho_descripcion) {
    this.hecho_descripcion = hecho_descripcion;
  }

  public void setHecho_categoria(String hecho_categoria) {
    this.hecho_categoria = hecho_categoria;
  }

  public void setHecho_direccion(String hecho_direccion) {
    this.hecho_direccion = hecho_direccion;
  }

  public void setHecho_ubicacion(PuntoGeografico hecho_ubicacion) {
    this.hecho_ubicacion = hecho_ubicacion;
  }

  public void setFechasuceso(LocalDateTime fechaSuceso) {
    this.hecho_fecha_suceso = fechaSuceso;
  }

  public void setEtiquetas(List<Etiqueta> etiquetas) {
    this.etiquetas = new ArrayList<>(etiquetas);
  }

  public void setEstado(Estado estado) {
    this.estado = estado;
  }

  public void setFechacarga(LocalDateTime fechaCarga) {
    this.hecho_fecha_carga = fechaCarga;
  }

  public void setHecho_provincia(String hecho_provincia) {
    this.hecho_provincia = hecho_provincia;
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
                        .isBefore(hecho_fecha_carga.plusWeeks(1));
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
    return Objects.equals(hecho_titulo, hecho.hecho_titulo)
        && Objects.equals(hecho_descripcion, hecho.hecho_descripcion)
        && Objects.equals(hecho_categoria, hecho.hecho_categoria)
        && Objects.equals(hecho_direccion, hecho.hecho_direccion)
        && Objects.equals(hecho_ubicacion, hecho.hecho_ubicacion)
        && Objects.equals(hecho_fecha_suceso, hecho.hecho_fecha_suceso);
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        hecho_titulo,
        hecho_descripcion, hecho_categoria, hecho_direccion,
        hecho_ubicacion, hecho_fecha_suceso
    );
  }


}

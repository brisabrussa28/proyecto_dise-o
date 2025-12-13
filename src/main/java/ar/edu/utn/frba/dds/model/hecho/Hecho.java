package ar.edu.utn.frba.dds.model.hecho;

import ar.edu.utn.frba.dds.model.coleccion.Coleccion;
import ar.edu.utn.frba.dds.model.hecho.etiqueta.Etiqueta;
import ar.edu.utn.frba.dds.model.hecho.multimedia.Multimedia;
import ar.edu.utn.frba.dds.model.info.PuntoGeografico;
import ar.edu.utn.frba.dds.model.usuario.Usuario;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.FullTextField;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.Indexed;

/**
 * Hecho.
 */

@Entity
@Indexed
@Table(name = "Hecho")
public class Hecho {
  // Quiza deberiamos poner el allocation a 10 pq la mayoria de los hechos se cargan en bulk
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "hecho_id")
  Long id;

  @ElementCollection
  private List<Etiqueta> etiquetas = new ArrayList<>();

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

  @ElementCollection(fetch = FetchType.LAZY)
  @CollectionTable(
      name = "hecho_foto",
      joinColumns = @JoinColumn(name = "hecho_id")
  )
  List<Multimedia> fotos;

  @ManyToOne
  @JoinColumn(name = "hecho_autor")
  @JsonIgnoreProperties("hechos")
  private Usuario autor;

  @ManyToMany(mappedBy = "hechos")
  @JsonIgnore
  private Set<Coleccion> colecciones = new HashSet<>();

  /**
   * Constructor vacio para un Hecho.
   */
  public Hecho() {
    this.hecho_fecha_carga = LocalDateTime.now();
    this.estado = Estado.ORIGINAL;
    this.etiquetas = new ArrayList<>();
    this.fotos = new ArrayList<>();
  }

  /**
   * Constructor completo (12 parametros).
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
      List<Etiqueta> etiquetas,
      List<Multimedia> fotos,
      Usuario autor
  ) {
    this.hecho_titulo = titulo;
    this.hecho_descripcion = descripcion;
    this.hecho_categoria = categoria;
    this.hecho_ubicacion = ubicacion;
    this.hecho_direccion = direccion;
    this.hecho_fecha_suceso = fechaSuceso;
    this.hecho_fecha_carga = fechaCarga;
    this.fuenteOrigen = fuenteOrigen;
    this.etiquetas = etiquetas;
    this.hecho_provincia = provincia;
    this.estado = Estado.ORIGINAL;
    this.fotos = fotos;
    this.autor = autor;
  }

  /**
   * Constructor LEGACY para Tests (10 parametros).
   * Mantiene compatibilidad con los tests que no envian fotos ni autor.
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
    // Llamamos al constructor principal pasando listas vacias y null
    this(
        titulo, descripcion, categoria, direccion, provincia, ubicacion,
        fechaSuceso, fechaCarga, fuenteOrigen, etiquetas,
        new ArrayList<>(), // Fotos vacias
        null               // Autor nulo
    );
  }


  /**
   * Obtiene el titulo del hecho.
   *
   * @return string titulo del hecho
   */
  @JsonProperty("hecho_titulo")
  public String getTitulo() {
    return hecho_titulo;
  }

  /**
   * Obtiene la descripción del hecho.
   *
   * @return string descripción del hecho
   */
  @JsonProperty("hecho_descripcion")
  public String getDescripcion() {
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
  @JsonProperty("hecho_categoria")
  public String getCategoria() {
    return hecho_categoria;
  }

  /**
   * Obtiene la dirección del hecho.
   *
   * @return string dirección del hecho
   */
  @JsonProperty("hecho_direccion")
  public String getDireccion() {
    return hecho_direccion;
  }

  /**
   * Obtiene la ubicación del hecho.
   *
   * @return PuntoGeografico ubicación del hecho
   */
  @JsonProperty("hecho_ubicacion")
  public PuntoGeografico getUbicacion() {
    return hecho_ubicacion;
  }

  /**
   * Obtiene la fecha del suceso del hecho.
   *
   * @return Date fecha del suceso
   */
  @JsonProperty("hecho_fecha_suceso")
  public LocalDateTime getFechasuceso() {
    return hecho_fecha_suceso;
  }

  /**
   * Obtiene la fecha de carga del hecho.
   *
   * @return Date fecha de carga del hecho
   */
  @JsonProperty("hecho_fecha_carga")
  public LocalDateTime getFechacarga() {
    return hecho_fecha_carga;
  }

  /**
   * Obtiene el origen del hecho.
   *
   * @return Origen fuente del hecho
   */
  @JsonProperty("hecho_origen")
  public Origen getOrigen() {
    return fuenteOrigen;
  }

  /**
   * Obtiene el ID del usuario creador del hecho.
   *
   * @return UUID del usuario creador
   */
  @JsonProperty("hecho_etiquetas")
  public List<Etiqueta> getEtiquetas() {
    return new ArrayList<>(this.etiquetas);
  }

  /**
   * Obtiene el estado del del hecho.
   *
   * @return estado del del hecho.
   */
  @JsonProperty("hecho_estado")
  public Estado getEstado() {
    return estado;
  }

  @JsonProperty("hecho_provincia")
  public String getProvincia() {
    return hecho_provincia;
  }

  public Usuario getAutor() {
    return this.autor;
  }

  //  @JsonProperty("hecho_fotos")
  public List<Multimedia> getFotos() {
    return new ArrayList<>(fotos);
  }

  public void quitar(int indice) {
    this.fotos.remove(indice);
  }

  public Set<Coleccion> getColecciones() {
    return this.colecciones;
  }

  public void agregarFoto(Multimedia foto) {
    this.fotos.add(foto);
  }

  public void setTitulo(String hecho_titulo) {
    this.hecho_titulo = hecho_titulo;
  }

  public void setDescripcion(String hecho_descripcion) {
    this.hecho_descripcion = hecho_descripcion;
  }

  public void setCategoria(String hecho_categoria) {
    this.hecho_categoria = hecho_categoria;
  }

  public void setDireccion(String hecho_direccion) {
    this.hecho_direccion = hecho_direccion;
  }

  public void setUbicacion(PuntoGeografico hecho_ubicacion) {
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

  public void setProvincia(String hecho_provincia) {
    this.hecho_provincia = hecho_provincia;
  }

  @JsonProperty("hecho_origen")
  public void setOrigen(Origen fuenteOrigen) {
    this.fuenteOrigen = fuenteOrigen;
  }

  public void setFotos(List<Multimedia> fotos) {
    this.fotos = new ArrayList<>(fotos);
  }

  /**
   * Verifica si el hecho es editable por un usuario específico.
   * Un hecho es editable por su creador durante una semana desde su fecha de carga.
   *
   * @return true si el hecho es editable por el usuario, false en caso contrario
   */
  public boolean esEditable(Usuario usuarioEditor) {
    return LocalDateTime.now()
                        .isBefore(hecho_fecha_carga.plusWeeks(1)) && usuarioEditor != null && this.autor.getId()
                                                                                                        .equals(
                                                                                                            usuarioEditor.getId());
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
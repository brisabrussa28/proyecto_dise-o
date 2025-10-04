package ar.edu.utn.frba.dds.domain.coleccion;

import ar.edu.utn.frba.dds.domain.coleccion.algoritmosconsenso.AlgoritmoDeConsenso;
import ar.edu.utn.frba.dds.domain.filtro.Filtro;
import ar.edu.utn.frba.dds.domain.filtro.condiciones.Condicion;
import ar.edu.utn.frba.dds.domain.filtro.condiciones.CondicionTrue;
import ar.edu.utn.frba.dds.domain.fuentes.Fuente;
import ar.edu.utn.frba.dds.domain.hecho.Hecho;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.PostLoad;
import javax.persistence.SequenceGenerator;
import javax.persistence.Transient;

/**
 * Representa una colección de hechos obtenidos de una fuente específica,
 * con la capacidad de aplicar un filtro propio y un algoritmo de consenso.
 * Su responsabilidad es manejar su propia lógica de filtrado y consenso.
 * Puede recibir un filtro externo (ej. de hechos eliminados) para aplicarlo
 * antes que su filtro interno.
 */
@Entity
public class Coleccion {

  @Id
  @SequenceGenerator(name = "coleccion_seq", sequenceName = "coleccion_sequence", allocationSize = 1)
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "coleccion_seq")
  Long coleccion_id;
  @OneToOne
  @JoinColumn(name = "fuente_id")
  private Fuente fuente;
  private String titulo;
  private String descripcion;
  private String categoria;

  @ManyToOne
  private AlgoritmoDeConsenso algoritmo;

  @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
  private Condicion condicion;

  @ManyToMany
  @JoinTable(name = "hecho_x_coleccion")
  private List<Hecho> hechosConsensuados = new ArrayList<>();

  // --- Atributos Transitorios ---
  @Transient
  private Filtro filtro;

  // --- Constructores ---
  public Coleccion() {
    // Constructor vacío para JPA
  }

  /**
   * Constructor para crear una nueva Coleccion con sus datos fundamentales.
   *
   * @param titulo      El título de la colección.
   * @param fuente      La fuente de donde se obtendrán los hechos.
   * @param descripcion Una breve descripción del propósito de la colección.
   * @param categoria   Una categoría para organizar la colección.
   */
  public Coleccion(String titulo, Fuente fuente, String descripcion, String categoria) {
    validarCamposObligatorios(titulo, fuente, descripcion, categoria);
    this.titulo = titulo;
    this.fuente = fuente;
    this.descripcion = descripcion;
    this.categoria = categoria;
    this.condicion = new CondicionTrue(); // Por defecto, no filtra nada
    inicializarFiltro();
  }

  // --- Métodos de Lógica Principal ---

  /**
   * Recalcula y actualiza la lista interna de hechos consensuados.
   *
   * @param filtroExcluyente Un filtro (ej. de hechos eliminados) que se aplica ANTES del filtro propio.
   */
  public void recalcularHechosConsensuados(Filtro filtroExcluyente) {
    List<Hecho> hechosFiltrados = this.obtenerHechosFiltrados(filtroExcluyente);
    this.hechosConsensuados = aplicarConsenso(hechosFiltrados);
  }

  /**
   * Obtiene los hechos de la fuente y aplica una cadena de filtros
   *
   * @param filtroExcluyente Un filtro que se aplica primero (ej. para excluir hechos eliminados).
   * @return Una lista de hechos completamente filtrada.
   */
  public List<Hecho> obtenerHechosFiltrados(Filtro filtroExcluyente) {
    List<Hecho> hechosFuente = fuente.obtenerHechos();
    List<Hecho> hechosSinExcluidos = filtroExcluyente.filtrar(hechosFuente);
    return this.filtro.filtrar(hechosSinExcluidos);
  }


  // --- Métodos Auxiliares y de Ayuda ---

  /**
   * Verifica si la colección está asociada a una fuente específica.
   *
   * @param unaFuente La fuente a comprobar.
   * @return {@code true} si la fuente de la colección es la misma que la proporcionada.
   */
  public boolean contieneFuente(Fuente unaFuente) {
    return this.fuente.equals(unaFuente);
  }

  /**
   * Verifica si un hecho está presente en la colección después de aplicar todos los filtros.
   *
   * @param unHecho          El hecho a verificar.
   * @param filtroExcluyente El filtro externo necesario para una comprobación precisa.
   * @return {@code true} si el hecho está en la colección, {@code false} en caso contrario.
   */
  public boolean contieneHechoFiltrado(Hecho unHecho, Filtro filtroExcluyente) {
    return this.obtenerHechosFiltrados(filtroExcluyente)
               .contains(unHecho);
  }

  /**
   * Aplica el algoritmo de consenso configurado sobre una lista de hechos.
   * Si no hay ningún algoritmo asignado, devuelve la lista de hechos original sin cambios.
   *
   * @param hechos La lista de hechos a la que se le aplicará el consenso.
   * @return Una nueva lista con los hechos que cumplen con el criterio de consenso.
   */
  private List<Hecho> aplicarConsenso(List<Hecho> hechos) {
    if (algoritmo == null) {
      return hechos;
    }
    return algoritmo.listaDeHechosConsensuados(hechos, this.fuente);
  }

  /**
   * Valida que los campos obligatorios del constructor no sean nulos o vacíos.
   *
   * @throws IllegalArgumentException si alguna validación falla.
   */
  private void validarCamposObligatorios(
      String unTitulo, Fuente unaFuente, String unaDescripcion, String unaCategoria) {
    if (unTitulo == null || unTitulo.isBlank()) {
      throw new IllegalArgumentException("El titulo es un campo obligatorio.");
    }
    if (unaFuente == null) {
      throw new IllegalArgumentException("La fuente es un campo obligatorio.");
    }
    if (unaDescripcion == null || unaDescripcion.isBlank()) {
      throw new IllegalArgumentException("La descripcion es un campo obligatorio.");
    }
    if (unaCategoria == null || unaCategoria.isBlank()) {
      throw new IllegalArgumentException("La categoria es un campo obligatorio.");
    }
  }

  // --- Métodos de JPA ---

  /**
   * Inicializa el objeto Filtro transitorio después de que la entidad es cargada por JPA.
   * Esto asegura que el filtro esté disponible para ser usado incluso en entidades recuperadas
   * de la base de datos.
   */
  @PostLoad
  private void inicializarFiltro() {
    if (this.condicion != null) {
      this.filtro = new Filtro(this.condicion);
    } else {
      this.filtro = new Filtro(new CondicionTrue());
    }
  }

  // --- Getters y Setters ---

  /**
   * Devuelve una vista no modificable de la lista de hechos consensuados.
   * Para actualizar esta lista, se debe llamar a recalcularHechosConsensuados().
   *
   * @return Una lista de solo lectura de los hechos consensuados.
   */
  public List<Hecho> getHechosConsensuados() {
    return Collections.unmodifiableList(hechosConsensuados);
  }

  public String getTitulo() {
    return titulo;
  }

  public String getDescripcion() {
    return descripcion;
  }

  public String getCategoria() {
    return categoria;
  }

  public Filtro getFiltro() {
    return filtro;
  }

  public void setCondicion(Condicion condicion) {
    this.condicion = condicion;
    this.filtro.setCondicion(condicion);
  }

  public void setAlgoritmoDeConsenso(AlgoritmoDeConsenso algoritmo) {
    this.algoritmo = algoritmo;
  }
}
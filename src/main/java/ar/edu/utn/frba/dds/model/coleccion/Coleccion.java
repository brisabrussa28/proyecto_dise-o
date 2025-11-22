package ar.edu.utn.frba.dds.model.coleccion;

import ar.edu.utn.frba.dds.model.coleccion.algoritmosconsenso.AlgoritmoDeConsenso;
import ar.edu.utn.frba.dds.model.filtro.Filtro;
import ar.edu.utn.frba.dds.model.filtro.condiciones.Condicion;
import ar.edu.utn.frba.dds.model.filtro.condiciones.CondicionTrue;
import ar.edu.utn.frba.dds.model.fuentes.Fuente;
import ar.edu.utn.frba.dds.model.hecho.Hecho;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.PostLoad;
import javax.persistence.Table;
import javax.persistence.Transient;

/**
 * Representa una colección de hechos obtenidos de una fuente específica,
 * con la capacidad de aplicar un filtro propio y un algoritmo de consenso.
 * Su responsabilidad es manejar su propia lógica de filtrado y consenso.
 * Puede recibir un filtro externo (ej. de hechos eliminados) para aplicarlo
 * antes que su filtro interno.
 */
@Entity
@Table(name = "Coleccion")
public class Coleccion {
  //TODO: Poner nombres de var como la gente, no como si fueran de bd. de ultima le asignamos un nombre especal en la bd
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  Long coleccion_id;
  @ManyToOne
  @JoinColumn(name = "coleccion_fuente")
  private Fuente coleccion_fuente;

  private String coleccion_titulo;
  private String coleccion_descripcion;
  private String coleccion_categoria;

  @ManyToOne
  @JoinColumn(name = "coleccion_algoritmo")
  private AlgoritmoDeConsenso coleccion_algoritmo;

  @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
  private Condicion coleccion_condicion;

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
   * @param coleccion_titulo      El título de la colección.
   * @param coleccion_fuente      La fuente de donde se obtendrán los hechos.
   * @param coleccion_descripcion Una breve descripción del propósito de la colección.
   * @param coleccion_categoria   Una categoría para organizar la colección.
   */
  public Coleccion(
      String coleccion_titulo,
      Fuente coleccion_fuente,
      String coleccion_descripcion,
      String coleccion_categoria,
      AlgoritmoDeConsenso coleccion_algoritmo
  ) {
    validarCamposObligatorios(
        coleccion_titulo, coleccion_fuente, coleccion_descripcion,
        coleccion_categoria
    );
    this.coleccion_algoritmo = coleccion_algoritmo;
    this.coleccion_titulo = coleccion_titulo;
    this.coleccion_fuente = coleccion_fuente;
    this.coleccion_descripcion = coleccion_descripcion;
    this.coleccion_categoria = coleccion_categoria;
    this.coleccion_condicion = new CondicionTrue(); // Por defecto, no filtra nada
    inicializarFiltro();
  }

  // --- Métodos de Lógica Principal ---

  /**
   * Recalcula y actualiza la lista interna de hechos consensuados.
   *
   * @param filtroExcluyente Un filtro (ej. de hechos eliminados) que se aplica ANTES del filtro propio.
   */
  public void recalcularHechosConsensuados(Filtro filtroExcluyente, List<Fuente> fuentes) {
    List<Hecho> hechosFiltrados = this.obtenerHechosFiltrados(filtroExcluyente);
    this.coleccion_algoritmo.recalcularHechosConsensuados(hechosFiltrados, fuentes);
  }

  /**
   * Obtiene los hechos de la fuente y aplica una cadena de filtros
   *
   * @param filtroExcluyente Un filtro que se aplica primero (ej. para excluir hechos eliminados).
   * @return Una lista de hechos completamente filtrada.
   */
  public List<Hecho> obtenerHechosFiltrados(Filtro filtroExcluyente) {
    List<Hecho> hechosFuente = coleccion_fuente.getHechos();
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
    return this.coleccion_fuente.equals(unaFuente);
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
    if (this.coleccion_condicion != null) {
      this.filtro = new Filtro(this.coleccion_condicion);
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
    return coleccion_algoritmo.getHechosConsensuados();
  }

  public String getTitulo() {
    return coleccion_titulo;
  }

  public String getDescripcion() {
    return coleccion_descripcion;
  }

  public String getCategoria() {
    return coleccion_categoria;
  }

  public Filtro getFiltro() {
    return filtro;
  }

  public Long getId() {
    return this.coleccion_id;
  }

  public void setCondicion(Condicion coleccion_condicion) {
    this.coleccion_condicion = coleccion_condicion;
    this.filtro.setCondicion(coleccion_condicion);
  }

  public void setId(Long id) {
    this.coleccion_id = id;
  }

  public void setAlgoritmoDeConsenso(AlgoritmoDeConsenso algoritmo) {
    this.coleccion_algoritmo = algoritmo;
  }
}
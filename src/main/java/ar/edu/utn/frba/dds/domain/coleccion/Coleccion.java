package ar.edu.utn.frba.dds.domain.coleccion;

import ar.edu.utn.frba.dds.domain.coleccion.algoritmosconsenso.AlgoritmoDeConsenso;
import ar.edu.utn.frba.dds.domain.filtro.Filtro;
import ar.edu.utn.frba.dds.domain.filtro.condiciones.Condicion;
import ar.edu.utn.frba.dds.domain.filtro.condiciones.CondicionTrue;
import ar.edu.utn.frba.dds.domain.fuentes.Fuente;
import ar.edu.utn.frba.dds.domain.fuentes.FuenteDeAgregacion;
import ar.edu.utn.frba.dds.domain.hecho.Hecho;
import ar.edu.utn.frba.dds.domain.reportes.RepositorioDeSolicitudes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.PostLoad;
import javax.persistence.Transient;

/**
 * Clase Coleccion.
 * Representa una colección de hechos obtenidos de una fuente específica,
 * con la capacidad de aplicar filtros y algoritmos de consenso.
 */
@Entity
public class Coleccion {
  @Id
  @GeneratedValue
  Long id;
  @OneToOne
  private Fuente fuente;
  private String titulo;
  private String descripcion;
  private String categoria;
  @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
  private Condicion condicion;
  @ManyToOne
  private AlgoritmoDeConsenso algoritmo;
  @ManyToMany
  private List<Hecho> hechosConsensuados = new ArrayList<>();
  @Transient
  private  Filtro filtro;

  /**
   * Constructor de la colección.
   *
   * @param titulo      Título de la colección. No puede ser nulo o vacío.
   * @param fuente      Fuente de datos de la colección. No puede ser nula.
   * @param descripcion Descripción de la colección. No puede ser nula o vacía.
   * @param categoria   Categoría de la colección. No puede ser nula o vacía.
   * @throws RuntimeException si alguno de los parámetros obligatorios es inválido.
   */
  @SuppressWarnings("checkstyle:ParenPad")
  public Coleccion(
      String titulo,
      Fuente fuente,
      String descripcion,
      String categoria
  ) {
    if (titulo == null || titulo.isBlank()) {
      throw new RuntimeException("El titulo es campo obligatorio.");
    }
    if (fuente == null) {
      throw new RuntimeException("La fuente es campo obligatorio.");
    }
    if (descripcion == null || descripcion.isBlank()) {
      throw new RuntimeException("La descripcion es campo obligatorio.");
    }
    if (categoria == null || categoria.isBlank()) {
      throw new RuntimeException("La categoria es campo obligatorio.");
    }
    this.titulo = titulo;
    this.fuente = fuente;
    this.descripcion = descripcion;
    this.categoria = categoria;
    this.condicion = new CondicionTrue();
    this.filtro = new Filtro(this.condicion);
  }

  public Coleccion() {

  }

  @PostLoad
  private void inicializarFiltro() {
      if (this.condicion != null) {
          this.filtro = new Filtro(this.condicion);
      }
  }

  /**
   * Obtiene el título de la colección.
   *
   * @return El título.
   */
  public String getTitulo() {
    return titulo;
  }

  /**
   * Obtiene la descripción de la colección.
   *
   * @return La descripción.
   */
  public String getDescripcion() {
    return descripcion;
  }

  /**
   * Obtiene la categoría de la colección.
   *
   * @return La categoría.
   */
  public String getCategoria() {
    return categoria;
  }

  /**
   * Construye y devuelve el filtro persistente basado en la condición JSON almacenada.
   *
   * @return Un objeto {@link Filtro} listo para ser usado.
   */
  public Filtro getFiltro() {
    return filtro;
  }

  /**
   * Establece la condición de filtrado para la colección.
   * La condición se convierte a formato JSON para su almacenamiento.
   *
   * @param condicion El objeto {@link Condicion} que define el filtro.
   */
  public void setCondicion(Condicion condicion) {
    this.condicion = condicion;
    this.filtro.setCondicion(condicion);
  }

  /**
   * Obtiene los hechos de la colección después de aplicar los filtros correspondientes.
   *
   * @param repo El repositorio de solicitudes que contiene el filtro de exclusión.
   * @return Una lista de hechos filtrada y lista para su visualización o uso.
   */
  public List<Hecho> getHechos(RepositorioDeSolicitudes repo) {
    // Obtiene los hechos de la fuente
    List<Hecho> hechosFuente = fuente.obtenerHechos();

    // Aplica el filtro de exclusión del repositorio
    List<Hecho> hechosSinExcluidos = repo.filtroExcluyente().filtrar(hechosFuente);

    // Aplica el filtro propio de la colección
    return this.getFiltro().filtrar(hechosSinExcluidos);
  }

  /**
   * Recalcula la lista de hechos consensuados aplicando el algoritmo de consenso configurado.
   * Si no hay algoritmo, los hechos consensuados serán los mismos que los hechos filtrados.
   *
   * @param repo El repositorio de solicitudes necesario para obtener los hechos base.
   */
  public void recalcularHechosConsensuados(RepositorioDeSolicitudes repo) {
    List<Hecho> hechosBase = getHechos(repo);
    this.hechosConsensuados = aplicarConsenso(hechosBase);
  }

  /**
   * Aplica el algoritmo de consenso de la colección, si es que tiene uno.
   *
   * @param hechos Lista de hechos base de la colección.
   */
  private List<Hecho> aplicarConsenso(List<Hecho> hechos) {
    return (algoritmo == null)
        ? hechos
        : algoritmo.listaDeHechosConsensuados(hechos, obtenerFuentesDelNodo());
  }

  /**
   * Devuelve la lista de hechos que han pasado el algoritmo de consenso.
   *
   * @return Una copia de la lista de hechos consensuados.
   */
  public List<Hecho> getHechosConsensuados() {
    return Collections.unmodifiableList(hechosConsensuados);
  }

  /**
   * Valida si la colección contiene una fuente específica.
   *
   * @param unaFuente La fuente a verificar.
   * @return {@code true} si la colección contiene la fuente, {@code false} en caso contrario.
   */
  public boolean contieneFuente(Fuente unaFuente) {
    return fuente == unaFuente;
  }

  /**
   * Verifica si un hecho está presente en la colección después de aplicar todos los filtros.
   *
   * @param unHecho               El hecho a verificar.
   * @param repositorioDeReportes El repositorio necesario para obtener la lista de hechos filtrados
   * @return {@code true} si el hecho está en la colección, {@code false} en caso contrario.
   */
  public boolean contieneA(Hecho unHecho, RepositorioDeSolicitudes repositorioDeReportes) {
    return this.getHechos(repositorioDeReportes)
               .contains(unHecho);
  }

  /**
   * Establece el algoritmo de consenso para la colección.
   *
   * @param algoritmo El algoritmo de consenso a utilizar.
   */
  public void setAlgoritmoDeCoscenso(AlgoritmoDeConsenso algoritmo) {
    this.algoritmo = algoritmo;
  }

  /**
   * Mét0do auxiliar para obtener la lista de fuentes subyacentes.
   * Si la fuente principal es un agregador, devuelve las fuentes que lo componen.
   * Si es una fuente simple, la devuelve en una lista unitaria.
   *
   * @return La lista de fuentes base.
   */
  private List<Fuente> obtenerFuentesDelNodo() {
    if (this.fuente instanceof FuenteDeAgregacion agregador) {
      return agregador.getFuentesCargadas();
    } else {
      return List.of(this.fuente);
    }
  }
}
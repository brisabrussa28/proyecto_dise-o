package ar.edu.utn.frba.dds.domain.coleccion;

import ar.edu.utn.frba.dds.domain.algoritmosconsenso.AlgoritmoDeConsenso;
import ar.edu.utn.frba.dds.domain.filtro.Filtro;
import ar.edu.utn.frba.dds.domain.filtro.FiltroListaAnd;
import ar.edu.utn.frba.dds.domain.filtro.FiltroPredicado;
import ar.edu.utn.frba.dds.domain.fuentes.Fuente;
import ar.edu.utn.frba.dds.domain.fuentes.FuenteDeAgregacion;
import ar.edu.utn.frba.dds.domain.hecho.Hecho;
import ar.edu.utn.frba.dds.domain.reportes.RepositorioDeSolicitudes;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Transient;

/**
 * Clase Coleccion.
 * Representa una colección de hechos obtenidos de una fuente específica.
 */
@Entity
public class Coleccion {
  @Id
  @GeneratedValue
  Long id;
  @Transient
  private final Fuente fuente;
  private final String titulo;
  private final String descripcion;
  private final String categoria;
  @Transient
  private Filtro filtro;
  @Transient
  private AlgoritmoDeConsenso algoritmo;
  @Transient
  private List<Hecho> hechosConsensuados = new ArrayList<>();

  /**
   * Constructor de la colección.
   *
   * @param titulo      Título de la colección.
   * @param fuente      Fuente de la colección.
   * @param descripcion Descripción de la colección.
   * @param categoria   Categoría de la colección.
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
    this.filtro = new FiltroPredicado(h -> true);
  }

  public Coleccion(
      String titulo,
      Fuente fuente,
      String descripcion,
      String categoria,
      AlgoritmoDeConsenso algoritmo
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
    this.filtro = new FiltroPredicado(h -> true);
    this.algoritmo = algoritmo;
  }

  /**
   * Obtiene la fuente de la colección.
   *
   * @return Fuente de la colección.
   */
  public String getTitulo() {
    return titulo;
  }

  /**
   * Obtiene la fuente de la colección.
   *
   * @return Fuente de la colección.
   */
  public String getDescripcion() {
    return descripcion;
  }

  /**
   * Obtiene la fuente de la colección.
   *
   * @return Fuente de la colección.
   */
  public String getCategoria() {
    return categoria;
  }

  /**
   * Obtiene la fuente de la colección.
   *
   * @return Fuente de la colección.
   */
  public Filtro getFiltro() {
    return filtro;
  }

  /**
   * Establece el filtro de la colección.
   *
   * @param filtro Filtro
   */

  public void setFiltro(Filtro filtro) {
    this.filtro = filtro != null ? filtro : new FiltroPredicado(h -> true);
    ;
  }

  /**
   * Obtiene los hechos de la colección filtrados por un criterio y un filtro externo opcional.
   *
   * @param repo RepositorioDeReportes
   * @return Lista de hechos filtrados.
   */
  public List<Hecho> getHechos(RepositorioDeSolicitudes repo) {
    return repo.filtroExcluyente()
               .filtrar(fuente.obtenerHechos());
  }

  public void recalcularHechosConsensuados(RepositorioDeSolicitudes repo) {
    List<Fuente> fuentesNodo = this.obtenerFuentesDelNodo();
    List<Hecho> hechos = getHechos(repo);

    if (algoritmo == null) {
      this.hechosConsensuados = hechos;
    } else {
      this.hechosConsensuados = algoritmo.listaDeHechosConsensuados(hechos, fuentesNodo);
    }
  }

  public List<Hecho> getHechosConsensuados() {
    return new ArrayList<>(hechosConsensuados);
  }

  /**
   * filtra los hechos de la colección aplicando el filtro propio y un filtro excluyente.
   *
   * @param filtroExcluyente Filtro
   * @return un nuevo filtro que combina el filtro de la colección con el filtro excluyente.
   */

  private Filtro filtrarHechos(Filtro filtroExcluyente) {
    if (filtroExcluyente == null) {
      return filtro;
    }
    return new FiltroListaAnd(List.of(filtro, filtroExcluyente));
  }

  /**
   * Valida si la colección contiene una fuente específica.
   *
   * @param unaFuente Fuente
   * @return true si la colección contiene la fuente, false en caso contrario.
   */
  public boolean contieneFuente(Fuente unaFuente) {
    return fuente == unaFuente;
  }

  /**
   * Verifica si un hecho está presente en la colección.
   *
   * @param unHecho               Hecho a verificar.
   * @param repositorioDeReportes Repositorio de reportes para obtener los hechos.
   * @return true si el hecho está en la colección, false en caso contrario.
   */

  public boolean contieneA(Hecho unHecho, RepositorioDeSolicitudes repositorioDeReportes) {
    return this.getHechos(repositorioDeReportes)
               .contains(unHecho);
  }


  public void setAlgoritmoDeConcenso(AlgoritmoDeConsenso algoritmo) {
    this.algoritmo = algoritmo;
  }

  private List<Fuente> obtenerFuentesDelNodo() {
    if (this.fuente instanceof FuenteDeAgregacion agregador) {
      return agregador.getFuentesCargadas();
    } else {
      return List.of(this.fuente);
    }
  }
}

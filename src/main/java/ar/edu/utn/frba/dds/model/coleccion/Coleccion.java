package ar.edu.utn.frba.dds.model.coleccion;

import ar.edu.utn.frba.dds.model.coleccion.algoritmosconsenso.Absoluta;
import ar.edu.utn.frba.dds.model.coleccion.algoritmosconsenso.AlgoritmoDeConsenso;
import ar.edu.utn.frba.dds.model.coleccion.algoritmosconsenso.MayoriaSimple;
import ar.edu.utn.frba.dds.model.coleccion.algoritmosconsenso.MultiplesMenciones;
import ar.edu.utn.frba.dds.model.filtro.Filtro;
import ar.edu.utn.frba.dds.model.filtro.condiciones.Condicion;
import ar.edu.utn.frba.dds.model.filtro.condiciones.CondicionTrue;
import ar.edu.utn.frba.dds.model.fuentes.Fuente;
import ar.edu.utn.frba.dds.model.hecho.Hecho;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
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
import javax.persistence.Table;
import javax.persistence.Transient;

/**
 * Representa una colección de hechos obtenidos de una fuente específica.
 */
@Entity
@Table(name = "Coleccion")
public class Coleccion {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  Long coleccion_id;

  @ManyToOne
  @JoinColumn(name = "coleccion_fuente")
  private Fuente coleccion_fuente;

  private String coleccion_titulo;
  private String coleccion_descripcion;
  private String coleccion_categoria;

  @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
  @JoinColumn(name = "algoritmo_id")
  private AlgoritmoDeConsenso coleccion_algoritmo;

  @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
  private Condicion coleccion_condicion;

  @ManyToMany
  @JoinTable(
      name = "coleccion_hecho",
      joinColumns = @JoinColumn(name = "coleccion_coleccion_id"),
      inverseJoinColumns = @JoinColumn(name = "hechos_hecho_id")
  )
  private Set<Hecho> hechos = new HashSet<>();

  // --- Atributos Transitorios ---
  @Transient
  private Filtro filtro;

  // --- Constructores ---
  public Coleccion() {
  }

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
    this.coleccion_condicion = new CondicionTrue();
    inicializarFiltro();
  }

  // --- Métodos de Lógica Principal ---

  public void recalcularHechosConsensuados(Filtro filtroExcluyente, List<Fuente> fuentes) {
    List<Hecho> hechosParaAnalizar = this.obtenerHechosFiltrados(filtroExcluyente);
    List<Hecho> aprobados = this.coleccion_algoritmo.listaDeHechosConsensuados(
        hechosParaAnalizar,
        fuentes
    );
    this.hechos.clear();
    this.hechos.addAll(aprobados);
  }

  public List<Hecho> obtenerHechosFiltrados(Filtro filtroExcluyente) {
    List<Hecho> hechosFuente = coleccion_fuente.getHechos();
    List<Hecho> hechosSinExcluidos = filtroExcluyente.filtrar(hechosFuente);
    return this.filtro.filtrar(hechosSinExcluidos);
  }

  public boolean contieneFuente(Fuente unaFuente) {
    return this.coleccion_fuente.equals(unaFuente);
  }

  public boolean contieneHechoFiltrado(Hecho unHecho, Filtro filtroExcluyente) {
    return this.obtenerHechosFiltrados(filtroExcluyente)
               .contains(unHecho);
  }

  private void validarCamposObligatorios(
      String unTitulo, Fuente unaFuente, String unaDescripcion, String unaCategoria) {
    if (unTitulo == null || unTitulo.isBlank()) throw new IllegalArgumentException("El titulo es obligatorio.");
    if (unaFuente == null) throw new IllegalArgumentException("La fuente es obligatoria.");
    if (unaDescripcion == null || unaDescripcion.isBlank()) throw new IllegalArgumentException("La descripcion es obligatoria.");
    if (unaCategoria == null || unaCategoria.isBlank()) throw new IllegalArgumentException("La categoria es obligatoria.");
  }

  @PostLoad
  private void inicializarFiltro() {
    if (this.coleccion_condicion != null) {
      this.filtro = new Filtro(this.coleccion_condicion);
    } else {
      this.filtro = new Filtro(new CondicionTrue());
    }
  }

  public List<Hecho> getHechosConsensuados() {
    return new ArrayList<>(this.hechos);
  }

  public void recalcularConsenso() {
    if (this.coleccion_fuente == null || this.coleccion_algoritmo == null) {
      return;
    }
    List<Hecho> hechosCrudos = this.coleccion_fuente.getHechos();
    List<Hecho> hechosFiltrados = this.coleccion_algoritmo.listaDeHechosConsensuados(
        hechosCrudos,
        List.of(coleccion_fuente)
    );
    this.hechos.clear();
    this.hechos.addAll(hechosFiltrados);
  }

  // --- Método Helper para la Vista (Handlebars) ---
  public String getAlgoritmoTipo() {
    if (this.coleccion_algoritmo == null) return "";

    if (this.coleccion_algoritmo instanceof Absoluta) return "Absoluta";
    if (this.coleccion_algoritmo instanceof MayoriaSimple) return "May_simple";
    if (this.coleccion_algoritmo instanceof MultiplesMenciones) return "Mult_menciones";

    return "";
  }

  // --- Getters y Setters Básicos ---
  public String getTitulo() { return coleccion_titulo; }
  public String getDescripcion() { return coleccion_descripcion; }
  public String getCategoria() { return coleccion_categoria; }
  public Filtro getFiltro() { return filtro; }
  public Fuente getFuente() { return this.coleccion_fuente; }
  public AlgoritmoDeConsenso getAlgoritmo() { return this.coleccion_algoritmo; }
  public Long getId() { return this.coleccion_id; }
  public Set<Hecho> getHechos() { return this.hechos; }

  public void setCondicion(Condicion coleccion_condicion) {
    this.coleccion_condicion = coleccion_condicion;
    this.filtro.setCondicion(coleccion_condicion);
  }
  public void setId(Long id) { this.coleccion_id = id; }
  public void setTitulo(String titulo) { this.coleccion_titulo = titulo; }
  public void setCategoria(String categoria) { this.coleccion_categoria = categoria; }
  public void setDescripcion(String descripcion) { this.coleccion_descripcion = descripcion; }
  public void setFuente(Fuente fuente) { this.coleccion_fuente = fuente; }
  public void setAlgoritmoDeConsenso(AlgoritmoDeConsenso algoritmo) { this.coleccion_algoritmo = algoritmo; }

  // --- Identidad ---
  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Coleccion coleccion = (Coleccion) o;
    return Objects.equals(coleccion_id, coleccion.coleccion_id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(coleccion_id);
  }
}
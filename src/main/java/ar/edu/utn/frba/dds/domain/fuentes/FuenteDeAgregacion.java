package ar.edu.utn.frba.dds.domain.fuentes;

import ar.edu.utn.frba.dds.domain.exportador.configuracion.ConfiguracionExportador;
import ar.edu.utn.frba.dds.domain.hecho.Hecho;
import ar.edu.utn.frba.dds.domain.lector.configuracion.ConfiguracionLector;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToMany;

/**
 * Fuente que combina los hechos de múltiples otras fuentes.
 * Hereda de FuenteDeCopiaLocal para cachear en memoria el resultado agregado
 * y persistir su configuración de lector/exportador de forma nativa con JPA.
 */
@Entity
@DiscriminatorValue("AGREGACION")
public class FuenteDeAgregacion extends FuenteDeCopiaLocal {

  /**
   * Relación con las fuentes que se van a agregar.
   * EAGER se usa para que las fuentes se carguen inmediatamente con esta entidad.
   */
  @ManyToMany(fetch = FetchType.LAZY)
  private List<Fuente> fuentesCargadas = new ArrayList<>();

  /**
   * Constructor vacío requerido por JPA.
   */
  protected FuenteDeAgregacion() {
    super();
  }

  /**
   * Constructor principal actualizado para recibir los objetos de configuración.
   *
   * @param nombre           Nombre de la fuente.
   * @param rutaCopia        Ruta del archivo para la copia local (caché).
   * @param configLector     La entidad de configuración para el lector.
   * @param configExportador La entidad de configuración para el exportador.
   */
  public FuenteDeAgregacion(
      String nombre,
      String rutaCopia,
      ConfiguracionLector configLector,
      ConfiguracionExportador configExportador
  ) {
    super(nombre, rutaCopia, configLector, configExportador);
  }

  /**
   * Agrega una fuente a la lista de fuentes que serán agregadas.
   *
   * @param fuente La fuente a agregar.
   */
  public void agregarFuente(Fuente fuente) {
    if (this.fuentesCargadas == null) {
      this.fuentesCargadas = new ArrayList<>();
    }
    this.fuentesCargadas.add(fuente);
  }

  /**
   * Obtiene una copia de la lista de fuentes cargadas.
   *
   * @return Una nueva lista conteniendo las fuentes.
   */
  public List<Fuente> getFuentesCargadas() {
    return new ArrayList<>(this.fuentesCargadas);
  }

  /**
   * Consulta los hechos de todas las fuentes cargadas, los combina
   * en una sola lista y elimina duplicados.
   *
   * @return La lista consolidada de hechos.
   */
  @Override
  protected List<Hecho> consultarNuevosHechos() {
    if (this.fuentesCargadas == null || this.fuentesCargadas.isEmpty()) {
      return new ArrayList<>();
    }
    return this.fuentesCargadas.stream()
        .flatMap(fuente -> fuente.obtenerHechos().stream())
        .distinct()
        .collect(Collectors.toList());
  }
}
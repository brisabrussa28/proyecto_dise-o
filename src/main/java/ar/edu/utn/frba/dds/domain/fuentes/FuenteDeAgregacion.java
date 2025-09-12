package ar.edu.utn.frba.dds.domain.fuentes;

import ar.edu.utn.frba.dds.domain.hecho.Hecho;
import ar.edu.utn.frba.dds.domain.serializadores.Lector.Lector;
import ar.edu.utn.frba.dds.domain.serializadores.exportador.Exportador;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Fuente que combina los hechos de múltiples otras fuentes en una sola lista.
 * También hereda de FuenteDeCopiaLocal para cachear el resultado agregado.
 */
public class FuenteDeAgregacion extends FuenteDeCopiaLocal {

  private final List<Fuente> fuentesCargadas;

  /**
   * Constructor de la fuente de agregación.
   *
   * @param nombre       Nombre de la fuente.
   * @param rutaCopia    Ruta del archivo para la copia local (caché).
   * @param lector Serializador para manejar la persistencia de la caché.
   * @param exportador Exportador para guardar la caché.
   */
  public FuenteDeAgregacion(String nombre, String rutaCopia, Lector<Hecho> lector, Exportador<Hecho> exportador) {
    super(nombre, rutaCopia, lector, exportador);
    this.fuentesCargadas = new ArrayList<>();
  }

  /**
   * Agrega una fuente a la lista de fuentes que serán agregadas.
   *
   * @param fuente La fuente a agregar.
   */
  public void agregarFuente(Fuente fuente) {
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
    return this.fuentesCargadas.stream()
                               .flatMap(fuente -> fuente.obtenerHechos()
                                                        .stream())
                               .distinct()
                               .collect(Collectors.toList());
  }
}
package ar.edu.utn.frba.dds.model.lector.csv.filaconverter.model.coleccion.algoritmosconsenso;

import ar.edu.utn.frba.dds.model.fuentes.Fuente;
import ar.edu.utn.frba.dds.model.fuentes.FuenteDeAgregacion;
import ar.edu.utn.frba.dds.model.hecho.Estado;
import ar.edu.utn.frba.dds.model.hecho.Hecho;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Table;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "algoritmo_tipo")
@Table(name = "AlgoritmoDeConsenso")
public abstract class AlgoritmoDeConsenso {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  Long algoritmo_id;

  public List<Hecho> listaDeHechosConsensuados(
      List<Hecho> listaDeHechos,
      List<Fuente> fuentes
  ) {

    List<Set<Hecho>> hechosPorFuenteEnSets = obtenerListasDeHechos(fuentes);

    return listaDeHechos.stream()
                        .distinct()
                        .filter(hecho -> esConsensuado(hecho, hechosPorFuenteEnSets))
                        .toList();
  }

  /**
   * Método auxiliar para obtener la lista de fuentes subyacentes.
   */
  List<Fuente> obtenerFuentesDelNodo(Fuente fuente) {
    if (fuente instanceof FuenteDeAgregacion agregador) {
      return agregador.getFuentesCargadas();
    } else {
      return List.of(fuente);
    }
  }

  /**
   * Obtiene la lista de hechos de cada fuente, excluyendo los eliminados.
   */
  List<Set<Hecho>> obtenerListasDeHechos(List<Fuente> fuentes) {
    return fuentes.stream()
                  .map(fuente ->
                           fuente.getHechos()
                                 .stream()
                                 .filter(hecho -> hecho.getEstado() != Estado.ELIMINADO)
                                 .collect(Collectors.toSet())
                  )
                  .toList();
  }

  protected boolean existeHechoSimilarEnFuente(Set<Hecho> hechosDeLaFuente, Hecho hechoBuscado) {
    return hechosDeLaFuente.stream().anyMatch(h -> sonHechosEquivalentes(h, hechoBuscado));
  }

  /**
   * Determina si dos hechos son equivalentes comparando múltiples campos clave.
   * Reglas:
   * 1. Identidad exacta (equals).
   * 2. O coincidencia aproximada en: Título + (Dirección O Ubicación) + Fecha.
   */
  private boolean sonHechosEquivalentes(Hecho h1, Hecho h2) {
    if (h1.equals(h2)) return true;

    boolean tituloSimilar = h1.getTitulo() != null && h2.getTitulo() != null
        && h1.getTitulo().trim().equalsIgnoreCase(h2.getTitulo().trim());

    if (!tituloSimilar) return false;

    boolean fechaSimilar = (h1.getFechaSuceso() == null && h2.getFechaSuceso() == null) ||
        (h1.getFechaSuceso() != null && h2.getFechaSuceso() != null &&
            h1.getFechaSuceso().equals(h2.getFechaSuceso()));

    if (!fechaSimilar) return false;

    boolean ubicacionSimilar = false;

    if (h1.getUbicacion() != null && h2.getUbicacion() != null) {
      double epsilon = 0.0001;
      boolean latIgual = Math.abs(h1.getUbicacion().getLatitud() - h2.getUbicacion().getLatitud()) < epsilon;
      boolean lngIgual = Math.abs(h1.getUbicacion().getLongitud() - h2.getUbicacion().getLongitud()) < epsilon;
      ubicacionSimilar = latIgual && lngIgual;
    } else {
      ubicacionSimilar = h1.getDireccion() != null && h2.getDireccion() != null
          && h1.getDireccion().trim().equalsIgnoreCase(h2.getDireccion().trim());
    }

    return ubicacionSimilar;
  }

  protected abstract boolean esConsensuado(Hecho hecho, List<Set<Hecho>> hechosDeFuentes);
}
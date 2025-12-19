package ar.edu.utn.frba.dds.model.coleccion.algoritmosconsenso;

import ar.edu.utn.frba.dds.model.fuentes.Fuente;
import ar.edu.utn.frba.dds.model.hecho.Estado;
import ar.edu.utn.frba.dds.model.hecho.Hecho;
import ar.edu.utn.frba.dds.repositories.FuenteRepository;
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

  public List<Hecho> listaDeHechosConsensuados(List<Hecho> listaDeHechos) {

    // 1. Traemos todas las fuentes
    List<Fuente> todasLasFuentes = FuenteRepository.instance().findAllFuentesConHechos();

    if (todasLasFuentes == null || todasLasFuentes.isEmpty()) {
      return List.of();
    }

    List<Fuente> fuentesActivas = todasLasFuentes.stream()
                                                 .filter(f -> f.getHechos() != null && !f.getHechos().isEmpty())
                                                 .collect(Collectors.toList());

    if (fuentesActivas.isEmpty()) {
      return List.of();
    }

    // 3. Mapeamos a Sets de hechos para búsqueda rápida
    List<Set<Hecho>> hechosPorFuente = fuentesActivas.stream()
                                                     .map(fuente -> fuente.getHechos().stream()
                                                                          .filter(h -> h.getEstado() != Estado.ELIMINADO)
                                                                          .collect(Collectors.toSet()))
                                                     .collect(Collectors.toList());

    return listaDeHechos.stream()
                        .distinct()
                        .filter(hecho -> esConsensuado(hecho, hechosPorFuente))
                        .collect(Collectors.toList());
  }

  protected boolean existeHechoSimilarEnFuente(Set<Hecho> hechosDeLaFuente, Hecho hechoBuscado) {
    return hechosDeLaFuente.stream().anyMatch(h -> sonHechosEquivalentes(h, hechoBuscado));
  }

  // --- LÓGICA DE COMPARACIÓN ROBUSTA ---
  protected boolean sonHechosEquivalentes(Hecho h1, Hecho h2) {
    if (h1 == h2) return true;
    if (h1.getId() != null && h2.getId() != null && h1.getId().equals(h2.getId())) return true;
    if (h1.equals(h2)) return true;

    // 1. Validar Título (Normalizado)
    String t1 = normalizarTexto(h1.getTitulo());
    String t2 = normalizarTexto(h2.getTitulo());

    // Si títulos difieren, no hay nada que hacer.
    if (t1.isEmpty() || t2.isEmpty() || !t1.equals(t2)) return false;

    // 2. Validar Fecha (Solo la parte de fecha YYYY-MM-DD, ignorando horas/minutos si fuera timestamp)
    boolean fechaSimilar = false;
    if (h1.getFechaSuceso() == null && h2.getFechaSuceso() == null) {
      fechaSimilar = true;
    } else if (h1.getFechaSuceso() != null && h2.getFechaSuceso() != null) {
      // Usamos toString() que suele formatear a YYYY-MM-DD en LocalDate/Date SQL
      // Esto evita problemas de milisegundos o zonas horarias
      String f1 = h1.getFechaSuceso().toString().split(" ")[0]; // Quedarse solo con fecha si hay hora
      String f2 = h2.getFechaSuceso().toString().split(" ")[0];
      fechaSimilar = f1.equals(f2);
    }

    if (!fechaSimilar) return false;

    // 3. Validar Ubicación (LÓGICA PERMISIVA)
    // Si coinciden en Título y Fecha, asumimos que es el mismo hecho,
    // SALVO que las ubicaciones sean explícitamente contradictorias.

    boolean h1TieneUbicacion = h1.getUbicacion() != null;
    boolean h2TieneUbicacion = h2.getUbicacion() != null;
    boolean h1TieneDireccion = h1.getDireccion() != null;
    boolean h2TieneDireccion = h2.getDireccion() != null;

    // Caso A: Ambos tienen coordenadas -> Deben coincidir.
    if (h1TieneUbicacion && h2TieneUbicacion) {
      double epsilon = 0.0001; // ~11 metros
      boolean latIgual = Math.abs(h1.getUbicacion().getLatitud() - h2.getUbicacion().getLatitud()) < epsilon;
      boolean lngIgual = Math.abs(h1.getUbicacion().getLongitud() - h2.getUbicacion().getLongitud()) < epsilon;
      if (!latIgual || !lngIgual) return false; // Contradicción explícita
    }

    // Caso B: Ambos tienen dirección -> Deben coincidir.
    if (h1TieneDireccion && h2TieneDireccion) {
      String d1 = normalizarTexto(h1.getDireccion());
      String d2 = normalizarTexto(h2.getDireccion());
      if (!d1.isEmpty() && !d1.equals(d2)) return false; // Contradicción explícita
    }


    return true;
  }

  protected String normalizarTexto(String input) {
    if (input == null) return "";
    return input.trim().replaceAll("\\s+", " ").toLowerCase();
  }

  protected abstract boolean esConsensuado(Hecho hecho, List<Set<Hecho>> hechosDeFuentes);
}
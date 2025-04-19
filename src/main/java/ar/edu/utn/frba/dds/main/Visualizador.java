package ar.edu.utn.frba.dds.main;

import ar.edu.utn.frba.dds.domain.Fuente;
import ar.edu.utn.frba.dds.domain.Hecho;
import java.util.List;

public class Visualizador {

  public List<Hecho> visualizarHechos(Fuente fuente) {
    return fuente.obtenerHechos();
  }

  public List<Hecho> filtrarPorEtiqueta(List<Hecho> hechos, String etiqueta) {
    return hechos.stream()
        .filter(h -> h.getEtiquetas().contains(etiqueta))
        .toList();
  }

  public List<Hecho> filtrarPorCategoria(List<Hecho> hechos, String categoria) {
    return hechos.stream()
        .filter(h -> h.getCategoria().equalsIgnoreCase(categoria))
        .toList();
  }

  // No requerirá identificarse, y podrá subir hechos si así lo quisiera manteniendo su anonimato
}

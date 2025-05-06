package ar.edu.utn.frba.dds.domain;

import ar.edu.utn.frba.dds.domain.fuentes.Fuente;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ServicioDeAgregacion extends Fuente {
  private List<Fuente> fuentesCargadas;

  public ServicioDeAgregacion(String nombre, List<Hecho> hechos) {
    super(nombre, hechos);
    this.fuentesCargadas = new ArrayList<>();
  }

  public void agregarFuente(Fuente fuente) {
    this.fuentesCargadas.add(fuente);
  }

  @Override
  public List<Hecho> obtenerHechos() {
    return fuentesCargadas.stream()
        .flatMap(fuente -> fuente.obtenerHechos().stream())
        .collect(Collectors.toList());
  }
}

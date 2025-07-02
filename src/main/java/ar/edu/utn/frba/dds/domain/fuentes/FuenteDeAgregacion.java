package ar.edu.utn.frba.dds.domain.fuentes;

import ar.edu.utn.frba.dds.domain.hecho.Hecho;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class FuenteDeAgregacion extends FuenteCacheable {

  private final List<Fuente> fuentesCargadas;

  public FuenteDeAgregacion(String nombre, String jsonFilePathParaCopias) {
    // FIX: Se llama al constructor del padre con todos los parámetros necesarios.
    super(nombre, jsonFilePathParaCopias);
    this.fuentesCargadas = new ArrayList<>();
    // FIX: Se elimina la llamada a iniciarScheduler del constructor.
  }

  public void agregarFuente(Fuente fuente) {
    this.fuentesCargadas.add(fuente);
  }

  public List<Fuente> getFuentesCargadas() {
    return new ArrayList<>(this.fuentesCargadas);
  }

  @Override
  protected List<Hecho> consultarNuevosHechos() {
    System.out.println("FuenteDeAgregacion: Consultando hechos de " + fuentesCargadas.size() + " fuentes internas...");
    return this.fuentesCargadas.stream()
        .flatMap(fuente -> fuente.obtenerHechos().stream())
        .collect(Collectors.toList());
  }
}

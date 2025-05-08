package ar.edu.utn.frba.dds.domain.fuentes;

import ar.edu.utn.frba.dds.domain.hecho.Hecho;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ServicioDeAgregacion extends Fuente {
  private List<Fuente> fuentesCargadas;

  public ServicioDeAgregacion(String nombre) {
    super(nombre, null); // no usamos una lista fija local
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

  @Override
  public void eliminarHecho(Hecho hecho) {
    for (Fuente fuente : fuentesCargadas) {
      try {
        fuente.eliminarHecho(hecho);
        return; // eliminado con éxito, salimos
      } catch (IllegalStateException e) {
        // ignoramos y seguimos buscando en otras fuentes
      }
    }
    throw new IllegalStateException("El hecho no se encontró en ninguna de las fuentes: " + hecho.getTitulo());
  }

  public List<Fuente> getFuentesCargadas() {
    return fuentesCargadas;
  }
}

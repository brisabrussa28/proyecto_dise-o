package ar.edu.utn.frba.dds.domain.fuentes.apis;

import ar.edu.utn.frba.dds.domain.fuentes.apis.serviciometamapa.HechoQuerys;
import ar.edu.utn.frba.dds.domain.fuentes.apis.serviciometamapa.ServicioMetaMapa;
import ar.edu.utn.frba.dds.domain.hecho.Hecho;
import java.io.IOException;
import java.util.List;

/**
 * Implementación del Adapter que sabe cómo comunicarse con el ServicioMetaMapa.
 * Envuelve la lógica de la llamada a la API de MetaMapa.
 */
public class AdapterMetaMapa implements FuenteAdapter {
  private final ServicioMetaMapa servicio;
  private final HechoQuerys query;

  public AdapterMetaMapa(ServicioMetaMapa servicio, HechoQuerys query) {
    this.servicio = servicio;
    this.query = query;
  }

  @Override
  public List<Hecho> consultarHechos() throws IOException {
    // La lógica de la llamada específica a Retrofit queda encapsulada aquí.
    return this.servicio.listadoDeHechos(this.query);
  }
}

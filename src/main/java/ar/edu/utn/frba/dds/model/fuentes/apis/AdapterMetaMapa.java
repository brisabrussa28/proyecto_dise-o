package ar.edu.utn.frba.dds.model.fuentes.apis;

import ar.edu.utn.frba.dds.model.fuentes.apis.serviciometamapa.HechoQuerys;
import ar.edu.utn.frba.dds.model.fuentes.apis.serviciometamapa.ServicioMetaMapa;
import ar.edu.utn.frba.dds.model.hecho.Hecho;

import java.io.IOException;
import java.util.List;

/**
 * Implementación del Adapter que sabe cómo comunicarse con el ServicioMetaMapa.
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
    return this.servicio.listadoDeHechos(this.query);
  }

}
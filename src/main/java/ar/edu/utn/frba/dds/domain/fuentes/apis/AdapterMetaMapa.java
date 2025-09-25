package ar.edu.utn.frba.dds.domain.fuentes.apis;

import ar.edu.utn.frba.dds.domain.fuentes.apis.serviciometamapa.HechoQuerys;
import ar.edu.utn.frba.dds.domain.fuentes.apis.serviciometamapa.ServicioMetaMapa;
import ar.edu.utn.frba.dds.domain.hecho.Hecho;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

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
package ar.edu.utn.frba.dds.controller;

import ar.edu.utn.frba.dds.dto.EstadisticaDTO;
import ar.edu.utn.frba.dds.model.coleccion.Coleccion;
import ar.edu.utn.frba.dds.model.estadisticas.CentralDeEstadisticas;
import ar.edu.utn.frba.dds.model.estadisticas.Estadistica;
import ar.edu.utn.frba.dds.model.reportes.GestorDeSolicitudes;
import ar.edu.utn.frba.dds.repositories.ColeccionRepository;
import ar.edu.utn.frba.dds.repositories.EstadisticaRepository;
import ar.edu.utn.frba.dds.repositories.SolicitudesRepository;
import java.util.List;

public class EstadisticaController {
  public Estadistica getEstadistica(EstadisticaDTO estadisticaDTO) {
    return EstadisticaRepository.instance()
                                .findByTipo(estadisticaDTO.getTipo());
  }

  public List<Estadistica> getEstadisticas() {
    return EstadisticaRepository.instance().findAll();
  }

  public Estadistica calcularEstadistica(EstadisticaDTO estadisticaDTO) {
    CentralDeEstadisticas central = new CentralDeEstadisticas();
    central.setGestor(new GestorDeSolicitudes(SolicitudesRepository.instance()));
    if (estadisticaDTO.getColeccion() != null ) {
      Coleccion coleccion = ColeccionRepository.instance()
                                               .findById(estadisticaDTO.getColeccion());
      Estadistica stat = central.provinciaConMasHechos(coleccion);
      EstadisticaRepository.instance()
                           .save(stat);
      return stat;
    }
    String categoria = estadisticaDTO.getCategoria();
    Estadistica stat = new Estadistica();
    switch (estadisticaDTO.getTipo()) {
      case "CATEGORIA CON MAS HECHOS":
        stat = central.categoriaConMasHechos();
        break;
      case "HORA CON MAS HECHOS DE UNA CATEGORIA":
        if (categoria != null) {
          stat = central.horaConMasHechosDeCiertaCategoria(categoria);
          break;
        }
        throw new RuntimeException("No se puede calcular la estadistica sin una categoria");
      case "PROVINCIA CON MAS HECHOS DE UNA CATEGORIA":
        if (categoria != null) {
          stat = central.provinciaConMasHechosDeCiertaCategoria(categoria);
          break;
        }
        throw new RuntimeException("No se puede calcular la estadistica sin una categoria");
      case "CANTIDAD DE SOLICITUDES SPAM":
        stat = central.cantidadDeSolicitudesSpam();
        break;
      default:
        throw new RuntimeException("No se especifico una estadistica valida");
    }
    EstadisticaRepository.instance()
                         .save(stat);
    return stat;
  }
}

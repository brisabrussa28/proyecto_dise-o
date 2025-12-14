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

  public List<Estadistica> getEstadisticas() {
    return EstadisticaRepository.instance().findAll();
  }

  public List<Estadistica> calcularEstadisticas(EstadisticaDTO dto) {

    CentralDeEstadisticas central = new CentralDeEstadisticas();
    central.setGestor(new GestorDeSolicitudes(SolicitudesRepository.instance()));

    List<Estadistica> resultado;

    switch (dto.getTipo()) {

      case "HECHOS POR PROVINCIA Y CATEGORIA":
        if (dto.getCategoria() == null)
          throw new RuntimeException("Debe especificarse una categoría");
        resultado = central.hechosPorProvinciaSegunCategoria(dto.getCategoria());
        break;

      case "HECHOS POR HORA Y CATEGORIA":
        if (dto.getCategoria() == null)
          throw new RuntimeException("Debe especificarse una categoría");
        resultado = central.hechosPorHora(dto.getCategoria());
        break;

      case "HECHOS REPORTADOS POR CATEGORIA":
        resultado = central.hechosPorCategoria();
        break;

      case "HECHOS REPORTADOS POR PROVINCIA Y COLECCION":
        if (dto.getColeccion() == null)
          throw new RuntimeException("Debe especificarse una colección");
        Coleccion col = ColeccionRepository.instance().findById(dto.getColeccion());
        if (col == null)
          throw new RuntimeException("La colección no existe");
        resultado = central.hechosPorProvinciaDeUnaColeccion(col);
        break;

      case "CANTIDAD DE HECHOS":
        resultado = List.of(central.calcularStatsCantHechos());
        break;

      case "CANTIDAD DE SOLICITUDES PENDIENTES":
        resultado = List.of(central.calcularStatsCantSolicitudes());
        break;

      case "CANTIDAD DE SPAM":
        resultado = List.of(central.calcularStatsCantSpam());
        break;

      default:
        throw new RuntimeException("Tipo de estadística no reconocido");
    }

    resultado.forEach(EstadisticaRepository.instance()::save);

    return resultado;
  }
}
package ar.edu.utn.frba.dds.controller;

import ar.edu.utn.frba.dds.dto.EstadisticaDTO;
import ar.edu.utn.frba.dds.model.coleccion.Coleccion;
import ar.edu.utn.frba.dds.model.estadisticas.CentralDeEstadisticas;
import ar.edu.utn.frba.dds.model.estadisticas.Estadistica;
import ar.edu.utn.frba.dds.model.reportes.GestorDeSolicitudes;
import ar.edu.utn.frba.dds.repositories.ColeccionRepository;
import ar.edu.utn.frba.dds.repositories.EstadisticaRepository;
import ar.edu.utn.frba.dds.repositories.SolicitudesRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class EstadisticaController {

  public List<Estadistica> getEstadisticas() {
    return EstadisticaRepository.instance().findAll();
  }

  public List<Estadistica> calcularEstadisticas(EstadisticaDTO dto) {
    CentralDeEstadisticas central = new CentralDeEstadisticas();
    central.setGestor(new GestorDeSolicitudes(SolicitudesRepository.instance()));

    List<Estadistica> resultado = new ArrayList<>();

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
        // OPTIMIZADO: Query directa a DB, devuelve Scalar values (no Entidades Hecho)
        Map<String, Long> conteoPorCategoria = SolicitudesRepository.instance().countHechosReportadosPorCategoria();

        for (Map.Entry<String, Long> entry : conteoPorCategoria.entrySet()) {
          resultado.add(new Estadistica(
              null,
              entry.getValue(),
              entry.getKey(),
              null,
              dto.getTipo()
          ));
        }
        break;

      case "HECHOS REPORTADOS POR PROVINCIA Y COLECCION":
        Long coleccionId = dto.getColeccion();
        if (coleccionId == null)
          throw new RuntimeException("Debe especificarse una colección");

        Coleccion col = ColeccionRepository.instance().findById(coleccionId);
        if (col == null)
          throw new RuntimeException("La colección no existe");

        // OPTIMIZADO: Query directa respetando flujo Coleccion -> Fuente -> Hecho.
        // No carga fotos ni hechos en memoria.
        Map<String, Long> conteoPorProvincia = SolicitudesRepository.instance()
                                                                    .countHechosReportadosPorProvinciaYColeccion(coleccionId);

        System.out.println("Stats REPORTADOS calculadas para Colección ID " + coleccionId);

        for (Map.Entry<String, Long> entry : conteoPorProvincia.entrySet()) {
          resultado.add(new Estadistica(
              entry.getKey(), // Provincia
              entry.getValue(), // Cantidad
              null,
              col,
              dto.getTipo()
          ));
        }
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
        throw new RuntimeException("Tipo de estadística no reconocido: " + dto.getTipo());
    }

    resultado.forEach(EstadisticaRepository.instance()::save);

    return resultado;
  }

  public List<String> getCategorias() {
    return new HechoController().getCategorias();
  }

  public List<Coleccion> getColeccionesConHechos() {
    // Nota: Usar con cuidado en el frontend, puede ser pesado.
    return ColeccionRepository.instance().findAllConFuentesYHechos();
  }

  public void calcularTodasLasEstadisticas() {
    try {
      System.out.println("Calculando todas las estadísticas...");

      this.calcularEstadisticas(new EstadisticaDTO("CANTIDAD DE HECHOS", null, null));
      this.calcularEstadisticas(new EstadisticaDTO("CANTIDAD DE SOLICITUDES PENDIENTES", null, null));
      this.calcularEstadisticas(new EstadisticaDTO("CANTIDAD DE SPAM", null, null));

      List<String> categorias = this.getCategorias();
      for (String categoria : categorias) {
        this.calcularEstadisticas(new EstadisticaDTO("HECHOS POR PROVINCIA Y CATEGORIA", categoria, null));
        this.calcularEstadisticas(new EstadisticaDTO("HECHOS POR HORA Y CATEGORIA", categoria, null));
      }

      // IMPORTANTE: Aquí estaba el error de N+1.
      // Cambiamos findAllConFuentesYHechos() por findAll().
      // No necesitamos traer los hechos ni las fotos para obtener el ID y calcular stats.
      List<Coleccion> colecciones = ColeccionRepository.instance().findAll();

      for (Coleccion col : colecciones) {
        // Solo intentamos calcular si la colección tiene una fuente asignada
        // (Verificación rápida sin traer todo el árbol)
        if (col.getFuente() != null) {
          try {
            this.calcularEstadisticas(
                new EstadisticaDTO("HECHOS REPORTADOS POR PROVINCIA Y COLECCION", null, col.getId())
            );
          } catch (Exception ex) {
            System.err.println("Error calculando stats para colección " + col.getTitulo() + ": " + ex.getMessage());
          }
        }
      }

      this.calcularEstadisticas(new EstadisticaDTO("HECHOS REPORTADOS POR CATEGORIA", null, null));

      System.out.println("Todas las estadísticas calculadas correctamente.");

    } catch (Exception e) {
      System.err.println("Error calculando estadísticas: " + e.getMessage());
      e.printStackTrace();
    }
  }
}
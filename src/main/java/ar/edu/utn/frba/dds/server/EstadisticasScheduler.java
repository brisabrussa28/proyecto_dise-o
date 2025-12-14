package ar.edu.utn.frba.dds.server;

import ar.edu.utn.frba.dds.controller.EstadisticaController;
import ar.edu.utn.frba.dds.dto.EstadisticaDTO;
import ar.edu.utn.frba.dds.repositories.ColeccionRepository;
import ar.edu.utn.frba.dds.repositories.EstadisticaRepository;
import ar.edu.utn.frba.dds.model.coleccion.Coleccion;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class EstadisticasScheduler {

  private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
  private final EstadisticaController estadisticaController = new EstadisticaController();

  public void iniciar() {
    recalcularTodas();

    scheduler.scheduleAtFixedRate(
        this::recalcularTodas,
        1,
        1,
        TimeUnit.DAYS
    );
  }

  private void recalcularTodas() {
    try {
      System.out.println("♻ Recalculando estadísticas...");

      EstadisticaRepository.instance().deleteAll();

      estadisticaController.calcularEstadisticas(
          new EstadisticaDTO("CANTIDAD DE HECHOS", null, null)
      );
      estadisticaController.calcularEstadisticas(
          new EstadisticaDTO("CANTIDAD DE SOLICITUDES PENDIENTES", null, null)
      );
      estadisticaController.calcularEstadisticas(
          new EstadisticaDTO("CANTIDAD DE SPAM", null, null)
      );

      List<String> categorias = estadisticaController.getCategorias();

      for (String categoria : categorias) {
        estadisticaController.calcularEstadisticas(
            new EstadisticaDTO("HECHOS POR PROVINCIA Y CATEGORIA", categoria, null)
        );
        estadisticaController.calcularEstadisticas(
            new EstadisticaDTO("HECHOS POR HORA Y CATEGORIA", categoria, null)
        );
      }

      List<Coleccion> colecciones = ColeccionRepository.instance().findAll();

      for (Coleccion col : colecciones) {
        estadisticaController.calcularEstadisticas(
            new EstadisticaDTO("HECHOS REPORTADOS POR PROVINCIA Y COLECCION", null, col.getId())
        );
      }

      estadisticaController.calcularEstadisticas(
          new EstadisticaDTO("HECHOS REPORTADOS POR CATEGORIA", null, null)
      );

      System.out.println("✅ Estadísticas recalculadas correctamente.");

    } catch (Exception e) {
      System.err.println("❌ Error recalculando estadísticas: " + e.getMessage());
    }
  }
}
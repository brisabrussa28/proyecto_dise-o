package ar.edu.utn.frba.dds.server;

import ar.edu.utn.frba.dds.controller.EstadisticaController;
import ar.edu.utn.frba.dds.dto.EstadisticaDTO;
import ar.edu.utn.frba.dds.model.coleccion.Coleccion;
import ar.edu.utn.frba.dds.utils.DBUtils;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class EstadisticasScheduler {

  private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
  private final EstadisticaController estadisticaController = new EstadisticaController();

  public void iniciar() {
    System.out.println("Iniciando scheduler de estadísticas...");

    recalcularTodas();

    scheduler.scheduleAtFixedRate(
        this::recalcularTodas,
        1,
        1,
        TimeUnit.DAYS
    );

    System.out.println("Scheduler programado para ejecutarse diariamente");
  }

  private void recalcularTodas() {
    EntityManager em = null;
    try {
      System.out.println("Recalculando estadísticas...");

      System.out.println("Borrando estadísticas antiguas...");
      em = DBUtils.getEntityManager();
      DBUtils.comenzarTransaccion(em);
      em.createQuery("DELETE FROM Estadistica").executeUpdate();
      DBUtils.commit(em);
      em.close();
      System.out.println("Estadísticas antiguas eliminadas");

      System.out.println("Calculando cantidad de hechos...");
      estadisticaController.calcularEstadisticas(
          new EstadisticaDTO("CANTIDAD DE HECHOS", null, null)
      );

      System.out.println("Calculando solicitudes pendientes...");
      estadisticaController.calcularEstadisticas(
          new EstadisticaDTO("CANTIDAD DE SOLICITUDES PENDIENTES", null, null)
      );

      System.out.println("Calculando cantidad de spam...");
      estadisticaController.calcularEstadisticas(
          new EstadisticaDTO("CANTIDAD DE SPAM", null, null)
      );

      System.out.println("Obteniendo categorías...");
      List<String> categorias = estadisticaController.getCategorias();
      System.out.println("Se encontraron " + categorias.size() + " categorías");

      if (!categorias.isEmpty()) {
        System.out.println("Calculando estadísticas por categoría...");
        for (String categoria : categorias) {
          try {
            estadisticaController.calcularEstadisticas(
                new EstadisticaDTO("HECHOS POR PROVINCIA Y CATEGORIA", categoria, null)
            );
            estadisticaController.calcularEstadisticas(
                new EstadisticaDTO("HECHOS POR HORA Y CATEGORIA", categoria, null)
            );
          } catch (Exception e) {
            System.err.println("Error con categoría '" + categoria + "': " + e.getMessage());
          }
        }
        System.out.println("Estadísticas por categoría calculadas");
      }

      System.out.println("Cargando colecciones con hechos...");
      em = DBUtils.getEntityManager();
      List<Coleccion> colecciones = em.createQuery(
                                          "SELECT DISTINCT c FROM Coleccion c " +
                                              "LEFT JOIN FETCH c.coleccion_fuente f " +
                                              "LEFT JOIN FETCH f.hechosPersistidos " +
                                              "WHERE f IS NOT NULL", Coleccion.class)
                                      .getResultList();
      em.close();

      System.out.println("Se cargaron " + colecciones.size() + " colecciones con hechos");

      if (!colecciones.isEmpty()) {
        System.out.println("Calculando estadísticas por colección...");
        for (Coleccion col : colecciones) {
          try {
            if (col.getFuente() != null && !col.getFuente().getHechos().isEmpty()) {
              estadisticaController.calcularEstadisticas(
                  new EstadisticaDTO("HECHOS REPORTADOS POR PROVINCIA Y COLECCION", null, col.getId())
              );
            }
          } catch (Exception e) {
            System.err.println("Error con colección '" + col.getTitulo() + "': " + e.getMessage());
          }
        }
        System.out.println("Estadísticas por colección calculadas");
      }

      System.out.println("Calculando estadística general por categoría...");
      estadisticaController.calcularEstadisticas(
          new EstadisticaDTO("HECHOS REPORTADOS POR CATEGORIA", null, null)
      );

      em = DBUtils.getEntityManager();
      long totalEstadisticas = em.createQuery("SELECT COUNT(e) FROM Estadistica e", Long.class)
                                 .getSingleResult();
      em.close();

      System.out.println("Estadísticas recalculadas correctamente. Total: " + totalEstadisticas + " estadísticas guardadas.");

    } catch (Exception e) {
      System.err.println("ERROR CRÍTICO recalculando estadísticas: " + e.getMessage());
      e.printStackTrace();

      if (em != null && em.isOpen()) {
        if (em.getTransaction().isActive()) {
          try {
            DBUtils.rollback(em);
          } catch (Exception rollbackEx) {
            System.err.println("Error al hacer rollback: " + rollbackEx.getMessage());
          }
        }
        em.close();
      }

      try {
        guardarErrorEnEstadisticas(e.getMessage());
      } catch (Exception ex) {
        System.err.println("No se pudo guardar el registro de error: " + ex.getMessage());
      }
    } finally {
      if (em != null && em.isOpen()) {
        em.close();
      }
    }
  }

  private void guardarErrorEnEstadisticas(String mensajeError) {
    EntityManager em = null;
    try {
      em = DBUtils.getEntityManager();
      DBUtils.comenzarTransaccion(em);

      ar.edu.utn.frba.dds.model.estadisticas.Estadistica errorStat =
          new ar.edu.utn.frba.dds.model.estadisticas.Estadistica(
              "ERROR",
              0L,
              null,
              null,
              "ERROR_EN_SCHEDULER"
          );

      em.persist(errorStat);
      DBUtils.commit(em);

      System.out.println("Se registró error en estadísticas: " + mensajeError);
    } catch (Exception e) {
      System.err.println("No se pudo guardar estadística de error: " + e.getMessage());
    } finally {
      if (em != null && em.isOpen()) {
        em.close();
      }
    }
  }

  public void detener() {
    System.out.println("Deteniendo scheduler de estadísticas...");
    scheduler.shutdown();
    try {
      if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
        scheduler.shutdownNow();
      }
    } catch (InterruptedException e) {
      scheduler.shutdownNow();
      Thread.currentThread().interrupt();
    }
    System.out.println("Scheduler detenido");
  }
}
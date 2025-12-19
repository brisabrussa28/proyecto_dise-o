package ar.edu.utn.frba.dds.server;

import ar.edu.utn.frba.dds.controller.ColeccionController;
import ar.edu.utn.frba.dds.controller.EstadisticaController;
import ar.edu.utn.frba.dds.dto.EstadisticaDTO;
import ar.edu.utn.frba.dds.model.coleccion.Coleccion;
import ar.edu.utn.frba.dds.repositories.ColeccionRepository;
import ar.edu.utn.frba.dds.utils.DBUtils;

import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;

import java.util.logging.Logger;
import java.util.logging.Level;
import javax.persistence.EntityManager;
import java.util.List;

public class EstadisticasScheduler {

  private static final Logger logger = Logger.getLogger(EstadisticasScheduler.class.getName());
  private Scheduler scheduler;
  private static final EstadisticaController estadisticaController = new EstadisticaController();
  private static final ColeccionController coleccionController = new ColeccionController();

  public void iniciar() {
    logger.info("Iniciando scheduler de estadísticas con Cron...");

    recalcularTodas();
    recalcularConsenso();

    try {
      scheduler = StdSchedulerFactory.getDefaultScheduler();

      JobDetail job = JobBuilder.newJob(RecalcularTodasJob.class)
                                .withIdentity("jobEstadisticas", "groupEstadisticas")
                                .build();

      CronTrigger trigger = TriggerBuilder.newTrigger()
                                          .withIdentity("triggerDiario", "groupEstadisticas")
                                          .withSchedule(CronScheduleBuilder.cronSchedule("0 */15 * * * ?"))
                                          .build();

      scheduler.scheduleJob(job, trigger);
      scheduler.start();

      logger.info("Scheduler de estadísticas iniciado correctamente.");

    } catch (SchedulerException e) {
      logger.log(Level.SEVERE, "No se pudo iniciar el scheduler de estadísticas", e);
    }
  }

  public static class RecalcularTodasJob implements Job {
    @Override
    public void execute(JobExecutionContext context) {
      logger.info("=== Iniciando ejecución programada ===");
      recalcularTodas();
      recalcularConsenso();
      logger.info("=== Ejecución programada finalizada ===");
    }
  }

  private static void recalcularTodas() {
    EntityManager em = null;
    try {
      logger.info("Recalculando estadísticas...");

      logger.info("Borrando estadísticas antiguas...");
      em = DBUtils.getEntityManager();
      DBUtils.comenzarTransaccion(em);
      em.createQuery("DELETE FROM Estadistica").executeUpdate();
      DBUtils.commit(em);
      em.close();
      logger.info("Estadísticas antiguas eliminadas");

      logger.info("Calculando cantidad de hechos...");
      estadisticaController.calcularEstadisticas(
          new EstadisticaDTO("CANTIDAD DE HECHOS", null, null)
      );

      logger.info("Calculando solicitudes pendientes...");
      estadisticaController.calcularEstadisticas(
          new EstadisticaDTO("CANTIDAD DE SOLICITUDES PENDIENTES", null, null)
      );

      logger.info("Calculando cantidad de spam...");
      estadisticaController.calcularEstadisticas(
          new EstadisticaDTO("CANTIDAD DE SPAM", null, null)
      );

      logger.info("Obteniendo categorías...");
      List<String> categorias = estadisticaController.getCategorias();
      logger.info("Se encontraron " + categorias.size() + " categorías");

      if (!categorias.isEmpty()) {
        logger.info("Calculando estadísticas por categoría...");
        for (String categoria : categorias) {
          try {
            estadisticaController.calcularEstadisticas(
                new EstadisticaDTO("HECHOS POR PROVINCIA Y CATEGORIA", categoria, null)
            );
            estadisticaController.calcularEstadisticas(
                new EstadisticaDTO("HECHOS POR HORA Y CATEGORIA", categoria, null)
            );
          } catch (Exception e) {
            logger.warning("Error con categoría '" + categoria + "': " + e.getMessage());
          }
        }
        logger.info("Estadísticas por categoría calculadas");
      }

      logger.info("Cargando colecciones con hechos...");
      List<Coleccion> colecciones = ColeccionRepository.instance()
                                                       .findAllConFuentesYHechos();

      logger.info("Se cargaron " + colecciones.size() + " colecciones con hechos");

      if (!colecciones.isEmpty()) {
        logger.info("Calculando estadísticas por colección...");
        for (Coleccion col : colecciones) {
          try {
            if (col.getFuente() != null && !col.getFuente().getHechos().isEmpty()) {
              estadisticaController.calcularEstadisticas(
                  new EstadisticaDTO(
                      "HECHOS REPORTADOS POR PROVINCIA Y COLECCION",
                      null,
                      col.getId()
                  )
              );
            }
          } catch (Exception e) {
            logger.warning("Error con colección '" + col.getTitulo() + "': " + e.getMessage());
          }
        }
        logger.info("Estadísticas por colección calculadas");
      }

      logger.info("Calculando estadística general por categoría...");
      estadisticaController.calcularEstadisticas(
          new EstadisticaDTO("HECHOS REPORTADOS POR CATEGORIA", null, null)
      );

      em = DBUtils.getEntityManager();
      long totalEstadisticas = em.createQuery("SELECT COUNT(e) FROM Estadistica e", Long.class)
                                 .getSingleResult();
      em.close();

      logger.info("Estadísticas recalculadas correctamente. Total: " + totalEstadisticas + " estadísticas guardadas.");

    } catch (Exception e) {
      logger.log(Level.SEVERE, "ERROR CRÍTICO recalculando estadísticas", e);

      if (em != null && em.isOpen()) {
        if (em.getTransaction().isActive()) {
          try {
            DBUtils.rollback(em);
          } catch (Exception rollbackEx) {
            logger.log(Level.SEVERE, "Error al hacer rollback", rollbackEx);
          }
        }
        em.close();
      }

      try {
        guardarErrorEnEstadisticas(e.getMessage());
      } catch (Exception ex) {
        logger.log(Level.SEVERE, "No se pudo guardar el registro de error", ex);
      }
    } finally {
      if (em != null && em.isOpen()) {
        em.close();
      }
    }
  }

  private static void guardarErrorEnEstadisticas(String mensajeError) {
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

  private static void recalcularConsenso() {
    EntityManager em = null;
    try {
      em = DBUtils.getEntityManager();
      DBUtils.comenzarTransaccion(em);
      List<Coleccion> colecciones = em.createQuery("SELECT c FROM Coleccion c", Coleccion.class)
                                      .getResultList();

      for (Coleccion col : colecciones) {
        try {
          col.recalcularConsenso();
          em.merge(col);
        } catch (Exception e) {
          Logger.getLogger("logConsenso")
                .severe("Error recalculando colección ID " + col.getId() + ": " + e.getMessage());
        }
      }
      DBUtils.commit(em);
      Logger.getLogger("logConsenso").info("Consensos recalculados correctamente.");

    } catch (Exception e) {
      Logger.getLogger("logConsenso")
            .severe("Error CRÍTICO en job de consensos: " + e.getMessage());
      e.printStackTrace();
      if (em != null && em.getTransaction().isActive()) {
        DBUtils.rollback(em);
      }
    } finally {
      if (em != null && em.isOpen()) {
        em.close();
      }
    }
  }

  public void detener() {
    System.out.println("Deteniendo scheduler de estadísticas...");
    try {
      if (scheduler != null && !scheduler.isShutdown()) {
        scheduler.shutdown(true);
        System.out.println("Scheduler detenido correctamente");
      }
    } catch (SchedulerException e) {
      System.err.println("Error al detener scheduler: " + e.getMessage());
    }
  }
}
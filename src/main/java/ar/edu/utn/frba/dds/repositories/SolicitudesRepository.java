package ar.edu.utn.frba.dds.repositories;

import ar.edu.utn.frba.dds.model.hecho.Hecho;
import ar.edu.utn.frba.dds.model.reportes.EstadoSolicitud;
import ar.edu.utn.frba.dds.model.reportes.Solicitud;
import ar.edu.utn.frba.dds.utils.DBUtils;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceException;

public class SolicitudesRepository {

  private static final SolicitudesRepository INSTANCE = new SolicitudesRepository();

  public static SolicitudesRepository instance() {
    return INSTANCE;
  }

  public void guardar(Solicitud solicitud) {
    Solicitud existente = buscarPorHechoYRazon(
        solicitud.getHechoSolicitado(),
        solicitud.getRazonEliminacion()
    );

    EntityManager em = DBUtils.getEntityManager();
    try {
      DBUtils.comenzarTransaccion(em);
      if (existente != null) {
        solicitud.setId(existente.getId());
        em.merge(solicitud);
      } else {
        em.persist(solicitud);
      }
      DBUtils.commit(em);
    } catch (PersistenceException e) {
      DBUtils.rollback(em);
      throw new RuntimeException(e.getMessage());
    } finally {
      em.close();
    }
  }

  public Solicitud findById(Long id) {
    EntityManager em = DBUtils.getEntityManager();
    try {
      return em.find(Solicitud.class, id);
    } finally {
      em.close();
    }
  }

  public Solicitud buscarPorHechoYRazon(Hecho hecho, String razon) {
    EntityManager em = DBUtils.getEntityManager();
    try {
      return em.createQuery(
                   "SELECT s FROM Solicitud s WHERE s.hechoSolicitado = :hecho AND s.razonEliminacion = :razon",
                   Solicitud.class
               )
               .setParameter("hecho", hecho)
               .setParameter("razon", razon)
               .getResultStream()
               .findFirst()
               .orElse(null);
    } finally {
      em.close();
    }
  }

  public List<Solicitud> findAll() {
    EntityManager em = DBUtils.getEntityManager();
    try {
      return em.createQuery("SELECT s FROM Solicitud s", Solicitud.class)
               .getResultList();
    } finally {
      em.close();
    }
  }

  public List<Solicitud> obtenerPorEstado(EstadoSolicitud estado) {
    EntityManager em = DBUtils.getEntityManager();
    try {
      return em.createQuery("SELECT s FROM Solicitud s WHERE s.estado = :estado", Solicitud.class)
               .setParameter("estado", estado)
               .getResultList();
    } finally {
      em.close();
    }
  }

  public Long cantidadTotal() {
    EntityManager em = DBUtils.getEntityManager();
    try {
      return em.createQuery("SELECT COUNT(s) FROM Solicitud s where s.estado = 'PENDIENTE'", Long.class)
               .getSingleResult();
    } finally {
      em.close();
    }
  }

  public void aceptarSolicitud(Solicitud solicitud) {
    EntityManager em = DBUtils.getEntityManager();
    try {
      DBUtils.comenzarTransaccion(em);
      solicitud.aceptar();
      em.merge(solicitud);
      DBUtils.commit(em);
    } catch (Exception e) {
      DBUtils.rollback(em);
      throw e;
    } finally {
      em.close();
    }
  }

  public void rechazarSolicitud(Solicitud solicitud) {
    EntityManager em = DBUtils.getEntityManager();
    try {
      DBUtils.comenzarTransaccion(em);
      solicitud.rechazar();
      em.merge(solicitud);
      DBUtils.commit(em);
    } catch (PersistenceException e) {
      DBUtils.rollback(em);
      throw new RuntimeException(e.getMessage());
    } finally {
      em.close();
    }
  }

  // --- QUERIES OPTIMIZADAS ---

  public Map<String, Long> countHechosReportadosPorCategoria() {
    EntityManager em = DBUtils.getEntityManager();
    try {
      List<Object[]> resultados = em.createQuery(
          "SELECT h.categoria, COUNT(s) " +
              "FROM Solicitud s " +
              "JOIN s.hechoSolicitado h " +
              "WHERE h.categoria IS NOT NULL " +
              "GROUP BY h.categoria", Object[].class
      ).getResultList();

      Map<String, Long> mapa = new HashMap<>();
      for (Object[] fila : resultados) {
        mapa.put((String) fila[0], (Long) fila[1]);
      }
      return mapa;
    } finally {
      em.close();
    }
  }

  public Map<String, Long> countHechosReportadosPorProvinciaYColeccion(Long coleccionId) {
    EntityManager em = DBUtils.getEntityManager();
    try {
      // ESTO SOLUCIONA EL PROBLEMA DE LAZY INITIALIZATION Y N+1
      // Usamos una subquery para filtrar los hechos que pertenecen a la colección
      // a través de su Fuente, sin traer objetos a memoria.
      List<Object[]> resultados = em.createQuery(
                                        "SELECT h.provincia, COUNT(s) " +
                                            "FROM Solicitud s " +
                                            "JOIN s.hechoSolicitado h " +
                                            "WHERE h.provincia IS NOT NULL " +
                                            "AND h.id IN (" +
                                            "   SELECT fh.id FROM Coleccion c " +
                                            "   JOIN c.fuente f " +
                                            "   JOIN f.hechos fh " +
                                            "   WHERE c.id = :coleccionId" +
                                            ") " +
                                            "GROUP BY h.provincia", Object[].class
                                    )
                                    .setParameter("coleccionId", coleccionId)
                                    .getResultList();

      Map<String, Long> mapa = new HashMap<>();
      for (Object[] fila : resultados) {
        mapa.put((String) fila[0], (Long) fila[1]);
      }
      return mapa;
    } finally {
      em.close();
    }
  }
}

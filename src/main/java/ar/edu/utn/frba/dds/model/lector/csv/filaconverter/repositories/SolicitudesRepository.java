package ar.edu.utn.frba.dds.model.lector.csv.filaconverter.repositories;

import ar.edu.utn.frba.dds.model.hecho.Hecho;
import ar.edu.utn.frba.dds.model.reportes.EstadoSolicitud;
import ar.edu.utn.frba.dds.model.reportes.Solicitud;
import ar.edu.utn.frba.dds.utils.DBUtils;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceException;

public class SolicitudesRepository {

  private static final SolicitudesRepository INSTANCE = new SolicitudesRepository();

  public static SolicitudesRepository instance() {
    return INSTANCE;
  }

  public void guardar(Solicitud solicitud) {
    // Buscamos primero para no tener transacciones anidadas con EMs distintos
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
      return em.createQuery("SELECT COUNT(s) FROM Solicitud s", Long.class)
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
}
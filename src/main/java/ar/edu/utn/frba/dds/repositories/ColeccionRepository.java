package ar.edu.utn.frba.dds.repositories;

import ar.edu.utn.frba.dds.model.coleccion.Coleccion;
import ar.edu.utn.frba.dds.model.hecho.Hecho;
import ar.edu.utn.frba.dds.utils.DBUtils;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceException;

public class ColeccionRepository {
  private static final ColeccionRepository INSTANCE = new ColeccionRepository();

  public static ColeccionRepository instance() {
    return INSTANCE;
  }

  public void save(Coleccion coleccion) {
    EntityManager em = DBUtils.getEntityManager();
    coleccion.recalcularConsenso();
    DBUtils.comenzarTransaccion(em);
    try {
      if (coleccion.getHechos() != null) {
        for (Hecho h : coleccion.getHechos()) {
          DBUtils.enriquecerHecho(h);
          if (h.getId() == null) {
            em.persist(h);
          } else {
            em.merge(h);
          }
        }
      }
      if (coleccion.getId() == null) {
        em.persist(coleccion);
      } else {
        em.merge(coleccion);
      }
      DBUtils.commit(em);
    } catch (PersistenceException e) {
      DBUtils.rollback(em);
      throw new RuntimeException(e.getMessage());
    } finally {
      em.close();
    }
  }

  public List<Coleccion> findAll() {
    EntityManager em = DBUtils.getEntityManager();
    try {
      return em.createQuery("select c from Coleccion c", Coleccion.class)
               .getResultList();
    } finally {
      if (em != null && em.isOpen()) {
        em.close();
      }
    }
  }

  public Coleccion findById(Long id) {
    EntityManager em = DBUtils.getEntityManager();
    try {
      return em.createQuery(
                   "SELECT DISTINCT c FROM Coleccion c " + "LEFT JOIN FETCH c.hechos " + "LEFT JOIN FETCH c.coleccion_fuente f " + "LEFT JOIN FETCH f.hechosPersistidos " + "WHERE c.coleccion_id = :id",
                   Coleccion.class
               )
               .setParameter("id", id)
               .getSingleResult();
    } catch (NoResultException e) {
      return null;
    } finally {
      em.close();
    }
  }

  public List<Coleccion> findAllConFuentesYHechos() {
    EntityManager em = DBUtils.getEntityManager();
    try {
      return em.createQuery(
                   "SELECT DISTINCT c FROM Coleccion c " + "LEFT JOIN FETCH c.coleccion_fuente f " + "LEFT JOIN FETCH f.hechosPersistidos " + "WHERE f IS NOT NULL",
                   Coleccion.class
               )
               .getResultList();
    } finally {
      em.close();
    }
  }

  // Buscar colecciones por ID de fuente
  public List<Coleccion> findByFuenteId(Long fuenteId) {
    EntityManager em = DBUtils.getEntityManager();
    try {
      return em.createQuery(
                   "SELECT c FROM Coleccion c WHERE c.coleccion_fuente.id = :fuenteId",
                   Coleccion.class
               )
               .setParameter("fuenteId", fuenteId)
               .getResultList();
    } finally {
      em.close();
    }
  }

  public void delete(Coleccion coleccion) {
    EntityManager em = DBUtils.getEntityManager();
    DBUtils.comenzarTransaccion(em);
    try {
      Coleccion managed = em.contains(coleccion) ? coleccion : em.merge(coleccion);
      em.remove(managed);
      DBUtils.commit(em);
    } catch (Exception e) {
      DBUtils.rollback(em);
      throw new RuntimeException("Error al eliminar colección", e);
    } finally {
      em.close();
    }
  }

  public Long countAll() {
    EntityManager em = DBUtils.getEntityManager();
    try {
      return em.createQuery("SELECT COUNT(DISTINCT c.coleccion_id) FROM Coleccion c", Long.class)
               .getSingleResult();
    } finally {
      em.close();
    }
  }

  public List<String> getCategorias() {
    EntityManager em = DBUtils.getEntityManager();
    try {
      return em.createQuery("select distinct c.coleccion_categoria from Coleccion c", String.class)
               .getResultList();
    } finally {
      em.close();
    }
  }

  public List<Coleccion> findAllConHechos() {
    EntityManager em = DBUtils.getEntityManager();
    try {
      return em.createQuery(
                   "SELECT DISTINCT c FROM Coleccion c " +
                       "LEFT JOIN FETCH c.coleccion_fuente f " +
                       "LEFT JOIN FETCH f.hechosPersistidos h " +
                       "LEFT JOIN FETCH h.etiquetas " +
                       "LEFT JOIN FETCH h.fotos",
                   Coleccion.class
               )
               .getResultList();
    } finally {
      em.close();
    }
  }

  // MÉTODO ADICIONAL: Para forzar la inicialización de la colección de hechos
  public Coleccion findByIdConHechosInicializados(Long id) {
    EntityManager em = DBUtils.getEntityManager();
    try {
      Coleccion coleccion = em.createQuery(
                                  "SELECT DISTINCT c FROM Coleccion c " +
                                      "LEFT JOIN FETCH c.coleccion_fuente f " +
                                      "WHERE c.coleccion_id = :id", Coleccion.class
                              )
                              .setParameter("id", id)
                              .getSingleResult();

      // Forzar la inicialización de la colección lazy
      if (coleccion != null && coleccion.getFuente() != null) {
        coleccion.getFuente()
                 .getHechos()
                 .size(); // Esto fuerza el load
      }

      return coleccion;
    } catch (NoResultException e) {
      return null;
    } finally {
      em.close();
    }
  }
}
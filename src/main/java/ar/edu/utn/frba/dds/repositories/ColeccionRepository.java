package ar.edu.utn.frba.dds.repositories;

import ar.edu.utn.frba.dds.model.coleccion.Coleccion;
import ar.edu.utn.frba.dds.model.hecho.Hecho; // Import necesario
import ar.edu.utn.frba.dds.utils.DBUtils;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceException;
import javax.persistence.NoResultException; // Import necesario para manejar queries sin resultado
import org.hibernate.Hibernate;

public class ColeccionRepository {
  private static final ColeccionRepository INSTANCE = new ColeccionRepository();

  public static ColeccionRepository instance() {
    return INSTANCE;
  }

  public void save(Coleccion coleccion) {
    EntityManager em = DBUtils.getEntityManager();

    // 1. Recalcular consenso (actualiza la lista en memoria)
    coleccion.recalcularConsenso();

    DBUtils.comenzarTransaccion(em);
    try {
      // 2. Persistir Hechos NUEVOS antes de guardar la Colección.
      // Esto es crucial para que tengan ID y no fallen las relaciones.
      if (coleccion.getHechos() != null) {
        for (Hecho h : coleccion.getHechos()) {
          DBUtils.enriquecerHecho(h);
          if (h.getId() == null) {
            em.persist(h); // Asigna ID
          } else {
            em.merge(h);   // Asegura que esté en el contexto
          }
        }
      }

      // 3. Guardar la Colección (y sus relaciones ya válidas)
      if (coleccion.getId() == null) {
        em.persist(coleccion);
      } else {
        em.merge(coleccion);
      }
      DBUtils.commit(em);
    } catch (PersistenceException e) {
      DBUtils.rollback(em);
      throw new RuntimeException("Error al guardar la colección: " + e.getMessage(), e);
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
      em.close();
    }
  }

  public Coleccion findById(Long id) {
    EntityManager em = DBUtils.getEntityManager();
    try {
      // SOLUCIÓN: Usamos el nombre exacto del atributo 'coleccion_id' y DISTINCT.
      // Esto guía a Hibernate para generar el SQL correcto sobre la tabla de unión.
      return em.createQuery("SELECT DISTINCT c FROM Coleccion c LEFT JOIN FETCH c.hechos WHERE c.coleccion_id = :id", Coleccion.class)
               .setParameter("id", id)
               .getSingleResult();
    } catch (NoResultException e) {
      // Si no encuentra la colección, devolvemos null para mantener compatibilidad
      return null;
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

  public Long countAll() {
    EntityManager em = DBUtils.getEntityManager();
    try {
      // Usamos coleccion_id explícitamente aquí también
      return em.createQuery("SELECT COUNT(DISTINCT c.coleccion_id) FROM Coleccion c", Long.class)
               .getSingleResult();
    } finally {
      em.close();
    }
  }
}
package ar.edu.utn.frba.dds.repositories;

import ar.edu.utn.frba.dds.model.coleccion.Coleccion;
import ar.edu.utn.frba.dds.model.fuentes.Fuente;
import ar.edu.utn.frba.dds.model.fuentes.FuenteDeAgregacion;
import ar.edu.utn.frba.dds.model.fuentes.FuenteEstatica;
import ar.edu.utn.frba.dds.utils.DBUtils;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceException;

public class FuenteRepository {
  private static final FuenteRepository INSTANCE = new FuenteRepository();

  public static FuenteRepository instance() {
    return INSTANCE;
  }

  public void save(Fuente fuente) {
    EntityManager em = DBUtils.getEntityManager();
    fuente.getHechos().forEach(DBUtils::enriquecerHecho);
    DBUtils.comenzarTransaccion(em);
    try {
      if (fuente.getId() == null) {
        em.persist(fuente);
        em.flush();
      } else {
        fuente = em.merge(fuente);
      }

      if (fuente instanceof FuenteEstatica) {
        FuenteEstatica fuenteEstatica = (FuenteEstatica) fuente;
        fuenteEstatica.getHechos().forEach(DBUtils::enriquecerHecho);
      }

      DBUtils.commit(em);
    } catch (PersistenceException e) {
      DBUtils.rollback(em);
      throw new RuntimeException("Error al guardar fuente: " + e.getMessage());
    } finally {
      em.close();
    }
  }

  public void update(Fuente fuente) {
    EntityManager em = DBUtils.getEntityManager();
    DBUtils.comenzarTransaccion(em);
    try {
      Fuente managedFuente = em.merge(fuente);

      if (managedFuente instanceof FuenteEstatica) {
        FuenteEstatica fuenteEstatica = (FuenteEstatica) managedFuente;
        fuenteEstatica.getHechos().forEach(DBUtils::enriquecerHecho);
      }

      DBUtils.commit(em);
    } catch (PersistenceException e) {
      DBUtils.rollback(em);
      throw new RuntimeException("Error al actualizar fuente: " + e.getMessage());
    } finally {
      em.close();
    }
  }

  public List<Fuente> findAll() {
    EntityManager em = DBUtils.getEntityManager();
    try {
      return em.createQuery(
                   "SELECT DISTINCT f FROM Fuente f " +
                       "LEFT JOIN FETCH f.hechosPersistidos",
                   Fuente.class)
               .getResultList();
    } finally {
      em.close();
    }
  }

  public Fuente findById(Long id) {
    EntityManager em = DBUtils.getEntityManager();
    try {
      return em.createQuery(
                   "SELECT f FROM Fuente f " +
                       "LEFT JOIN FETCH f.hechosPersistidos " +
                       "WHERE f.fuente_id = :id", Fuente.class)
               .setParameter("id", id)
               .getSingleResult();
    } catch (Exception e) {
      try {
        return em.find(Fuente.class, id);
      } finally {
        em.close();
      }
    }
  }

  public Fuente findByIdConHechos(Long id) {
    EntityManager em = DBUtils.getEntityManager();
    try {
      return em.createQuery(
                   "SELECT DISTINCT f FROM Fuente f " +
                       "LEFT JOIN FETCH f.hechosPersistidos h " +
                       "LEFT JOIN FETCH h.etiquetas " +
                       "LEFT JOIN FETCH h.fotos " +
                       "WHERE f.fuente_id = :id", Fuente.class)
               .setParameter("id", id)
               .getSingleResult();
    } finally {
      em.close();
    }
  }

  public void delete(Fuente fuente) {
    EntityManager em = DBUtils.getEntityManager();
    DBUtils.comenzarTransaccion(em);
    try {
      eliminarRecursivamente(fuente, em);
      DBUtils.commit(em);
    } catch (PersistenceException e) {
      DBUtils.rollback(em);
      throw new RuntimeException("Error al eliminar la fuente: " + e.getMessage());
    } catch (Exception e) {
      DBUtils.rollback(em);
      throw new RuntimeException("Error inesperado al eliminar la fuente: " + e.getMessage());
    } finally {
      em.close();
    }
  }

  private void eliminarRecursivamente(Fuente fuente, EntityManager em) {
    List<Coleccion> coleccionesDirectas = em.createQuery(
                                                "SELECT c FROM Coleccion c WHERE c.coleccion_fuente.id = :id",
                                                Coleccion.class)
                                            .setParameter("id", fuente.getId())
                                            .getResultList();

    for (Coleccion c : coleccionesDirectas) {
      Coleccion managedCol = em.contains(c) ? c : em.merge(c);
      em.remove(managedCol);
    }

    List<FuenteDeAgregacion> padres = em.createQuery(
                                            "SELECT f FROM FuenteDeAgregacion f JOIN f.fuentesCargadas hija WHERE hija.id = :hijoId",
                                            FuenteDeAgregacion.class)
                                        .setParameter("hijoId", fuente.getId())
                                        .getResultList();

    for (FuenteDeAgregacion padre : padres) {
      em.refresh(padre);

      padre.removerFuente(fuente);
      em.merge(padre);

      if (padre.getFuentesCargadas().isEmpty()) {
        eliminarRecursivamente(padre, em);
      }
    }

    Fuente managed = em.contains(fuente) ? fuente : em.merge(fuente);
    em.remove(managed);
  }

  public List<FuenteDeAgregacion> findAgregacionesByHija(Long hijoId) {
    EntityManager em = DBUtils.getEntityManager();
    try {
      return em.createQuery(
                   "SELECT f FROM FuenteDeAgregacion f JOIN f.fuentesCargadas hija WHERE hija.id = :hijoId",
                   FuenteDeAgregacion.class)
               .setParameter("hijoId", hijoId)
               .getResultList();
    } finally {
      em.close();
    }
  }

  public List<Coleccion> findColeccionesIndirectas(Long fuenteId) {
    EntityManager em = DBUtils.getEntityManager();
    try {
      return em.createQuery(
                   "SELECT DISTINCT c FROM Coleccion c " +
                       "WHERE c.coleccion_fuente.id IN " +
                       "(SELECT f.id FROM FuenteDeAgregacion f JOIN f.fuentesCargadas hija WHERE hija.id = :fuenteId)",
                   Coleccion.class)
               .setParameter("fuenteId", fuenteId)
               .getResultList();
    } finally {
      em.close();
    }
  }
}
package ar.edu.utn.frba.dds.repositories;

import ar.edu.utn.frba.dds.model.coleccion.Coleccion;
import ar.edu.utn.frba.dds.model.fuentes.Fuente;
import ar.edu.utn.frba.dds.model.fuentes.FuenteConHechos;
import ar.edu.utn.frba.dds.model.fuentes.FuenteDeAgregacion;
import ar.edu.utn.frba.dds.model.fuentes.FuenteDeCopiaLocal;
import ar.edu.utn.frba.dds.model.fuentes.FuenteDinamica;
import ar.edu.utn.frba.dds.model.fuentes.FuenteEstatica;
import ar.edu.utn.frba.dds.model.fuentes.FuenteExternaAPI;
import ar.edu.utn.frba.dds.utils.DBUtils;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceException;

public class FuenteRepository {
  private static final FuenteRepository INSTANCE = new FuenteRepository();

  public static FuenteRepository instance() {
    return INSTANCE;
  }

  public void save(Fuente fuente) {
    EntityManager em = DBUtils.getEntityManager();

    // Enriquecer hechos antes de guardar
    if (fuente instanceof FuenteConHechos) {
      ((FuenteConHechos) fuente).getHechos().forEach(DBUtils::enriquecerHecho);
    }

    DBUtils.comenzarTransaccion(em);
    try {
      if (fuente.getId() == null) {
        em.persist(fuente);
        em.flush();
      } else {
        fuente = em.merge(fuente);
      }
      DBUtils.commit(em);
    } catch (PersistenceException e) {
      DBUtils.rollback(em);
      throw new RuntimeException("Error al guardar fuente: " + e.getMessage());
    } finally {
      em.close();
    }
  }

  public Fuente findByName(String nombre) {
    EntityManager em = DBUtils.getEntityManager();
    try {
      return em.createQuery(
                   "SELECT DISTINCT f FROM Fuente f where f.fuente_nombre = :nombre",
                   Fuente.class
               )
               .setParameter("nombre", nombre)
               .getSingleResult();
    } catch (NoResultException e) {
      return null;
    } finally {
      em.close();
    }
  }

  public void update(Fuente fuente) {
    EntityManager em = DBUtils.getEntityManager();
    DBUtils.comenzarTransaccion(em);
    try {
      Fuente managedFuente = em.merge(fuente);

      if (managedFuente instanceof FuenteConHechos) {
        ((FuenteConHechos) managedFuente).getHechos().forEach(DBUtils::enriquecerHecho);
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
                   "SELECT DISTINCT f FROM Fuente f",
                   Fuente.class)
               .getResultList();
    } finally {
      em.close();
    }
  }

  public Fuente findById(Long id) {
    EntityManager em = DBUtils.getEntityManager();
    try {
      Fuente fuente = em.find(Fuente.class, id);

      if (fuente != null) {
        // Cargar hechos de forma eager
        cargarHechosParaFuente(em, fuente);
      }

      return fuente;
    } finally {
      em.close();
    }
  }

  public Fuente findByIdConHechos(Long id) {
    EntityManager em = DBUtils.getEntityManager();
    try {
      Fuente fuente = em.find(Fuente.class, id);

      if (fuente != null) {
        cargarHechosParaFuente(em, fuente);
      }

      return fuente;
    } finally {
      em.close();
    }
  }

  /**
   * Carga los hechos para cualquier tipo de fuente de forma eager.
   */
  private void cargarHechosParaFuente(EntityManager em, Fuente fuente) {
    if (fuente == null) {
      return;
    }

    try {
      // Para FuenteDeAgregacion, cargar las fuentes hijas
      if (fuente instanceof FuenteDeAgregacion) {
        FuenteDeAgregacion fuenteAgregacion = em.createQuery(
                                                    "SELECT f FROM FuenteDeAgregacion f " +
                                                        "LEFT JOIN FETCH f.fuentesCargadas " +
                                                        "WHERE f.fuente_id = :id",
                                                    FuenteDeAgregacion.class)
                                                .setParameter("id", fuente.getId())
                                                .getSingleResult();

        ((FuenteDeAgregacion) fuente).setFuentesCargadas(
            fuenteAgregacion.getFuentesCargadas()
        );
        return;
      }

      // Para FuenteDinamica
      if (fuente instanceof FuenteDinamica) {
        FuenteDinamica fuenteDinamica = em.createQuery(
                                              "SELECT f FROM FuenteDinamica f " +
                                                  "LEFT JOIN FETCH f.hechosPersistidos " +
                                                  "WHERE f.fuente_id = :id",
                                              FuenteDinamica.class)
                                          .setParameter("id", fuente.getId())
                                          .getSingleResult();

        ((FuenteDinamica) fuente).setHechosPersistidos(
            new ArrayList<>(fuenteDinamica.getHechos())
        );
      }
      // Para FuenteEstatica
      else if (fuente instanceof FuenteEstatica) {
        FuenteEstatica fuenteEstatica = em.createQuery(
                                              "SELECT f FROM FuenteEstatica f " +
                                                  "LEFT JOIN FETCH f.hechosPersistidos " +
                                                  "WHERE f.fuente_id = :id",
                                              FuenteEstatica.class)
                                          .setParameter("id", fuente.getId())
                                          .getSingleResult();

        ((FuenteEstatica) fuente).setHechosPersistidos(
            new ArrayList<>(fuenteEstatica.getHechos())
        );
      }
      // Para FuenteExternaAPI
      else if (fuente instanceof FuenteExternaAPI) {
        FuenteExternaAPI fuenteAPI = em.createQuery(
                                           "SELECT f FROM FuenteExternaAPI f " +
                                               "LEFT JOIN FETCH f.copiaLocalDeHechos " +
                                               "WHERE f.fuente_id = :id",
                                           FuenteExternaAPI.class)
                                       .setParameter("id", fuente.getId())
                                       .getSingleResult();

        ((FuenteExternaAPI) fuente).setCopiaLocalDeHechos(
            new ArrayList<>(fuenteAPI.getHechos())
        );
      }
      // Para FuenteDeCopiaLocal genérica
      else if (fuente instanceof FuenteDeCopiaLocal) {
        FuenteDeCopiaLocal fuenteCopia = em.createQuery(
                                               "SELECT f FROM FuenteDeCopiaLocal f " +
                                                   "LEFT JOIN FETCH f.copiaLocalDeHechos " +
                                                   "WHERE f.fuente_id = :id",
                                               FuenteDeCopiaLocal.class)
                                           .setParameter("id", fuente.getId())
                                           .getSingleResult();

        ((FuenteDeCopiaLocal) fuente).setCopiaLocalDeHechos(
            new ArrayList<>(fuenteCopia.getHechos())
        );
      }
      // Para FuenteConHechos genérica
      else if (fuente instanceof FuenteConHechos) {
        FuenteConHechos fuenteConHechos = em.createQuery(
                                                "SELECT f FROM FuenteConHechos f " +
                                                    "LEFT JOIN FETCH f.hechos " +
                                                    "WHERE f.fuente_id = :id",
                                                FuenteConHechos.class)
                                            .setParameter("id", fuente.getId())
                                            .getSingleResult();

        ((FuenteConHechos) fuente).setHechos(
            new ArrayList<>(fuenteConHechos.getHechos())
        );
      }
    } catch (Exception e) {
      System.err.println("Advertencia: No se pudieron cargar hechos para fuente " +
                             fuente.getNombre() + " (ID: " + fuente.getId() + "): " + e.getMessage());
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
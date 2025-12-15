package ar.edu.utn.frba.dds.repositories;

import ar.edu.utn.frba.dds.model.coleccion.Coleccion;
import ar.edu.utn.frba.dds.model.fuentes.Fuente;
import ar.edu.utn.frba.dds.model.fuentes.FuenteConHechos;
import ar.edu.utn.frba.dds.model.fuentes.FuenteDeCopiaLocal;
import ar.edu.utn.frba.dds.model.fuentes.FuenteDeAgregacion;
import ar.edu.utn.frba.dds.model.fuentes.FuenteDinamica;
import ar.edu.utn.frba.dds.model.fuentes.FuenteEstatica;
import ar.edu.utn.frba.dds.model.fuentes.FuenteExternaAPI;
import ar.edu.utn.frba.dds.model.hecho.Hecho;
import ar.edu.utn.frba.dds.utils.DBUtils;
import java.util.ArrayList;
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
      return em.createQuery("SELECT c FROM Coleccion c", Coleccion.class)
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
      Coleccion coleccion = em.createQuery(
                                  "SELECT DISTINCT c FROM Coleccion c " +
                                      "LEFT JOIN FETCH c.coleccion_fuente " +
                                      "LEFT JOIN FETCH c.algoritmoDeConsenso " +
                                      "LEFT JOIN FETCH c.condicion " +
                                      "WHERE c.coleccion_id = :id", Coleccion.class)
                              .setParameter("id", id)
                              .getSingleResult();

      if (coleccion.getFuente() != null) {
        cargarHechosParaFuente(em, coleccion.getFuente());
      }

      return coleccion;
    } catch (NoResultException e) {
      return null;
    } finally {
      em.close();
    }
  }

  /**
   * Carga los hechos para una fuente utilizando fetch explícito en lugar de lazy loading.
   * Esto evita problemas de LazyInitializationException.
   */
  private void cargarHechosParaFuente(EntityManager em, Fuente fuente) {
    if (fuente == null) {
      return;
    }

    try {
      // Para FuenteDeAgregacion, no necesitamos cargar hechos directamente
      // porque los obtiene de sus fuentes hijas
      if (fuente instanceof FuenteDeAgregacion) {
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
      // Si falla la carga de hechos, registrar el error pero no detener el proceso
      System.err.println("Advertencia: No se pudieron cargar hechos para fuente " +
                             fuente.getNombre() + " (ID: " + fuente.getId() + "): " + e.getMessage());
    }
  }

  public List<Coleccion> findAllConFuentesYHechos() {
    EntityManager em = DBUtils.getEntityManager();
    try {
      List<Coleccion> colecciones = em.createQuery(
                                          "SELECT DISTINCT c FROM Coleccion c " +
                                              "LEFT JOIN FETCH c.coleccion_fuente f " +
                                              "WHERE f IS NOT NULL", Coleccion.class)
                                      .getResultList();

      for (Coleccion coleccion : colecciones) {
        if (coleccion.getFuente() != null) {
          cargarHechosParaFuente(em, coleccion.getFuente());
        }
      }

      return colecciones;
    } finally {
      em.close();
    }
  }

  public List<Coleccion> findByFuenteId(Long fuenteId) {
    EntityManager em = DBUtils.getEntityManager();
    try {
      return em.createQuery(
                   "SELECT c FROM Coleccion c WHERE c.coleccion_fuente.id = :fuenteId",
                   Coleccion.class)
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
      return em.createQuery(
                   "SELECT COUNT(DISTINCT c.coleccion_id) FROM Coleccion c",
                   Long.class)
               .getSingleResult();
    } finally {
      em.close();
    }
  }

  public List<String> getCategorias() {
    EntityManager em = DBUtils.getEntityManager();
    try {
      return em.createQuery(
                   "SELECT DISTINCT c.coleccion_categoria FROM Coleccion c",
                   String.class)
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
                   Coleccion.class)
               .getResultList();
    } finally {
      em.close();
    }
  }

  public Coleccion findByIdConHechosInicializados(Long id) {
    EntityManager em = DBUtils.getEntityManager();
    try {
      Coleccion coleccion = em.createQuery(
                                  "SELECT DISTINCT c FROM Coleccion c " +
                                      "LEFT JOIN FETCH c.coleccion_fuente f " +
                                      "WHERE c.coleccion_id = :id", Coleccion.class)
                              .setParameter("id", id)
                              .getSingleResult();

      if (coleccion != null && coleccion.getFuente() != null) {
        cargarHechosParaFuente(em, coleccion.getFuente());
      }

      return coleccion;
    } catch (NoResultException e) {
      return null;
    } finally {
      em.close();
    }
  }
}
package ar.edu.utn.frba.dds.repositories;

import ar.edu.utn.frba.dds.model.coleccion.Coleccion;
import ar.edu.utn.frba.dds.model.fuentes.Fuente;
import ar.edu.utn.frba.dds.model.fuentes.FuenteConHechos;
import ar.edu.utn.frba.dds.model.fuentes.FuenteDeAgregacion;
import ar.edu.utn.frba.dds.model.fuentes.FuenteDinamica;
import ar.edu.utn.frba.dds.model.fuentes.FuenteEstatica;
import ar.edu.utn.frba.dds.model.fuentes.FuenteExternaAPI;
import ar.edu.utn.frba.dds.utils.DBUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
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

    // Enriquecer hechos antes de guardar si corresponde
    if (fuente instanceof FuenteConHechos) {
      ((FuenteConHechos) fuente).getHechos().forEach(DBUtils::enriquecerHecho);
    }

    DBUtils.comenzarTransaccion(em);
    try {
      if (fuente.getId() == null) {
        em.persist(fuente);
      } else {
        fuente = em.merge(fuente);
      }
      em.flush();
      DBUtils.commit(em);
    } catch (PersistenceException e) {
      DBUtils.rollback(em);
      throw new RuntimeException("Error al guardar fuente: " + e.getMessage(), e);
    } finally {
      em.close();
    }
  }

  public Fuente findByName(String nombre) {
    EntityManager em = DBUtils.getEntityManager();
    try {
      Fuente fuente = em.createQuery(
                            "SELECT DISTINCT f FROM Fuente f where f.fuente_nombre = :nombre",
                            Fuente.class
                        )
                        .setParameter("nombre", nombre)
                        .getSingleResult();

      if (fuente != null) {
        cargarDependenciasLazy(em, fuente);
      }

      return fuente;

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
      throw new RuntimeException("Error al actualizar fuente: " + e.getMessage(), e);
    } finally {
      em.close();
    }
  }

  public List<Fuente> findAll() {
    EntityManager em = DBUtils.getEntityManager();
    try {
      return em.createQuery("SELECT DISTINCT f FROM Fuente f", Fuente.class)
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
        cargarDependenciasLazy(em, fuente);
      }

      return fuente;
    } finally {
      em.close();
    }
  }

  public Fuente findByIdConHechos(Long id) {
    return findById(id);
  }

  private void cargarDependenciasLazy(EntityManager em, Fuente fuente) {
    if (fuente == null) {
      return;
    }

    try {
      if (fuente instanceof FuenteDeAgregacion) {
        FuenteDeAgregacion agregacion = em.createQuery(
                                              "SELECT DISTINCT f FROM FuenteDeAgregacion f " +
                                                  "LEFT JOIN FETCH f.fuentesCargadas " +
                                                  "WHERE f.id = :id", FuenteDeAgregacion.class)
                                          .setParameter("id", fuente.getId())
                                          .getSingleResult();

        ((FuenteDeAgregacion) fuente).setFuentesCargadas(agregacion.getFuentesCargadas());
      } else if (fuente instanceof FuenteConHechos) {
        FuenteConHechos conHechos = em.createQuery(
                                          "SELECT DISTINCT f FROM FuenteConHechos f " +
                                              "LEFT JOIN FETCH f.hechos " +
                                              "WHERE f.id = :id", FuenteConHechos.class)
                                      .setParameter("id", fuente.getId())
                                      .getSingleResult();

        ((FuenteConHechos) fuente).getHechos();
      }

    } catch (Exception e) {
      System.err.println("Advertencia: No se pudieron cargar dependencias lazy para fuente " +
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
    // 1. Buscar colecciones que usan esta fuente directamente
    List<Coleccion> coleccionesDirectas = em.createQuery(
                                                "SELECT c FROM Coleccion c WHERE c.coleccion_fuente.id = :id",
                                                Coleccion.class)
                                            .setParameter("id", fuente.getId())
                                            .getResultList();

    // 2. FIX: Borrar estadísticas que referencian a estas colecciones ANTES de borrar las colecciones
    if (!coleccionesDirectas.isEmpty()) {
      List<Long> idsColecciones = coleccionesDirectas.stream()
                                                     .map(Coleccion::getId)
                                                     .collect(Collectors.toList());

      // Ejecutar borrado masivo de estadísticas para evitar Constraint Violation
      em.createQuery("DELETE FROM Estadistica e WHERE e.estadistica_coleccion.id IN :ids")
        .setParameter("ids", idsColecciones)
        .executeUpdate();
    }

    // 3. Ahora sí, borrar las colecciones
    for (Coleccion c : coleccionesDirectas) {
      Coleccion managedCol = em.contains(c) ? c : em.merge(c);
      em.remove(managedCol);
    }

    // 4. Buscar fuentes de agregación que contienen esta fuente como hija
    List<FuenteDeAgregacion> padres = em.createQuery(
                                            "SELECT DISTINCT f FROM FuenteDeAgregacion f JOIN f.fuentesCargadas hija WHERE hija.id = :hijoId",
                                            FuenteDeAgregacion.class)
                                        .setParameter("hijoId", fuente.getId())
                                        .getResultList();

    for (FuenteDeAgregacion padre : padres) {
      padre = em.contains(padre) ? padre : em.merge(padre);
      padre.removerFuente(fuente);
      em.merge(padre);

      if (padre.cantidadFuentes() == 0) {
        eliminarRecursivamente(padre, em);
      }
    }

    // 5. Finalmente, eliminar la fuente
    Fuente managed = em.contains(fuente) ? fuente : em.merge(fuente);
    em.remove(managed);
  }

  public List<FuenteDeAgregacion> findAgregacionesByHija(Long hijoId) {
    EntityManager em = DBUtils.getEntityManager();
    try {
      return em.createQuery(
                   "SELECT DISTINCT f FROM FuenteDeAgregacion f JOIN f.fuentesCargadas hija WHERE hija.id = :hijoId",
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

  public List<Fuente> findByTipo(String tipoDiscriminador) {
    EntityManager em = DBUtils.getEntityManager();
    try {
      return em.createQuery(
                   "SELECT f FROM Fuente f WHERE TYPE(f) = :tipo",
                   Fuente.class)
               .getResultList();
    } catch (IllegalArgumentException e) {
      return findAll().stream()
                      .filter(f -> f.getTipo().equalsIgnoreCase(tipoDiscriminador))
                      .collect(Collectors.toList());
    } finally {
      em.close();
    }
  }

  public List<Fuente> findByHechoId(Long hechoId) {
    EntityManager em = DBUtils.getEntityManager();
    try {
      return em.createQuery(
                   "SELECT DISTINCT f FROM FuenteConHechos f JOIN f.hechos h WHERE h.id = :hechoId",
                   Fuente.class)
               .setParameter("hechoId", hechoId)
               .getResultList();
    } finally {
      em.close();
    }
  }

  public Fuente findFuenteByNombre(String nombreFuente) {
    EntityManager em = DBUtils.getEntityManager();
    try {
      return em.createQuery(
                   "SELECT f FROM Fuente f WHERE f.fuente_nombre = :nombre",
                   Fuente.class)
               .setParameter("nombre", nombreFuente)
               .getSingleResult();
    } catch (NoResultException e) {
      return null;
    } finally {
      em.close();
    }
  }
}
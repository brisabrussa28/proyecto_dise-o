package ar.edu.utn.frba.dds.repositories;

import ar.edu.utn.frba.dds.model.coleccion.Coleccion;
import ar.edu.utn.frba.dds.model.fuentes.Fuente;
import ar.edu.utn.frba.dds.model.fuentes.FuenteDeAgregacion;
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
    fuente.getHechos()
          .forEach(DBUtils::enriquecerHecho);
    DBUtils.comenzarTransaccion(em);
    try {
      em.persist(fuente);
      DBUtils.commit(em);
    } catch (PersistenceException e) {
      DBUtils.rollback(em);
      throw new RuntimeException(e.getMessage());
    } finally {
      em.close();
    }
  }

  public void update(Fuente fuente) {
    EntityManager em = DBUtils.getEntityManager();
    DBUtils.comenzarTransaccion(em);
    try {
      em.merge(fuente);
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
    DBUtils.comenzarTransaccion(em);
    try {
      return em.createQuery("SELECT f FROM Fuente f", Fuente.class)
               .getResultList();
    } finally {
      em.close();
    }
  }

  public Fuente findById(Long id) {
    EntityManager em = DBUtils.getEntityManager();
    try {
      return em.find(Fuente.class, id);
    } finally {
      em.close();
    }
  }

  /**
   * Elimina una fuente y maneja todas sus dependencias de forma recursiva.
   * Proceso:
   * 1. Elimina colecciones que dependen directamente de esta fuente
   * 2. Desvincula la fuente de sus agregaciones padre
   * 3. Elimina recursivamente agregaciones padre que quedan vacías
   * 4. Elimina la fuente
   */
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

  /**
   * Lógica recursiva de eliminación que maneja todas las dependencias.
   * Este método debe ejecutarse dentro de una transacción activa.
   */
  private void eliminarRecursivamente(Fuente fuente, EntityManager em) {
    // Paso 1: Eliminar Colecciones dependientes directas
    List<Coleccion> coleccionesDirectas = em.createQuery(
                                                "SELECT c FROM Coleccion c WHERE c.coleccion_fuente.id = :id",
                                                Coleccion.class
                                            )
                                            .setParameter("id", fuente.getId())
                                            .getResultList();

    for (Coleccion c : coleccionesDirectas) {
      Coleccion managedCol = em.contains(c) ? c : em.merge(c);
      em.remove(managedCol);
    }

    // Paso 2: Desvincular de Agregaciones padre
    List<FuenteDeAgregacion> padres = em.createQuery(
                                            "SELECT f FROM FuenteDeAgregacion f JOIN f.fuentesCargadas hija WHERE hija.id = :hijoId",
                                            FuenteDeAgregacion.class
                                        )
                                        .setParameter("hijoId", fuente.getId())
                                        .getResultList();

    for (FuenteDeAgregacion padre : padres) {
      // Refrescar el padre para tener el estado más reciente
      em.refresh(padre);

      // Remover la fuente hija
      padre.removerFuente(fuente);
      em.merge(padre);

      // Paso 3: Si el padre quedó vacío, eliminarlo recursivamente
      if (padre.getFuentesCargadas()
               .isEmpty()) {
        eliminarRecursivamente(padre, em);
      }
    }

    // Paso 4: Eliminar la fuente en sí
    Fuente managed = em.contains(fuente) ? fuente : em.merge(fuente);
    em.remove(managed);
  }

  /**
   * Buscar Agregaciones que contienen una fuente hija.
   * Útil para mostrar dependencias en la vista.
   */
  public List<FuenteDeAgregacion> findAgregacionesByHija(Long hijoId) {
    EntityManager em = DBUtils.getEntityManager();
    try {
      return em.createQuery(
                   "SELECT f FROM FuenteDeAgregacion f JOIN f.fuentesCargadas hija WHERE hija.id = :hijoId",
                   FuenteDeAgregacion.class
               )
               .setParameter("hijoId", hijoId)
               .getResultList();
    } finally {
      em.close();
    }
  }

  /**
   * Obtener colecciones indirectas (a través de agregaciones padre).
   * Útil para visualizar el impacto completo del borrado.
   */
  public List<Coleccion> findColeccionesIndirectas(Long fuenteId) {
    EntityManager em = DBUtils.getEntityManager();
    try {
      return em.createQuery(
                   "SELECT DISTINCT c FROM Coleccion c " +
                       "WHERE c.coleccion_fuente.id IN " +
                       "(SELECT f.id FROM FuenteDeAgregacion f JOIN f.fuentesCargadas hija WHERE hija.id = :fuenteId)",
                   Coleccion.class
               )
               .setParameter("fuenteId", fuenteId)
               .getResultList();
    } finally {
      em.close();
    }
  }
}

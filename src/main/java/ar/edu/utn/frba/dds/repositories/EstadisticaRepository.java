package ar.edu.utn.frba.dds.repositories;

import ar.edu.utn.frba.dds.model.estadisticas.Estadistica;
import ar.edu.utn.frba.dds.utils.DBUtils;

import javax.persistence.EntityManager;
import java.util.List;

public class EstadisticaRepository {

  private static final EstadisticaRepository INSTANCE = new EstadisticaRepository();

  public static EstadisticaRepository instance() {
    return INSTANCE;
  }

  private Estadistica findExisting(EntityManager em, Estadistica stat) {

    Long coleccionId = stat.getColeccion() != null
                       ? stat.getColeccion().getId()
                       : null;

    return em.createQuery(
                 "SELECT e FROM Estadistica e " +
                     "WHERE e.estadistica_tipo = :tipo " +
                     "AND ((:grupo IS NULL AND e.estadistica_grupo IS NULL) OR e.estadistica_grupo = :grupo) " +
                     "AND ((:categoria IS NULL AND e.estadistica_categoria IS NULL) OR e.estadistica_categoria = :categoria) " +
                     "AND ((:coleccion IS NULL AND e.estadistica_coleccion IS NULL) OR e.estadistica_coleccion.id = :coleccion)",
                 Estadistica.class
             )
             .setParameter("tipo", stat.getTipo())
             .setParameter("grupo", stat.getGrupo())
             .setParameter("categoria", stat.getCategoria())
             .setParameter("coleccion", coleccionId)
             .getResultStream()
             .findFirst()
             .orElse(null);
  }

  public void save(Estadistica estadistica) {
    EntityManager em = DBUtils.getEntityManager();

    try {
      DBUtils.comenzarTransaccion(em);

      Estadistica existing = findExisting(em, estadistica);

      if (existing != null) {
        estadistica.setId(existing.getId());
        em.merge(estadistica);
      } else {
        em.persist(estadistica);
      }

      DBUtils.commit(em);

    } catch (Exception e) {
      DBUtils.rollback(em);
      throw new RuntimeException("Error guardando estadística", e);

    } finally {
      em.close();
    }
  }

  public List<Estadistica> findAll() {
    EntityManager em = DBUtils.getEntityManager();
    List<Estadistica> result = em.createQuery("SELECT e FROM Estadistica e", Estadistica.class)
                                 .getResultList();
    em.close();
    return result;
  }

  public Estadistica findById(Long id) {
    EntityManager em = DBUtils.getEntityManager();
    Estadistica e = em.find(Estadistica.class, id);
    em.close();
    return e;
  }

  public Estadistica findByTipo(String tipo) {
    EntityManager em = DBUtils.getEntityManager();
    Estadistica e = em.createQuery(
                          "SELECT e FROM Estadistica e WHERE e.estadistica_tipo = :tipo",
                          Estadistica.class
                      )
                      .setParameter("tipo", tipo)
                      .getResultStream()
                      .findFirst()
                      .orElse(null);

    em.close();
    return e;
  }

  public void deleteAll() {
    EntityManager em = DBUtils.getEntityManager();

    try {
      DBUtils.comenzarTransaccion(em);
      em.createQuery("DELETE FROM Estadistica").executeUpdate();
      DBUtils.commit(em);
    } catch (Exception e) {
      DBUtils.rollback(em);
      throw new RuntimeException("Error al borrar estadísticas", e);
    } finally {
      em.close();
    }
  }
}
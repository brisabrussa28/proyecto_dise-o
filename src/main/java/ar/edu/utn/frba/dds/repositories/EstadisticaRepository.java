package ar.edu.utn.frba.dds.repositories;

import ar.edu.utn.frba.dds.model.estadisticas.Estadistica;
import ar.edu.utn.frba.dds.utils.DBUtils;
import java.util.List;
import javax.persistence.EntityManager;

public class EstadisticaRepository {
  EntityManager em = DBUtils.getEntityManager();

  private static final EstadisticaRepository INSTANCE = new EstadisticaRepository();

  public static EstadisticaRepository instance() {
    return INSTANCE;
  }

  public void save(Estadistica estadistica) {
    if (estadistica != null) {
      DBUtils.comenzarTransaccion(em);
      em.persist(estadistica);
      DBUtils.commit(em);
    }
  }

  public List<Estadistica> findAll() {
    return em.createQuery("SELECT * FROM estadistica", Estadistica.class)
             .getResultList();

  }

  public Estadistica findById(Long id) {
    return em.createQuery("SELECT * FROM estadistica WHERE estadistica_id = id", Estadistica.class)
             .setParameter("id", id)
             .getSingleResult();
  }
}

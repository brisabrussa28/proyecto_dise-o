package ar.edu.utn.frba.dds.domain.repos;

import ar.edu.utn.frba.dds.domain.estadisticas.Estadistica;
import ar.edu.utn.frba.dds.domain.utils.DBUtils;
import java.util.List;
import javax.persistence.EntityManager;

public class EstadisticaRepository {
  EntityManager em = DBUtils.getEntityManager();

  public EstadisticaRepository() {
  }

  public void save(Estadistica estadistica) {
    DBUtils.comenzarTransaccion(em);
    em.persist(estadistica);
    DBUtils.commit(em);
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

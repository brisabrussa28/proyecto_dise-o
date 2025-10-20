package ar.edu.utn.frba.dds.repositories;

import ar.edu.utn.frba.dds.model.estadisticas.Estadistica;
import ar.edu.utn.frba.dds.utils.DBUtils;
import java.util.List;
import javax.persistence.EntityManager;

public class EstadisticaRepository {
  private final EntityManager em = DBUtils.getEntityManager();

  public void save(Estadistica estadistica) {
    if (estadistica != null) {
      DBUtils.comenzarTransaccion(em);
      em.persist(estadistica);
      DBUtils.commit(em);
    }
  }

  public List<Estadistica> findAll() {
    return em.createQuery("SELECT * FROM Estadistica", Estadistica.class)
             .getResultList();

  }

  public Estadistica findById(Long id) {
    return em.find(Estadistica.class, id);
  }
}

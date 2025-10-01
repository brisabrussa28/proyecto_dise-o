package ar.edu.utn.frba.dds.domain.repos;

import ar.edu.utn.frba.dds.domain.fuentes.Fuente;
import javax.persistence.EntityManager;
import org.apache.commons.dbcp2.BasicDataSource;

public class FuentesRepository {

  private BasicDataSource dataSource;
  private EntityManager em;

  public FuentesRepository(BasicDataSource dataSource, EntityManager em) {
    this.em = em;
    this.dataSource = dataSource;
  }

  public void agregarFuente(Fuente fuente) {
    em.getTransaction()
      .begin();
    em.persist(fuente);
    em.getTransaction()
      .commit();
  }
}

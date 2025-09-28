package ar.edu.utn.frba.dds.domain.repo;

import ar.edu.utn.frba.dds.domain.fuentes.Fuente;
import javax.persistence.EntityManager;
import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;

public class FuentesRepository {

  private BasicDataSource dataSource;

  public FuentesRepository(BasicDataSource dataSource) {
    this.dataSource = dataSource;
  }

  public void agregarFuente(Fuente fuente, EntityManager em) {
//    try {
//      QueryRunner run = new QueryRunner(this.dataSource);
//      String sql = "INSERT INTO fuente (tipo_fuente,  nombre) VALUES (?, ?)";
//      String tipo = (fuente instanceof FuenteDinamica) ?
//                    "DINAMICA" :
//                    fuente.getClass()
//                          .getSimpleName();
//      run.update(sql, tipo, fuente.getNombre());
//    } catch (Exception e) {
//      throw new RuntimeException(e);
//    }
    em.getTransaction()
      .begin();
    em.persist(fuente);
    em.getTransaction()
      .commit();
  }

  private <T> T doQuery(String sql, ResultSetHandler<T> handler) {
    try {
      QueryRunner run = new QueryRunner(this.dataSource);
      return run.query(sql, handler);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}

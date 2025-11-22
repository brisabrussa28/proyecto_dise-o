package ar.edu.utn.frba.dds.repositories;

import ar.edu.utn.frba.dds.model.fuentes.Fuente;
import ar.edu.utn.frba.dds.utils.DBUtils;
import java.util.List;
import javax.persistence.EntityManager;

public class FuenteRepository {
  private final EntityManager em = DBUtils.getEntityManager();

  private static final FuenteRepository INSTANCE = new FuenteRepository();

  public static FuenteRepository instance() {
    return INSTANCE;
  }

  public void save(Fuente fuente) {
    fuente.getHechos()
          .forEach(hecho -> {
            DBUtils.enriquecerHecho(hecho);
          });
    DBUtils.comenzarTransaccion(em);
    em.persist(fuente);
    DBUtils.commit(em);
  }

  public List<Fuente> findAll() {
    return em.createQuery("SELECT f FROM Fuente f", Fuente.class)
             .getResultList();

  }

  public Fuente findById(Long id) {
    return em.find(Fuente.class, id);
  }

}

package ar.edu.utn.frba.dds.domain.repos;

import ar.edu.utn.frba.dds.domain.coleccion.Coleccion;
import ar.edu.utn.frba.dds.domain.fuentes.Fuente;
import ar.edu.utn.frba.dds.domain.utils.DBUtils;
import java.util.List;
import javax.persistence.EntityManager;

public class FuenteRepository {

  private EntityManager em = DBUtils.getEntityManager();

  public FuenteRepository() {
  }

  public void save(Fuente fuente) {
    DBUtils.comenzarTransaccion(em);
    fuente.getHechos()
          .forEach(hecho -> {
            DBUtils.completarUbicacionFaltante(hecho);
            DBUtils.completarProvinciaFaltante(hecho);
          });
    em.persist(fuente);
    DBUtils.commit(em);
  }

  public List<Coleccion> findAll() {
    return em.createQuery("SELECT * FROM fuente", Coleccion.class)
             .getResultList();

  }

  public Coleccion getById(Long id) {
    return em.createQuery("SELECT * FROM fuente WHERE fuente_id = id", Coleccion.class)
             .setParameter("id", id)
             .getSingleResult();
  }

}

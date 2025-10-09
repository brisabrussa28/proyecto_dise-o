package ar.edu.utn.frba.dds.domain.repos;

import ar.edu.utn.frba.dds.domain.hecho.Hecho;
import ar.edu.utn.frba.dds.domain.hecho.etiqueta.Etiqueta;
import ar.edu.utn.frba.dds.domain.utils.DBUtils;
import java.util.List;
import javax.persistence.EntityManager;

public class EtiquetaRepository {
  EntityManager em = DBUtils.getEntityManager();

  public EtiquetaRepository() {
  }

  public void save(Etiqueta etiqueta) {
    DBUtils.comenzarTransaccion(em);
    em.persist(etiqueta);
    DBUtils.commit(em);
  }

  public List<Hecho> findAll() {
    return em.createQuery("SELECT hecho_etiqueta FROM hecho", Hecho.class)
             .getResultList();

  }

  public Hecho getById(Long id) {
    return em.createQuery("SELECT hecho_etiqueta FROM hecho WHERE hecho_etiqueta = id", Hecho.class)
             .setParameter("id", id)
             .getSingleResult();
  }
}

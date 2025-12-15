package ar.edu.utn.frba.dds.repositories;

import ar.edu.utn.frba.dds.model.hecho.Hecho;
import ar.edu.utn.frba.dds.utils.DBUtils;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceException;
import javax.persistence.Query;

public class HechoRepository {

  private static final HechoRepository INSTANCE = new HechoRepository();

  public static HechoRepository instance() {
    return INSTANCE;
  }

  public void save(Hecho hecho) {
    EntityManager em = DBUtils.getEntityManager();
    DBUtils.comenzarTransaccion(em);
    try {
      DBUtils.enriquecerHecho(hecho);

      if (hecho.getId() == null) {
        em.persist(hecho);
      } else {
        em.merge(hecho);
      }
      DBUtils.commit(em);
    } catch (PersistenceException e) {
      DBUtils.rollback(em);
      throw new RuntimeException("Error al guardar hecho: " + e.getMessage());
    } finally {
      em.close();
    }
  }

  public List<Hecho> findAll() {
    EntityManager em = DBUtils.getEntityManager();
    try {
      return em.createQuery(
                   "SELECT DISTINCT h FROM Hecho h " +
                       "LEFT JOIN FETCH h.fotos " +
                       "LEFT JOIN FETCH h.etiquetas",
                   Hecho.class)
               .getResultList();
    } finally {
      em.close();
    }
  }

  public Optional<Hecho> findAny() {
    return this.findAll()
               .stream()
               .findAny();
  }

  public Hecho findById(Long id) {
    EntityManager em = DBUtils.getEntityManager();
    try {
      return em.createQuery(
                   "SELECT h FROM Hecho h " +
                       "LEFT JOIN FETCH h.fotos " +
                       "LEFT JOIN FETCH h.etiquetas " +
                       "WHERE h.id = :id", Hecho.class)
               .setParameter("id", id)
               .getSingleResult();
    } finally {
      em.close();
    }
  }

  /*
  public List<Hecho> findByTitle(String titulo) {
    EntityManager em = DBUtils.getEntityManager();

    TypedQuery<Hecho> q = em.createQuery(
        "SELECT h FROM Hecho h LEFT JOIN FETCH h.fotos WHERE LOWER(h.hecho_titulo) LIKE LOWER(:queryParam) OR LOWER(h.hecho_descripcion) LIKE LOWER(:queryParam)",
        Hecho.class
    );

    // Envolvemos el parámetro en "%" para que coincida con cualquier parte del texto
    q.setParameter("queryParam", "%" + titulo + "%");

    return q.getResultList();
  }*/

  public List<Hecho> findByTitle(String titulo) {
    EntityManager em = DBUtils.getEntityManager();

    int UMBRAL_TOLERANCIA = 3;

    String sql = "SELECT * FROM Hecho h " +
        "WHERE levenshtein(LOWER(h.hecho_titulo), LOWER(:queryParam)) <= :umbral " +
        "   OR LOWER(h.hecho_descripcion) LIKE LOWER(:queryParamLike)";

    try {
      Query q = em.createNativeQuery(sql, Hecho.class);

      q.setParameter("queryParam", titulo.trim());
      q.setParameter("umbral", UMBRAL_TOLERANCIA);
      q.setParameter("queryParamLike", "%" + titulo.trim() + "%");

      return q.getResultList();

    } catch (Exception e) {
      e.printStackTrace();
      return Collections.emptyList();
    }
  }

  public List<String> getCategorias() {
    EntityManager em = DBUtils.getEntityManager();
    try {
      return em.createQuery(
                   "SELECT DISTINCT h.hecho_categoria FROM Hecho h", String.class)
               .getResultList();
    } finally {
      em.close();
    }
  }

  public List<String> getEtiquetas() {
    EntityManager em = DBUtils.getEntityManager();
    try {
      return em.createNativeQuery(
                   "SELECT DISTINCT etiqueta_nombre FROM hecho_etiquetas")
               .getResultList();
    } finally {
      em.close();
    }
  }

  public Long countAll() {
    EntityManager em = DBUtils.getEntityManager();
    try {
      return em.createQuery(
                   "SELECT COUNT(DISTINCT h.id) FROM Hecho h", Long.class)
               .getSingleResult();
    } finally {
      em.close();
    }
  }
}
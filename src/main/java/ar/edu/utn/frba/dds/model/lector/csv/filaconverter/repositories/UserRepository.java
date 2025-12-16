package ar.edu.utn.frba.dds.model.lector.csv.filaconverter.repositories;

import ar.edu.utn.frba.dds.model.lector.csv.filaconverter.utils.DBUtils;
import ar.edu.utn.frba.dds.model.usuario.Usuario;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceException;
import org.hibernate.exception.ConstraintViolationException;

public class UserRepository {
  // BORRADO: private final EntityManager em = ... (¡ESTO ERA EL ERROR!)

  private static final UserRepository INSTANCE = new UserRepository();

  public static UserRepository instance() {
    return INSTANCE;
  }

  public void guardar(ar.edu.utn.frba.dds.model.lector.csv.filaconverter.model.usuario.Usuario usuario) {
    EntityManager em = ar.edu.utn.frba.dds.model.lector.csv.filaconverter.utils.DBUtils.getEntityManager(); // CREADO AQUÍ
    EntityTransaction tx = em.getTransaction();
    try {
      tx.begin();
      if (usuario.getId() == null) {
        em.persist(usuario);
      } else {
        em.merge(usuario);
      }
      tx.commit();
    } catch (Exception e) {
      if (tx != null && tx.isActive()) {
        tx.rollback();
      }

      Throwable cause = e.getCause();
      if (cause instanceof ConstraintViolationException) {
        throw new PersistenceException("El usuario o email ya existe.", e);
      }
      throw new PersistenceException("Error al guardar usuario: " + e.getMessage(), e);
    } finally {
      em.close(); // IMPRESCINDIBLE
    }
  }

  public ar.edu.utn.frba.dds.model.lector.csv.filaconverter.model.usuario.Usuario findByEmail(String email) {
    EntityManager em = ar.edu.utn.frba.dds.model.lector.csv.filaconverter.utils.DBUtils.getEntityManager();
    try {
      return em.createQuery("SELECT u FROM Usuario u WHERE u.email = :email", ar.edu.utn.frba.dds.model.lector.csv.filaconverter.model.usuario.Usuario.class)
               .setParameter("email", email)
               .getSingleResult();
    } catch (NoResultException e) {
      return null;
    } finally {
      em.close();
    }
  }

  public ar.edu.utn.frba.dds.model.lector.csv.filaconverter.model.usuario.Usuario findByName(String nombre) {
    EntityManager em = ar.edu.utn.frba.dds.model.lector.csv.filaconverter.utils.DBUtils.getEntityManager();
    try {
      return em.createQuery(
                   "SELECT DISTINCT u FROM Usuario u LEFT JOIN FETCH u.hechos WHERE u.userName = :userName",
                   ar.edu.utn.frba.dds.model.lector.csv.filaconverter.model.usuario.Usuario.class
               )
               .setParameter("userName", nombre)
               .getSingleResult();
    } catch (NoResultException e) {
      return null;
    } finally {
      em.close();
    }
  }

  public boolean emailExists(String email) {
    EntityManager em = ar.edu.utn.frba.dds.model.lector.csv.filaconverter.utils.DBUtils.getEntityManager();
    try {
      Long count = em.createQuery(
                         "SELECT COUNT(u) FROM Usuario u WHERE u.email = :email",
                         Long.class
                     )
                     .setParameter("email", email)
                     .getSingleResult();
      return count > 0;
    } finally {
      em.close();
    }
  }

  public List<ar.edu.utn.frba.dds.model.lector.csv.filaconverter.model.usuario.Usuario> findAll() {
    EntityManager em = ar.edu.utn.frba.dds.model.lector.csv.filaconverter.utils.DBUtils.getEntityManager();
    try {
      return em.createQuery("SELECT u FROM Usuario u", ar.edu.utn.frba.dds.model.lector.csv.filaconverter.model.usuario.Usuario.class)
               .getResultList();
    } finally {
      em.close();
    }
  }

  public ar.edu.utn.frba.dds.model.lector.csv.filaconverter.model.usuario.Usuario findById(Long id) {
    EntityManager em = ar.edu.utn.frba.dds.model.lector.csv.filaconverter.utils.DBUtils.getEntityManager();
    try {
      return em.createQuery(
                   "SELECT u FROM Usuario u LEFT JOIN FETCH u.hechos where u.id = :id",
                   ar.edu.utn.frba.dds.model.lector.csv.filaconverter.model.usuario.Usuario.class
               )
               .setParameter("id", id)
               .getSingleResult();
    } finally {
      em.close();
    }
  }

  /**
   * Busca un usuario por email
   */
  public ar.edu.utn.frba.dds.model.lector.csv.filaconverter.model.usuario.Usuario buscarPorEmail(String email) {
    EntityManager em = DBUtils.getEntityManager();
    try {
      List<ar.edu.utn.frba.dds.model.lector.csv.filaconverter.model.usuario.Usuario> usuarios = em.createQuery(
                                     "SELECT u FROM Usuario u WHERE u.email = :email", ar.edu.utn.frba.dds.model.lector.csv.filaconverter.model.usuario.Usuario.class)
                                                                                                  .setParameter("email", email)
                                                                                                  .getResultList();

      return usuarios.isEmpty() ? null : usuarios.get(0);
    } finally {
      em.close();
    }
  }
}
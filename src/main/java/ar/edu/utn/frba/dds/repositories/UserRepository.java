package ar.edu.utn.frba.dds.repositories;

import ar.edu.utn.frba.dds.model.usuario.Usuario;
import ar.edu.utn.frba.dds.utils.DBUtils;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceException;
import org.hibernate.exception.ConstraintViolationException;

/**
 * Repositorio de Usuarios. Maneja la persistencia.
 */
public class UserRepository {

  private final EntityManager em = DBUtils.getEntityManager();

  private static final UserRepository INSTANCE = new UserRepository();

  public static UserRepository instance() {
    return INSTANCE;
  }

  /**
   * Guarda un nuevo usuario o actualiza uno existente.
   * Lanza una excepción específica si el usuario ya existe (por unique constraints).
   *
   * @param usuario El usuario a persistir o actualizar.
   * @throws PersistenceException Si ocurre un error al guardar (ej: duplicado).
   */
  public void guardar(Usuario usuario) {
    EntityTransaction tx = em.getTransaction();
    try {
      if (!tx.isActive()) {
        tx.begin();
      }

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
        throw new PersistenceException("El usuario o email ya existe en la base de datos.", e);
      }

      throw new PersistenceException("Error al guardar el usuario: " + e.getMessage(), e);
    }
  }

  /**
   * Busca un usuario por su email.
   *
   * @param email El email del usuario.
   * @return El Usuario si se encuentra, o null si no.
   */
  public Usuario findByEmail(String email) {
    try {
      return em.createQuery("SELECT u FROM Usuario u WHERE u.email = :email", Usuario.class)
               .setParameter("email", email)
               .getSingleResult();
    } catch (NoResultException e) {
      return null;
    }
  }

  public Usuario findByName(String nombre) {
    try {
      return em.createQuery("SELECT u FROM Usuario u WHERE u.userName = :userName", Usuario.class)
               .setParameter("userName", nombre)
               .getSingleResult();
    } catch (NoResultException e) {
      return null;
    }
  }

  /**
   * Valida si un email ya existe.
   */
  public boolean emailExists(String email) {
    Long count = em.createQuery("SELECT COUNT(u) FROM Usuario u WHERE u.email = :email", Long.class)
                   .setParameter("email", email)
                   .getSingleResult();
    return count > 0;
  }

  public List<Usuario> findAll() {
    return em.createQuery("SELECT u FROM Usuario u", Usuario.class)
             .getResultList();
  }

  public Usuario findById(Long id) {
    return em.find(Usuario.class, id);
  }
}
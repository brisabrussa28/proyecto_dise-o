package ar.edu.utn.frba.dds.repositories;

import ar.edu.utn.frba.dds.model.usuario.Usuario;
import ar.edu.utn.frba.dds.utils.DBUtils;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;

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
   * Guarda un nuevo usuario.
   *
   * @param usuario El usuario a persistir.
   */
  public void guardar(Usuario usuario) {
    DBUtils.comenzarTransaccion(em);
    try {
      em.persist(usuario);
      DBUtils.commit(em);
    } catch (Exception e) {
      DBUtils.rollback(em);
      throw new RuntimeException("Error al guardar el usuario: " + e.getMessage(), e);
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
      return null; // No se encontró el usuario
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
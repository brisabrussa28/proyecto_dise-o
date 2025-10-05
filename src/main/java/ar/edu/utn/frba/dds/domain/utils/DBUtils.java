package ar.edu.utn.frba.dds.domain.utils;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import java.util.TimeZone;

public class DBUtils {
  // La fábrica ahora no es final y se inicializará de forma "lazy" (perezosa)
  private static EntityManagerFactory factory;

  // Bloque estático para configurar la zona horaria UNA SOLA VEZ al cargar la clase.
  // Esto se ejecuta antes que cualquier método de esta clase sea llamado.
  static {
    TimeZone.setDefault(TimeZone.getTimeZone("America/Argentina/Buenos_Aires"));
  }

  // Método sincronizado para asegurar que la fábrica se cree una sola vez (thread-safe)
  private static synchronized EntityManagerFactory getFactory() {
    if (factory == null) {
      factory = Persistence.createEntityManagerFactory("simple-persistence-unit");
    }
    return factory;
  }

  public static EntityManager getEntityManager() {
    // Ahora usa el método getFactory() para obtener la instancia
    return getFactory().createEntityManager();
  }

  public static void comenzarTransaccion(EntityManager em) {
    EntityTransaction tx = em.getTransaction();
    if (!tx.isActive()) {
      tx.begin();
    }
  }

  public static void commit(EntityManager em) {
    EntityTransaction tx = em.getTransaction();
    if (tx.isActive()) {
      tx.commit();
    }
  }

  public static void rollback(EntityManager em) {
    EntityTransaction tx = em.getTransaction();
    if (tx.isActive()) {
      tx.rollback();
    }
  }
}

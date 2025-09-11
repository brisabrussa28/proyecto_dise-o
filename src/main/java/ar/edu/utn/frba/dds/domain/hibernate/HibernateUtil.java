package ar.edu.utn.frba.dds.domain.hibernate;


import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

public class HibernateUtil {

  private static final SessionFactory sessionFactory = buildSessionFactory();

  private static SessionFactory buildSessionFactory() {
    try {
      // Crea el SessionFactory desde hibernate.cfg.xml
      return new Configuration().configure()
                                .buildSessionFactory();
    } catch (Throwable ex) {
      System.err.println("La creación inicial del SessionFactory falló." + ex);
      throw new ExceptionInInitializerError(ex);
    }
  }

  public static SessionFactory getSessionFactory() {
    return sessionFactory;
  }
}
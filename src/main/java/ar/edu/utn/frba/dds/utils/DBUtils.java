package ar.edu.utn.frba.dds.utils;

import ar.edu.utn.frba.dds.model.geolocalizacion.GeoApi;
import ar.edu.utn.frba.dds.model.geolocalizacion.GeoGeoref;
import ar.edu.utn.frba.dds.model.hecho.EnriquecedorDeHechos;
import ar.edu.utn.frba.dds.model.hecho.Hecho;
import io.github.cdimascio.dotenv.Dotenv;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;

public class DBUtils {
  private static EntityManagerFactory factory;

  static {
    Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();
    String timezone = dotenv.get("TIMEZONE", "America/Argentina/Buenos_Aires");
    TimeZone.setDefault(TimeZone.getTimeZone(timezone));
  }

  // Método sincronizado para asegurar que la fábrica se cree una sola vez (thread-safe)
  private static synchronized EntityManagerFactory getFactory() {
    if (factory == null) {
      Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();

      Map<String, String> configOverrides = new HashMap<>();

      String dbUrl = dotenv.get("DB_URL");
      if (dbUrl != null) {
        configOverrides.put("hibernate.connection.url", dbUrl);
      }

      String dbUser = dotenv.get("DB_USER");
      if (dbUser != null) {
        configOverrides.put("hibernate.connection.username", dbUser);
      }

      String dbPassword = dotenv.get("DB_PASSWORD");
      if (dbPassword != null) {
        configOverrides.put("hibernate.connection.password", dbPassword);
      }

      String dbTimezone = dotenv.get("TIMEZONE");
      if (dbTimezone != null) {
        configOverrides.put("hibernate.jdbc.time_zone", dbTimezone);
      }

      factory = Persistence.createEntityManagerFactory("simple-persistence-unit", configOverrides);

      inicializarExtensiones();
    }
    return factory;
  }

  private static void inicializarExtensiones() {
    EntityManager em = null;
    EntityTransaction tx = null;

    try {
      em = factory.createEntityManager();
      tx = em.getTransaction();
      tx.begin();

      em.createNativeQuery("CREATE EXTENSION IF NOT EXISTS fuzzystrmatch").executeUpdate();

      tx.commit();
      System.out.println("✅ Extensión fuzzystrmatch verificada/creada correctamente.");
    } catch (Exception e) {
      if (tx != null && tx.isActive()) {
        tx.rollback();
      }
      System.err.println("⚠️ Error al inicializar extensiones de BD: " + e.getMessage());
    } finally {
      if (em != null) {
        em.close();
      }
    }
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

  /**
   * Enriquece un único hecho con datos geográficos faltantes (provincia o ubicación)
   * utilizando el EnriquecedorDeHechos.
   * La operación es síncrona y modifica el objeto Hecho proporcionado.
   *
   * @param hecho El Hecho a enriquecer.
   */
  public static void enriquecerHecho(Hecho hecho) {
    if (hecho == null) {
      return;
    }
    // Determina si el hecho necesita ser enriquecido para evitar llamadas innecesarias a la API.
    boolean necesitaProvincia = (hecho.getProvincia() == null || hecho.getProvincia().isBlank()) && hecho.getUbicacion() != null;
    boolean necesitaUbicacion = hecho.getUbicacion() == null && (hecho.getProvincia() != null && !hecho.getProvincia().isBlank());

    if (!necesitaProvincia && !necesitaUbicacion) {
      return; // No hay nada que hacer.
    }

    try {
      GeoApi servicioGeo = new GeoGeoref();
      EnriquecedorDeHechos enriquecedor = new EnriquecedorDeHechos(servicioGeo);

      // El enriquecedor trabaja con listas, así que envolvemos el hecho en una.
      List<Hecho> listaOriginal = Collections.singletonList(hecho);

      // El método `completar` devuelve una nueva lista con los hechos enriquecidos.
      List<Hecho> listaEnriquecida = enriquecedor.completar(listaOriginal);

      // Si el enriquecimiento fue exitoso, copiamos los datos al objeto original.
      if (listaEnriquecida != null && !listaEnriquecida.isEmpty()) {
        Hecho hechoEnriquecido = listaEnriquecida.get(0);
        hecho.setProvincia(hechoEnriquecido.getProvincia());
        hecho.setUbicacion(hechoEnriquecido.getUbicacion());
      }
    } catch (Exception e) {
      // Manejo de errores si la llamada a la API o el enriquecimiento fallan
      System.err.println("Error al enriquecer el hecho ID " + hecho.getId() + ": " + e.getMessage());
    }
  }
}
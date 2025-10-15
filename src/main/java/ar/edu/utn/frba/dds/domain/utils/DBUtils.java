package ar.edu.utn.frba.dds.domain.utils;

import ar.edu.utn.frba.dds.domain.geolocalizacion.GeoApi;
import ar.edu.utn.frba.dds.domain.geolocalizacion.GeoGeoref;
import ar.edu.utn.frba.dds.domain.hecho.EnriquecedorDeHechos;
import ar.edu.utn.frba.dds.domain.hecho.Hecho;
import java.util.Collections;
import java.util.List;
import java.util.TimeZone;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;

public class DBUtils {
  private static EntityManagerFactory factory;

  static {
    TimeZone.setDefault(TimeZone.getTimeZone("America/Argentina/Buenos_Aires"));
  }

  private static synchronized EntityManagerFactory getFactory() {
    if (factory == null) {
      factory = Persistence.createEntityManagerFactory("simple-persistence-unit");
    }
    return factory;
  }

  public static EntityManager getEntityManager() {
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


package ar.edu.utn.frba.dds.repositories;

import ar.edu.utn.frba.dds.model.reportes.detectorspam.VectorTFIDF;
import ar.edu.utn.frba.dds.utils.DBUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceException;
import javax.persistence.TypedQuery;

public class TFIDFRepository {

  private static final TFIDFRepository INSTANCE = new TFIDFRepository();

  public static TFIDFRepository instance() {
    return INSTANCE;
  }

  private TFIDFRepository() {
    // Constructor privado para singleton
  }

  /**
   * Guarda un vector TF-IDF en la base de datos
   */
  public void guardarVector(String textoOriginal, Map<String, Double> vector, boolean esSpam) {
    System.out.println("Guardando vector TF-IDF...");
    System.out.println("Texto: " + (textoOriginal.length() > 50 ? textoOriginal.substring(0, 50) + "..." : textoOriginal));
    System.out.println("Es spam: " + esSpam);
    System.out.println("Vector tamaño: " + vector.size());

    EntityManager em = null;
    try {
      em = DBUtils.getEntityManager();
      System.out.println("EntityManager obtenido");

      DBUtils.comenzarTransaccion(em);
      System.out.println("Transacción comenzada");

      VectorTFIDF vectorEntity = new VectorTFIDF(textoOriginal, vector, esSpam);
      System.out.println("Vector entity creado: " + vectorEntity);

      em.persist(vectorEntity);
      System.out.println("Entity persistida");

      DBUtils.commit(em);
      System.out.println("Transacción commitada - Vector guardado exitosamente");

    } catch (PersistenceException e) {
      System.err.println("ERROR de persistencia al guardar vector TF-IDF: " + e.getMessage());
      if (em != null) {
        try {
          DBUtils.rollback(em);
          System.err.println("Rollback realizado");
        } catch (Exception rollbackEx) {
          System.err.println("Error al hacer rollback: " + rollbackEx.getMessage());
        }
      }
      throw new RuntimeException("Error al guardar vector TF-IDF: " + e.getMessage(), e);
    } catch (Exception e) {
      System.err.println("ERROR inesperado al guardar vector TF-IDF: " + e.getMessage());
      e.printStackTrace();
      if (em != null) {
        try {
          DBUtils.rollback(em);
        } catch (Exception rollbackEx) {
          System.err.println("Error al hacer rollback: " + rollbackEx.getMessage());
        }
      }
      throw new RuntimeException("Error inesperado al guardar vector TF-IDF", e);
    } finally {
      if (em != null && em.isOpen()) {
        em.close();
        System.out.println("EntityManager cerrado");
      }
    }
  }

  /**
   * Obtiene todos los vectores marcados como spam
   */
  public List<Map<String, Double>> obtenerVectoresSpam() {
    EntityManager em = null;
    try {
      em = DBUtils.getEntityManager();

      TypedQuery<VectorTFIDF> query = em.createQuery(
          "SELECT v FROM VectorTFIDF v WHERE v.esSpam = true ORDER BY v.fechaCreacion DESC",
          VectorTFIDF.class);

      List<VectorTFIDF> resultados = query.getResultList();
      System.out.println("Consultados " + resultados.size() + " vectores spam de la base de datos");

      List<Map<String, Double>> vectores = new ArrayList<>();

      for (VectorTFIDF vectorEntity : resultados) {
        try {
          vectores.add(vectorEntity.getVector());
        } catch (Exception e) {
          System.err.println("Error al obtener vector ID " + vectorEntity.getId() + ": " + e.getMessage());
        }
      }

      return vectores;

    } finally {
      if (em != null && em.isOpen()) {
        em.close();
      }
    }
  }

  /**
   * Verifica si un texto similar ya existe en la base de datos
   */
  public boolean existeTextoSimilar(String texto) {
    if (texto == null || texto.trim().isEmpty()) {
      return false;
    }

    EntityManager em = null;
    try {
      em = DBUtils.getEntityManager();

      Long count = em.createQuery(
                         "SELECT COUNT(v) FROM VectorTFIDF v WHERE LOWER(v.textoOriginal) = LOWER(:texto)",
                         Long.class)
                     .setParameter("texto", texto.trim())
                     .getSingleResult();

      boolean existe = count > 0;
      System.out.println("Texto '" + texto.substring(0, Math.min(30, texto.length())) +
                             "' existe en DB: " + existe);
      return existe;

    } catch (Exception e) {
      System.err.println("Error al verificar texto similar: " + e.getMessage());
      return false;
    } finally {
      if (em != null && em.isOpen()) {
        em.close();
      }
    }
  }

  /**
   * Obtiene el número total de vectores spam almacenados
   */
  public Long contarVectoresSpam() {
    EntityManager em = null;
    try {
      em = DBUtils.getEntityManager();

      Long count = em.createQuery(
                         "SELECT COUNT(v) FROM VectorTFIDF v WHERE v.esSpam = true",
                         Long.class)
                     .getSingleResult();

      System.out.println("Contados " + count + " vectores spam en DB");
      return count != null ? count : 0L;

    } catch (Exception e) {
      System.err.println("Error al contar vectores spam: " + e.getMessage());
      return 0L;
    } finally {
      if (em != null && em.isOpen()) {
        em.close();
      }
    }
  }

  /**
   * Obtiene textos de ejemplo de spam para entrenamiento
   */
  public List<String> obtenerTextosSpamEjemplo(int limite) {
    EntityManager em = null;
    try {
      em = DBUtils.getEntityManager();

      List<String> resultados = em.createQuery(
                                      "SELECT v.textoOriginal FROM VectorTFIDF v WHERE v.esSpam = true ORDER BY v.fechaCreacion DESC",
                                      String.class)
                                  .setMaxResults(limite)
                                  .getResultList();

      System.out.println("Obtenidos " + resultados.size() + " textos spam de ejemplo");
      return resultados;

    } finally {
      if (em != null && em.isOpen()) {
        em.close();
      }
    }
  }

  /**
   * Obtiene todos los vectores (spam y no spam)
   */
  public List<VectorTFIDF> obtenerTodosLosVectores() {
    EntityManager em = null;
    try {
      em = DBUtils.getEntityManager();

      List<VectorTFIDF> resultados = em.createQuery(
                                           "SELECT v FROM VectorTFIDF v ORDER BY v.fechaCreacion DESC",
                                           VectorTFIDF.class)
                                       .getResultList();

      System.out.println("Obtenidos " + resultados.size() + " vectores totales");
      return resultados;

    } finally {
      if (em != null && em.isOpen()) {
        em.close();
      }
    }
  }
}
package ar.edu.utn.frba.dds.domain.detectorspam;

import java.util.List;
import java.util.Map;

public class CalculadorScoreTFIDF {

  // Calcula el score promedio de similitud entre un mensaje nuevo y una lista de mensajes previos
  public double calcularPromedioSimilitud(List<Map<String, Double>> vectores, Map<String, Double> tfidfNuevo) {
    if (vectores == null || vectores.isEmpty()) {
      return 0;
    }

    double acumulado = 0;
    for (Map<String, Double> tfidfExistente : vectores) {
      // Compara el mensaje nuevo con cada mensaje entrenado, midiendo cuánto se parecen y sumando la similitud
      acumulado += calcularSimilitudCoseno(tfidfNuevo, tfidfExistente);
    }

    // Devuelve el promedio de similitud
    return acumulado / vectores.size();
  }

  // Calcula la similitud coseno entre dos mapas de TF-IDF
  private double calcularSimilitudCoseno(Map<String, Double> a, Map<String, Double> b) {
    double productoEscalar = 0.0; // Producto punto entre los dos vectores
    double normA = 0.0; // Norma del vector A
    double normB = 0.0; // Norma del vector B

    //producto escalar pero en lugar de posiciones (x, y, z), tenemos palabras como claves: "dinero", "gratis", "hola".
    for (String key : a.keySet()) {
      if (b.containsKey(key)) {
        productoEscalar += a.get(key) * b.get(key);
      }
    }

    // Calcula la norma de cada vector (como lo hicimos en álgebra)
    for (double valorA : a.values()) {
      normA += valorA * valorA;
    }
    normA = Math.sqrt(normA);

    for (double valorB : b.values()) {
      normB += valorB * valorB;
    }
    normB = Math.sqrt(normB);

    if (normA == 0 || normB == 0) {
      return 0;
    }

    // Devuelve la similitud coseno, que es el coseno del ángulo entre los dos vectores
    // Esto lo calculabamos en aga para encontrar la proyeccion de a sobre b
    return productoEscalar / (normA * normB);
  }
}
package ar.edu.utn.frba.dds.domain.detectorspam;

import java.util.List;
import java.util.Map;

public class CalculadorScoreTFIDF {

  // Calcula el score promedio de similitud entre un mensaje nuevo y una lista de mensajes previos
  public double calcularPromedioSimilitud(List<Map<String, Double>> vectores, Map<String, Double> tfidfNuevo) {
    if (vectores == null || vectores.isEmpty() || tfidfNuevo.isEmpty()) {
      return 0.0;
    }

    double acumulado = 0.0;
    int vectoresValidos = 0;

    for (Map<String, Double> tfidfExistente : vectores) {
      if (tfidfExistente.isEmpty()) {
        continue; // Saltar vectores vacíos
      }

      double similitud = calcularSimilitudCoseno(tfidfNuevo, tfidfExistente);
      acumulado += similitud;
      vectoresValidos++;
    }

    return vectoresValidos > 0 ? acumulado / vectoresValidos : 0.0;
  }

  // Calcula la similitud coseno entre dos mapas de TF-IDF normalizados
  public double calcularSimilitudCoseno(Map<String, Double> a, Map<String, Double> b) {
    if (a.isEmpty() || b.isEmpty()) {
      return 0.0;
    }

    double productoEscalar = 0.0;

    // Optimización: iterar sobre el vector más pequeño
    Map<String, Double> menor = a.size() <= b.size() ? a : b;
    Map<String, Double> mayor = a.size() <= b.size() ? b : a;

    for (Map.Entry<String, Double> entry : menor.entrySet()) {
      Double valorMayor = mayor.get(entry.getKey());
      if (valorMayor != null) {
        productoEscalar += entry.getValue() * valorMayor;
      }
    }

    // Nota: Si los vectores están normalizados (norma = 1),
    // el producto escalar ES la similitud coseno
    return productoEscalar;
  }

  // Versión alternativa más explícita (para depuración)
  public double calcularSimilitudCosenoCompleta(Map<String, Double> a, Map<String, Double> b) {
    if (a.isEmpty() || b.isEmpty()) {
      return 0.0;
    }

    double productoEscalar = 0.0;
    double normA = 0.0;
    double normB = 0.0;

    // Usar todos los términos de ambos vectores
    for (Map.Entry<String, Double> entry : a.entrySet()) {
      double valA = entry.getValue();
      normA += valA * valA;

      Double valB = b.get(entry.getKey());
      if (valB != null) {
        productoEscalar += valA * valB;
      }
    }

    for (double valB : b.values()) {
      normB += valB * valB;
    }

    normA = Math.sqrt(normA);
    normB = Math.sqrt(normB);

    if (normA == 0.0 || normB == 0.0) {
      return 0.0;
    }

    return productoEscalar / (normA * normB);
  }
}
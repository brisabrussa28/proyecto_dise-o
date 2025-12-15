package ar.edu.utn.frba.dds.model.reportes.detectorspam;

import ar.edu.utn.frba.dds.repositories.TFIDFRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DetectorSpamTFIDF implements DetectorSpam {

  private static final double UMBRAL_SIMILITUD = 0.7;
  private final CalculadorScoreTFIDF calculador;
  private final TFIDFRepository repositorio;

  public DetectorSpamTFIDF() {
    this.calculador = new CalculadorScoreTFIDF();
    this.repositorio = TFIDFRepository.instance();
  }

  @Override
  public boolean esSpam(String texto) {
    if (texto == null || texto.trim().isEmpty()) {
      return false;
    }

    String textoLimpio = limpiarTexto(texto);

    // Verificar si el texto exacto ya está en la base de datos
    if (repositorio.existeTextoSimilar(textoLimpio)) {
      return true;
    }

    // Calcular vector TF-IDF del nuevo texto
    Map<String, Double> vectorNuevo = calcularVectorTFIDF(textoLimpio);
    if (vectorNuevo == null || vectorNuevo.isEmpty()) {
      return false;
    }

    // Obtener vectores de spam de la base de datos
    List<Map<String, Double>> vectoresSpam = repositorio.obtenerVectoresSpam();

    if (vectoresSpam.isEmpty()) {
      return false;
    }

    // Calcular similitud promedio con los vectores existentes
    double similitudPromedio = calculador.calcularPromedioSimilitud(vectoresSpam, vectorNuevo);

    // Si la similitud supera el umbral, es spam
    return similitudPromedio >= UMBRAL_SIMILITUD;
  }

  public Map<String, Double> calcularVectorTFIDF(String texto) {
    if (texto == null || texto.trim().isEmpty()) {
      return new HashMap<>();
    }

    String textoLimpio = limpiarTexto(texto);
    Map<String, Double> frecuencias = new HashMap<>();

    String[] tokens = textoLimpio.toLowerCase().split("\\s+");
    int totalTokens = tokens.length;

    if (totalTokens == 0) {
      return new HashMap<>();
    }

    for (String token : tokens) {
      if (token.length() > 2 && !esPalabraComun(token)) {
        frecuencias.put(token, frecuencias.getOrDefault(token, 0.0) + 1.0);
      }
    }

    return normalizarVector(frecuencias);
  }

  private boolean esPalabraComun(String palabra) {
    String[] palabrasComunes = {"el", "la", "los", "las", "un", "una", "unos", "unas",
        "de", "del", "al", "y", "o", "pero", "por", "para",
        "con", "sin", "sobre", "entre", "hasta", "desde"};

    for (String comun : palabrasComunes) {
      if (comun.equals(palabra.toLowerCase())) {
        return true;
      }
    }
    return false;
  }

  private String limpiarTexto(String texto) {
    if (texto == null) return "";

    return texto.trim()
                .replaceAll("[^a-zA-ZáéíóúüñÁÉÍÓÚÜÑ0-9\\s]", " ")
                .replaceAll("\\s+", " ")
                .trim();
  }

  private Map<String, Double> normalizarVector(Map<String, Double> vector) {
    if (vector == null || vector.isEmpty()) {
      return new HashMap<>();
    }

    double norma = Math.sqrt(vector.values().stream()
                                   .mapToDouble(v -> v * v)
                                   .sum());

    if (norma <= 0) {
      return vector;
    }

    Map<String, Double> normalizado = new HashMap<>();
    vector.forEach((k, v) -> normalizado.put(k, v / norma));

    return normalizado;
  }

  public void entrenarConSpam(String textoSpam) {
    if (textoSpam == null || textoSpam.trim().isEmpty()) {
      return;
    }

    String textoLimpio = limpiarTexto(textoSpam);

    if (repositorio.existeTextoSimilar(textoLimpio)) {
      return;
    }

    Map<String, Double> vector = calcularVectorTFIDF(textoLimpio);
    if (!vector.isEmpty()) {
      repositorio.guardarVector(textoLimpio, vector, true);
    }
  }
}
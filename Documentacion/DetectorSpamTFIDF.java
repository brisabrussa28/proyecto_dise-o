package ar.edu.utn.frba.dds.domain.detectorspam;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class DetectorSpamTFIDF {

  private final List<String> mensajesSpam = new ArrayList<>();
  private final List<String> mensajesNoSpam = new ArrayList<>();
  private int totalMensajes = 0;

  private final CalculadorScoreTFIDF calculador = new CalculadorScoreTFIDF();

  public DetectorSpamTFIDF() {
    entrenamientoInicial();
  }

  // -------------------------
  // ENTRENAMIENTO INICIAL
  // -------------------------
  private void entrenamientoInicial() {
    entrenar("ganá dinero fácil y rápido", true);
    entrenar("oferta exclusiva solo hoy", true);
    entrenar("compra sin tarjeta y sin requisitos", true);
    entrenar("compra sin tarjeta y sin requisitos", true);

    entrenar("nos juntamos mañana en casa", false);
    entrenar("recordá traer los documentos", false);
    entrenar("no olvides comprar leche", false);
    entrenar("Este hecho tiene informacion que podria llevar al arresto de cierto politico", false);
  }

  public void entrenar(String mensaje, boolean esSpam) {
    if (esSpam) {
      mensajesSpam.add(mensaje);
    } else {
      mensajesNoSpam.add(mensaje);
    }
    totalMensajes++;
  }

  public boolean esSpam(String mensajeNuevo) {
    Map<String, Double> tfidfMensajeNuevo = calcularTFIDF(mensajeNuevo);

    List<Map<String, Double>> vectoresSpam = mensajesSpam.stream()
        .map(this::calcularTFIDF)
        .toList();
    List<Map<String, Double>> vectoresNoSpam = mensajesNoSpam.stream()
        .map(this::calcularTFIDF)
        .toList();

    double scoreSpam = calculador.calcularPromedioSimilitud(vectoresSpam, tfidfMensajeNuevo);
    double scoreNoSpam = calculador.calcularPromedioSimilitud(vectoresNoSpam, tfidfMensajeNuevo);

    return scoreSpam > scoreNoSpam;
  }

  private Map<String, Double> calcularTFIDF(String mensaje) {
    Map<String, Double> tf = new HashMap<>();
    List<String> palabras = Arrays.asList(mensaje.toLowerCase().split("\\W+"));

    for (String palabra : palabras) {
      tf.put(palabra, tf.getOrDefault(palabra, 0.0) + 1.0);
    }

    // Normalización TF
    tf.replaceAll((p, v) -> tf.get(p) / palabras.size());

    Map<String, Double> tfidf = new HashMap<>();
    for (Map.Entry<String, Double> entry : tf.entrySet()) {
      String palabra = entry.getKey();
      double tfValue = entry.getValue();
      int df = contarDocumentosQueContienen(palabra);
      double idf = df == 0 ? 0 : Math.log((double) totalMensajes / df); // calculo idf, evitando división por cero
      tfidf.put(palabra, tfValue * idf); // TF-IDF = TF * IDF
    }

    return tfidf;
  }

  private int contarDocumentosQueContienen(String palabra) {
    int count = 0;
    for (String mensaje : mensajesSpam) {
      if (obtenerPalabrasUnicas(mensaje).contains(palabra)) {
        count++;
      }
    }
    for (String mensaje : mensajesNoSpam) {
      if (obtenerPalabrasUnicas(mensaje).contains(palabra)) {
        count++;
      }
    }
    return count;
  }

  private Set<String> obtenerPalabrasUnicas(String mensaje) {
    /* No pregunten lo de la expresión regular, originalmente era un espacio pero chadgpt me recomendó esto:
     * \\W = cualquier carácter que NO sea una letra
     * + = Clausura positiva, uno o más repeticiones de la expresión regular (gracias doctor bruno)
     * \\W+ = uno o más caracteres que no son letras, números o guiones bajos
     */
    return new HashSet<>(Arrays.asList(mensaje.toLowerCase().split("\\W+")));
  }
}
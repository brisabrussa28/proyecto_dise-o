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
    // Ejemplos de SPAM
    entrenar("ganá dinero fácil y rápido sin moverte de tu casa, solo con hacer clic en un enlace", true);
    entrenar("oferta exclusiva solo por hoy, no te la pierdas, hacé clic ahora y participá", true);
    entrenar("compra sin tarjeta, sin requisitos, sin demoras, todo rápido y fácil", true);
    entrenar("gana dinero desde casa sin invertir un solo peso, oportunidad única garantizada", true);
    entrenar("haz clic aquí para obtener tu premio gratuito, sin condiciones ni restricciones", true);
    entrenar("sigue este enlace para más información sobre cómo duplicar tus ingresos en días", true);
    entrenar("te regalo criptomonedas gratis por registrarte en esta página, oferta limitada", true);
    entrenar("promoción exclusiva sin tarjeta, sin trámites, obtené lo que querés ya mismo", true);
    entrenar("accede ya a nuestra oferta única de inversión con retornos increíbles", true);
    entrenar("¡última oportunidad para ganar premios en efectivo instantáneamente!", true);

    // Ejemplos de NO SPAM
    entrenar("nos juntamos mañana en casa para revisar los informes que dejamos pendientes", false);
    entrenar("recordá traer los documentos y copias firmadas como lo solicitó el comité", false);
    entrenar("El hecho contiene información incorrecta que puede inducir a errores graves", false);
    entrenar("La noticia está desactualizada y se debería actualizar con datos actuales", false);
    entrenar("El contenido puede causar malentendidos en la comunidad, sugiero revisarlo", false);
    entrenar("Solicito eliminar porque es ofensivo hacia una persona en particular", false);
    entrenar("Considero que es una información privada que no debería hacerse pública", false);
    entrenar("Está repetido en el sistema, ya existe un hecho exactamente igual", false);
    entrenar("No corresponde al hecho informado originalmente en el reporte inicial", false);
    entrenar(
        "Este hecho tiene informacion que podria llevar al arresto de cierto politico y su publicación debe ser evaluada con cuidado",
        false
    );
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
    List<String> palabras = Arrays.asList(mensaje.toLowerCase()
                                                 .split("\\W+"));

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
    return new HashSet<>(Arrays.asList(mensaje.toLowerCase()
                                              .split("\\W+")));
  }
}